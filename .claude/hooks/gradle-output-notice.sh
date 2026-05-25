#!/usr/bin/env bash
# Hook #5 — Gradle output notice (revised from "truncator")
#
# Trigger:  PostToolUse on Bash where command contains "gradlew" or "gradle "
# Behavior: If the tool output exceeds 200 lines, save the FULL output to
#           .claude/state/last-gradle-output.log and surface a NOTICE to Claude
#           via additionalContext with line count + log path + grep hint.
#           NOTE: PostToolUse hooks cannot replace tool_response, so the original
#           full output still appears in the transcript. The log file enables
#           targeted grep/Read-with-offset on follow-up turns.
# Bypass:   GRADLE_TRUNCATE_SKIP=1
#
# Exit 0 always.

set -euo pipefail

[[ "${GRADLE_TRUNCATE_SKIP:-0}" == "1" ]] && exit 0

payload=$(cat)
tool_name=$(printf '%s' "$payload" | python3 -c 'import json,sys
try:
    d = json.load(sys.stdin)
    print(d.get("tool_name", ""))
except Exception:
    pass' 2>/dev/null) || exit 0
[[ "$tool_name" != "Bash" ]] && exit 0

command=$(printf '%s' "$payload" | python3 -c 'import json,sys
try:
    d = json.load(sys.stdin)
    print(d.get("tool_input", {}).get("command", ""))
except Exception:
    pass' 2>/dev/null) || exit 0

# Match gradlew or "gradle " (with trailing space) — avoid matching gradle.kts
case "$command" in
  *gradlew*) ;;
  *"gradle "*) ;;
  *) exit 0 ;;
esac

# Extract output from tool_response — keys vary across Claude Code versions
output=$(printf '%s' "$payload" | python3 -c '
import json, sys
try:
    d = json.load(sys.stdin)
    tr = d.get("tool_response", {})
    if isinstance(tr, str):
        print(tr)
    else:
        for k in ("output", "stdout", "content", "result"):
            v = tr.get(k)
            if isinstance(v, str) and v:
                print(v)
                break
except Exception:
    pass
' 2>/dev/null) || exit 0

[[ -z "$output" ]] && exit 0

line_count=$(printf '%s\n' "$output" | wc -l | tr -d ' ')
[[ "$line_count" -le 200 ]] && exit 0

# Find project root for log path
proj_root=""
search_dir="$(pwd)"
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

log_file="$proj_root/.claude/state/last-gradle-output.log"
mkdir -p "$(dirname "$log_file")"
printf '%s\n' "$output" > "$log_file"

msg="[gradle-output-notice] Build output was $line_count lines (threshold: 200). Full output saved to .claude/state/last-gradle-output.log — use Read with offset/limit or grep to navigate without re-running the build. Common patterns: \`grep -n 'FAIL\\|error:\\|^>' .claude/state/last-gradle-output.log\` for failures."

printf '%s' "$msg" | python3 -c '
import json, sys
print(json.dumps({"hookSpecificOutput": {"hookEventName": "PostToolUse", "additionalContext": sys.stdin.read()}}))
'

exit 0
