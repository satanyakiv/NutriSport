#!/bin/sh
# Derives versionName from the latest git tag.
# Tag format: v-{version} or v{version} → strips prefix.
# Fallback: "1.0-dev" when no tags exist.
tag=$(git describe --tags --abbrev=0 2>/dev/null) || true
if [ -n "$tag" ]; then
  echo "$tag" | sed 's/^v-//;s/^v//'
else
  echo "1.0-dev"
fi
