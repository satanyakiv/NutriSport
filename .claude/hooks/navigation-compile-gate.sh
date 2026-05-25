#!/usr/bin/env bash
# Navigation compile gate
#
# Trigger:  PostToolUse on Edit | Write | MultiEdit
# Filter:   tool_input.file_path basename matches one of:
#           Screen.kt | NavGraph.kt | Router.kt | DefaultRouter.kt
#           NavigationCommand.kt | FakeRouter.kt | AppContent.kt
# Behavior: Spawn `./gradlew :navigation:compileCommonMainKotlinMetadata
#           :shared:utils:compileCommonMainKotlinMetadata --quiet` in BACKGROUND.
#           Write result to .claude/state/last-nav-compile.txt.
#           If the PREVIOUS run failed, surface a hint to Claude via additionalContext.
# Bypass:   NAV_COMPILE_GATE_SKIP=1
# Hard rule: Never blocks. Returns immediately so Claude isn't blocked waiting.

set -euo pipefail

[[ "${NAV_COMPILE_GATE_SKIP:-0}" == "1" ]] && exit 0

payload=$(cat)
file_path=$(printf '%s' "$payload" | python3 -c 'import json,sys
try:
    d = json.load(sys.stdin)
    print(d.get("tool_input", {}).get("file_path", ""))
except Exception:
    pass' 2>/dev/null) || exit 0
[[ -z "$file_path" ]] && exit 0

base=$(basename "$file_path")
case "$base" in
  Screen.kt|NavGraph.kt|Router.kt|DefaultRouter.kt|NavigationCommand.kt|FakeRouter.kt|AppContent.kt) ;;
  *) exit 0 ;;
esac

# Walk up to find gradlew
proj_root=""
search_dir=$(dirname "$file_path")
for _ in $(seq 1 12); do
  if [[ -x "$search_dir/gradlew" ]]; then
    proj_root="$search_dir"
    break
  fi
  parent=$(dirname "$search_dir")
  [[ "$parent" == "$search_dir" ]] && break
  search_dir="$parent"
done
[[ -z "$proj_root" ]] && exit 0

state_file="$proj_root/.claude/state/last-nav-compile.txt"
mkdir -p "$(dirname "$state_file")"

prev_status=""
if [[ -f "$state_file" ]]; then
  prev_status=$(head -1 "$state_file" 2>/dev/null || true)
fi

# Spawn background compile (detached)
(
  cd "$proj_root"
  output=$(./gradlew :navigation:compileCommonMainKotlinMetadata :shared:utils:compileCommonMainKotlinMetadata --quiet 2>&1) && exit_code=0 || exit_code=$?
  {
    if [[ $exit_code -eq 0 ]]; then
      echo "OK $(date +%Y-%m-%dT%H:%M:%S)"
    else
      echo "FAIL $(date +%Y-%m-%dT%H:%M:%S) exit=$exit_code"
      echo "---"
      printf '%s\n' "$output" | tail -30
    fi
  } > "$state_file"
) >/dev/null 2>&1 &
disown 2>/dev/null || true

# Surface PREVIOUS failure (the new compile is still running)
if [[ "$prev_status" == FAIL* ]]; then
  failure_excerpt=$(tail -20 "$state_file" 2>/dev/null || true)
  printf '%s' "$failure_excerpt" | python3 -c '
import json, sys
excerpt = sys.stdin.read().strip()
msg = "[nav-compile-gate] Previous navigation compile FAILED. New compile is running in background after this edit. Last failure tail:\n" + excerpt
print(json.dumps({"hookSpecificOutput": {"hookEventName": "PostToolUse", "additionalContext": msg}}))
' 2>/dev/null || true
fi

exit 0
