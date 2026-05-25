#!/usr/bin/env bash
# Hook #2 — SKILL.md frontmatter validator
#
# Trigger:  PostToolUse on Edit | Write
# Filter:   tool_input.file_path matches .claude/skills/{name}/SKILL.md
# Behavior: Parse YAML frontmatter; warn (NEVER block) if:
#           - `name` field missing
#           - `description` field missing
#           - `description` < 50 chars (too vague for matrix routing per
#             .claude/rules/methodology.md)
# Bypass:   SKILL_FRONTMATTER_SKIP=1
#
# Output: JSON additionalContext with the specific problems found. Exit 0 always.

set -euo pipefail

[[ "${SKILL_FRONTMATTER_SKIP:-0}" == "1" ]] && exit 0

payload=$(cat)
file_path=$(printf '%s' "$payload" | python3 -c 'import json,sys
try:
    d = json.load(sys.stdin)
    print(d.get("tool_input", {}).get("file_path", ""))
except Exception:
    pass' 2>/dev/null) || exit 0
[[ -z "$file_path" ]] && exit 0

# Filter: must be .claude/skills/<name>/SKILL.md
case "$file_path" in
  */.claude/skills/*/SKILL.md) ;;
  *) exit 0 ;;
esac

[[ -f "$file_path" ]] || exit 0

name=$(awk '/^---[[:space:]]*$/{f++; next} f==1 && /^name:[[:space:]]*/{sub(/^name:[[:space:]]*/,""); print; exit}' "$file_path")
description=$(awk '/^---[[:space:]]*$/{f++; next} f==1 && /^description:[[:space:]]*/{sub(/^description:[[:space:]]*/,""); print; exit}' "$file_path")

problems=()
[[ -z "$name" ]] && problems+=("missing \`name\` field")
[[ -z "$description" ]] && problems+=("missing \`description\` field")
if [[ -n "$description" && ${#description} -lt 50 ]]; then
  problems+=("\`description\` is ${#description} chars (< 50 threshold). Too vague for the matrix routing in .claude/rules/methodology.md — add concrete trigger keywords")
fi

[[ ${#problems[@]} -eq 0 ]] && exit 0

joined=""
for p in "${problems[@]}"; do
  joined="${joined}- ${p}\n"
done

msg="[skill-frontmatter-check] $file_path frontmatter problems:\n${joined}"

printf '%s' "$msg" | python3 -c '
import json, sys
print(json.dumps({"hookSpecificOutput": {"hookEventName": "PostToolUse", "additionalContext": sys.stdin.read()}}))
'

exit 0
