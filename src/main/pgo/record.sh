#!/usr/bin/env bash
# Records src/main/pgo/default.iprof, the profile that native builds use for
# profile-guided optimization (see the native profile in pom.xml).
#
# Needs GraalVM 25 as JAVA_HOME, hey (brew install hey), and the local
# PostgreSQL database with APOD data. Run from anywhere:
#   JAVA_HOME=$(/usr/libexec/java_home -v 25) src/main/pgo/record.sh
# Set DB_URL, DB_USERNAME or DB_PASSWORD first if your local database differs.
set -euo pipefail
cd "$(dirname "$0")/../../.."

command -v hey >/dev/null || { echo "hey is missing: brew install hey" >&2; exit 1; }

export SPRING_PROFILES_ACTIVE=prod
export DB_URL="${DB_URL:-jdbc:postgresql://127.0.0.1:5432/hellouniverse_db}"
export DB_USERNAME="${DB_USERNAME:-hellouniverse_db_user}"
export DB_PASSWORD="${DB_PASSWORD:-}"
# The scheduled NASA fetch is turned off below, so no real key is needed
export NASA_API_KEY=pgo-recording

./mvnw -B -ntp -Pnative -Dpgo.option=--pgo-instrument -DskipTests clean native:compile

port=18080
base="http://127.0.0.1:$port"
profile="$PWD/src/main/pgo/default.iprof"
# Same heap limit as the VPS service
./target/app -Xmx768m -XX:ProfilesDumpFile="$profile.new" \
  --server.port=$port --scheduler.fetch-apod.cron=- &
app=$!
trap 'kill $app 2>/dev/null || true' EXIT
until curl -sf "$base/actuator/health" >/dev/null; do
  # Stop instead of waiting forever if the app failed to start
  kill -0 $app 2>/dev/null || { echo "The app exited during startup; see its output above." >&2; exit 1; }
  sleep 0.2
done

# Traffic shaped like real use: mostly date ranges of different lengths across
# the archive, some health checks, and a few rejected requests
ranges=(
  2026-10-25:2026-10-31 2026-10-01:2026-10-31 2025-12-01:2025-12-31
  2024-01-01:2024-03-31 2024-07-04:2024-07-04 2020-06-01:2020-06-30
  2015-01-01:2015-04-10 2008-07-01:2008-07-07 2001-03-01:2001-03-31
  1996-01-01:1996-02-15 2030-01-01:2030-01-10
)
for range in "${ranges[@]}"; do
  hey -z 8s -c 10 "$base/apods?start_date=${range%%:*}&end_date=${range##*:}" | grep 'Requests/sec'
done
for query in 'start_date=2024-01-01&end_date=2024-06-01' 'start_date=2024-02-01&end_date=2024-01-01' \
  'start_date=2024-13-01&end_date=2024-01-01' 'start_date=2024-01-01'; do
  hey -n 300 -c 5 "$base/apods?$query" >/dev/null
done
hey -n 500 -c 5 "$base/actuator/health" >/dev/null

# The profile is written when the app shuts down normally
kill -TERM $app
wait $app || true
trap - EXIT
mv "$profile.new" "$profile"
echo "Recorded $profile ($(du -h "$profile" | cut -f1)). Commit it with your other changes."
