#!/usr/bin/env python3
"""Decide whether pom.xml asks for a new release compared with BASE_SHA.

With --print-version PATH, only print the project version of that POM file.
"""

import os
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

VERSION = re.compile(r"(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-SNAPSHOT)?")


def git(*arguments):
    return subprocess.run(
        ["git", *arguments], check=True, capture_output=True, text=True
    ).stdout.strip()


def project_version(xml, source):
    try:
        project = ET.fromstring(xml)
    except ET.ParseError as error:
        raise ValueError(f"Invalid XML in {source}: {error}") from error
    versions = project.findall("{*}version")
    if project.tag.rsplit("}", 1)[-1] != "project" or len(versions) != 1:
        raise ValueError(f"{source} must contain exactly one direct project version.")
    version = " ".join("".join(versions[0].itertext()).split())
    match = VERSION.fullmatch(version)
    if not match:
        raise ValueError(f"{source}: use MAJOR.MINOR.PATCH[-SNAPSHOT] without leading zeros, got {version!r}.")
    return version, tuple(int(part) for part in match.groups()[:3])


def detect_release():
    base = os.environ.get("BASE_SHA", "")
    if not re.fullmatch(r"[0-9a-fA-F]{40}", base) or base == "0" * 40:
        raise ValueError("BASE_SHA is missing or invalid; refusing to guess a release.")
    try:
        git("cat-file", "-e", f"{base}^{{commit}}")
        base_xml = git("show", f"{base}:pom.xml")
    except subprocess.CalledProcessError as error:
        raise ValueError("Cannot read pom.xml at BASE_SHA; checkout must use fetch-depth: 0.") from error
    version, current = project_version(Path("pom.xml").read_text(encoding="utf-8"), "pom.xml")
    base_version, base_numbers = project_version(base_xml, "pom.xml at BASE_SHA")
    if version == base_version or version.endswith("-SNAPSHOT"):
        return version, False
    if current < base_numbers or (current == base_numbers and not base_version.endswith("-SNAPSHOT")):
        raise ValueError(f"Version must increase from {base_version}; got {version}.")
    for tag in git("tag", "--list", "v*").splitlines():
        match = VERSION.fullmatch(tag[1:])
        if not match or match[4]:
            continue
        tag_numbers = tuple(int(part) for part in match.groups()[:3])
        if current < tag_numbers:
            raise ValueError(f"Version {version} is older than existing release {tag}.")
        if current == tag_numbers and git("rev-parse", f"{tag}^{{commit}}") != os.environ.get("GITHUB_SHA"):
            raise ValueError(f"Release {tag} already belongs to another commit.")
    return version, True


def main():
    if len(sys.argv) == 3 and sys.argv[1] == "--print-version":
        path = Path(sys.argv[2])
        print(project_version(path.read_text(encoding="utf-8"), str(path))[0])
        return
    if len(sys.argv) != 1:
        raise ValueError("Usage: detect-release.py [--print-version PATH]")
    version, should_release = detect_release()
    with Path(os.environ["GITHUB_OUTPUT"]).open("a", encoding="utf-8") as output:
        output.write(f"version={version}\nshould-release={str(should_release).lower()}\n")
    print(f"{'New release' if should_release else 'No release'}: {version}.")


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, KeyError, subprocess.CalledProcessError) as error:
        message = error.stderr.strip() if isinstance(error, subprocess.CalledProcessError) else str(error)
        message = message.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
        print(f"::error::{message}", file=sys.stderr)
        sys.exit(1)
