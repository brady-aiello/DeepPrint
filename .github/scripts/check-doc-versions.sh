#!/usr/bin/env bash
# The README tells people which version to depend on, so it goes stale on every release
# unless something says so. It stayed on 0.5.0 through a release before this existed.
set -euo pipefail

version=$(grep '^version=' gradle.properties | cut -d= -f2)

found=$(
  {
    grep -ohE 'com\.bradyaiello\.deepprint:[a-z-]+:[0-9]+\.[0-9]+\.[0-9]+' README.md |
      grep -oE '[0-9]+\.[0-9]+\.[0-9]+$'
    grep -ohE 'id\("com\.bradyaiello\.deepprint"\) version "[0-9]+\.[0-9]+\.[0-9]+"' README.md |
      grep -oE '[0-9]+\.[0-9]+\.[0-9]+'
  } | sort -u
)

if [ -z "$found" ]; then
  echo "No DeepPrint coordinates found in README.md. Has the format changed?" >&2
  exit 1
fi

if [ "$found" != "$version" ]; then
  echo "README.md advertises a version other than gradle.properties' $version:" >&2
  printf '  %s\n' $found >&2
  echo "Update the coordinates in README.md, or the version in gradle.properties." >&2
  exit 1
fi

echo "README.md matches gradle.properties: $version"
