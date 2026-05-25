#!/usr/bin/env bash
# Hook #4 — Auto-append memory pointer to MEMORY.md
#
# Trigger:  PostToolUse on Write
# Filter:   tool_input.file_path matches memory/(reference|project|feedback|user|ios)_*.md
# Behavior: Parse YAML frontmatter (name, description); append a markdown link
#           to MEMORY.md if no entry for this basename exists yet.
#           Description trimmed to 200 chars to keep MEMORY.md scannable.
# Bypass:   MEMORY_APPEND_SKIP=1
# Side:     Modifies MEMORY.md in the same dir as the new file (or parent if nested).
#
# Exit 0 always — never fail the parent tool. The Write itself succeeded.

set -euo pipefail

[[ "${MEMORY_APPEND_SKIP:-0}" == "1" ]] && exit 0

payload=$(cat)

file_path=$(printf '%s' "$payload" | python3 -c 'import json,sys
try:
    d = json.load(sys.stdin)
    print(d.get("tool_input", {}).get("file_path", ""))
except Exception:
    pass' 2>/dev/null) || exit 0
[[ -z "$file_path" ]] && exit 0

# Filter: must match memory/{prefix}_{slug}.md
case "$file_path" in
  */memory/reference_*.md|*/memory/project_*.md|*/memory/feedback_*.md|*/memory/user_*.md|*/memory/ios/*.md) ;;
  *) exit 0 ;;
esac

[[ -f "$file_path" ]] || exit 0

# Walk up to find MEMORY.md (handles ios/team.md → memory/MEMORY.md)
memory_file=""
search_dir=$(dirname "$file_path")
for _ in 1 2 3; do
  if [[ -f "$search_dir/MEMORY.md" ]]; then
    memory_file="$search_dir/MEMORY.md"
    break
  fi
  parent=$(dirname "$search_dir")
  [[ "$parent" == "$search_dir" ]] && break
  search_dir="$parent"
done
[[ -z "$memory_file" ]] && exit 0

memory_dir=$(dirname "$memory_file")
rel_path="${file_path#"$memory_dir"/}"

# Skip if line for this basename already exists in the index
if grep -qF "($rel_path)" "$memory_file"; then
  exit 0
fi

# Parse YAML frontmatter — extract name and description
name=$(awk '/^---[[:space:]]*$/{f++; next} f==1 && /^name:[[:space:]]*/{sub(/^name:[[:space:]]*/,""); print; exit}' "$file_path")
description=$(awk '/^---[[:space:]]*$/{f++; next} f==1 && /^description:[[:space:]]*/{sub(/^description:[[:space:]]*/,""); print; exit}' "$file_path")

# Hard rule: skip silently if either is missing — partial entries pollute the index
[[ -z "$name" ]] && exit 0
[[ -z "$description" ]] && exit 0

# Truncate description to 200 chars
if [[ ${#description} -gt 200 ]]; then
  description="${description:0:200}…"
fi

printf -- '- [%s](%s) — %s\n' "$name" "$rel_path" "$description" >> "$memory_file"

exit 0
