#!/usr/bin/env python3
"""Smoke-test the native app in release-files/: health, then one NASA → PostgreSQL → API result."""

import json
import os
import signal
import subprocess
import sys
import time
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

APP_URL = "http://127.0.0.1:18080"
NASA_MOCK_URL = "http://127.0.0.1:18081"


def request(url, payload=None, timeout=3):
    # Both limits cover the whole transfer, including a trickling response body.
    command = ["curl", "--fail", "--silent", "--show-error", "--max-time", str(timeout)]
    if payload is not None:
        command += ["-H", "Content-Type: application/json", "--data-binary", json.dumps(payload)]
    result = subprocess.run(command + [url], check=True, capture_output=True, timeout=timeout)
    return json.loads(result.stdout or b"null")


def main():
    if os.environ.get("DB_URL") != "jdbc:postgresql://127.0.0.1:5432/smoke_test":
        raise RuntimeError("DB_URL must point to the temporary smoke_test database.")
    today = datetime.now(ZoneInfo("America/New_York")).date()
    apod_path = f"/wp-json/wp/v2/apod-basic/{today:%y%m%d}"
    image_url = "https://example.invalid/image.jpg"
    permalink = "https://example.invalid/apod/ci-astronomy-fixture/"
    expected = {
        "date": today.isoformat(), "title": "CI astronomy fixture", "media_type": "image",
        "url": image_url, "source_url": permalink,
    }
    request(f"{NASA_MOCK_URL}/__admin/mappings", {
        "request": {"method": "GET", "url": apod_path},
        "response": {
            "status": 200,
            "headers": {"Content-Type": "application/json"},
            "jsonBody": {
                "date": today.isoformat(), "post_id": 1, "title": "CI astronomy fixture",
                "permalink": permalink, "explanation": "Smoke-test fixture.",
                "media_type": "image", "url": permalink,
                "basic_html": f'<html><body><img src="{image_url}"></body></html>',
                "basic_html_url": f"{NASA_MOCK_URL}{apod_path}/html",
            },
        },
    })

    # collect-release-files already checked the file and copied its shared libraries beside it.
    executable = Path("release-files/app").resolve(strict=True)
    log_path = Path("target/smoke-test-native-app.log")
    log_path.parent.mkdir(parents=True, exist_ok=True)
    process = None
    try:
        with log_path.open("w") as log:
            process = subprocess.Popen([
                str(executable), "--server.address=127.0.0.1", "--server.port=18080",
                f"--nasa.api.base-url={NASA_MOCK_URL}", "--scheduler.fetch-apod.cron=*/3 * * * * *",
                f"--media.base-url={APP_URL}/media",
                f"--media.thumbnail-dir={log_path.parent.resolve() / 'smoke-test-thumbnails'}",
            ], cwd=executable.parent, stdout=log, stderr=subprocess.STDOUT)
            deadline = time.monotonic() + 90
            healthy = False
            last_error = "No matching response yet."
            while (remaining := deadline - time.monotonic()) > 0:
                if process.poll() is not None:
                    raise RuntimeError(f"Native executable exited with code {process.returncode}.")
                url = f"{APP_URL}/apods?start_date={today}&end_date={today}" if healthy else f"{APP_URL}/actuator/health"
                try:
                    response = request(url, timeout=min(3, remaining))
                    if healthy:
                        if (isinstance(response, list) and len(response) == 1
                                and isinstance(response[0], dict)
                                and all(response[0].get(key) == value for key, value in expected.items())):
                            break
                    else:
                        healthy = isinstance(response, dict) and response.get("status") == "UP"
                except (OSError, ValueError, subprocess.SubprocessError) as error:
                    last_error = str(error)
                time.sleep(min(0.25, max(0, deadline - time.monotonic())))
            else:
                raise RuntimeError(f"Smoke test timed out after 90 seconds: {last_error}")
    except BaseException:
        if log_path.exists():
            print(log_path.read_text(errors="replace")[-18000:], file=sys.stderr)
        raise
    finally:
        if process is not None and process.poll() is None:
            # Ignore repeat cancellation signals until bounded cleanup finishes.
            signal.signal(signal.SIGINT, signal.SIG_IGN)
            signal.signal(signal.SIGTERM, signal.SIG_IGN)
            process.terminate()
            try:
                process.wait(timeout=15)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait(timeout=5)
    print("PASS: native health and one NASA → PostgreSQL → API response.")


def cancel(signum, _frame):
    raise SystemExit(128 + signum)


if __name__ == "__main__":
    signal.signal(signal.SIGINT, cancel)
    signal.signal(signal.SIGTERM, cancel)
    try:
        main()
    except (OSError, ValueError, RuntimeError, subprocess.SubprocessError) as error:
        message = str(error).replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
        print(f"::error::{message}", file=sys.stderr)
        sys.exit(1)
