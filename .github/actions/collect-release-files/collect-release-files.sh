#!/usr/bin/env bash
set -euo pipefail
shopt -s nullglob
mkdir release-files
checksums='{}'
for source in target/app target/*.so*; do
  [[ -f "$source" && ! -L "$source" ]] || { echo "Not a regular file: $source" >&2; exit 1; }
  name=$(basename "$source")
  [[ "$name" =~ ^(app|[A-Za-z0-9_.+-]+\.so(\.[0-9.]+)?)$ ]] || { echo "Unexpected native file name: $name" >&2; exit 1; }
  install -m 750 "$source" "release-files/$name"
  checksum=$(sha256sum "release-files/$name" | cut -d ' ' -f 1)
  checksums=$(jq -cn --argjson checksums "$checksums" --arg name "$name" --arg hash "$checksum" '$checksums + {($name): $hash}')
done
# The infra playbook reads these field names; keep them unchanged.
jq -n --arg commit "$GITHUB_SHA" --arg version "$RELEASE_VERSION" \
  --arg architecture "$(uname -m)" --arg run_id "$GITHUB_RUN_ID" \
  --arg run_attempt "$GITHUB_RUN_ATTEMPT" --argjson files "$checksums" \
  '{commit: $commit, version: $version, architecture: $architecture,
    run_id: $run_id, run_attempt: $run_attempt, files: $files, sha256: $files.app}' \
  > release-files/manifest.json
