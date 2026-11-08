#!/usr/bin/env bash
set -euo pipefail

gh api "repos/$GITHUB_REPOSITORY/contents/pom.xml?ref=main" \
  -H 'Accept: application/vnd.github.raw+json' > "$RUNNER_TEMP/main-pom.xml"
main_version=$(python3 .github/scripts/detect-release.py --print-version "$RUNNER_TEMP/main-pom.xml")
if [[ "$main_version" != "$VERSION" ]]; then
  echo "Skipped v$VERSION: main now asks for $main_version." >> "$GITHUB_STEP_SUMMARY"
  exit 0
fi
tag="v$VERSION"
refs=$(gh api "repos/$GITHUB_REPOSITORY/git/matching-refs/tags/$tag")
if jq -e --arg ref "refs/tags/$tag" 'any(.[]; .ref == $ref)' <<< "$refs" > /dev/null; then
  tagged_sha=$(gh api "repos/$GITHUB_REPOSITORY/commits/$tag" --jq .sha)
  [[ "$tagged_sha" == "$GITHUB_SHA" ]] || { echo "$tag belongs to another commit." >&2; exit 1; }
fi
published=$(gh api --paginate "repos/$GITHUB_REPOSITORY/releases?per_page=100" |
  jq -r --arg tag "$tag" '.[] | select(.tag_name == $tag and .draft == false) | .tag_name')
if [[ -n "$published" ]]; then
  echo "Skipped $tag: it is already published." >> "$GITHUB_STEP_SUMMARY"
else
  echo 'needed=true' >> "$GITHUB_OUTPUT"
fi
