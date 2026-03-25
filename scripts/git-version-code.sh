#!/bin/sh
# Derives versionCode from total commit count (monotonically increasing).
# Fallback: 1 when git is unavailable.
count=$(git rev-list --count HEAD 2>/dev/null) || true
if [ -n "$count" ]; then
  echo "$count"
else
  echo "1"
fi
