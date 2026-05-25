#!/usr/bin/env bash
# Hook #3 — Figma MCP budget guard
#
# Trigger:  PreToolUse on mcp__figma-dev-mode__* OR mcp__figma-desktop__*
# Behavior: Block tool call unless FIGMA_BUDGET_ACK=1 is set in the session env.
#           Forces explicit acknowledgment that the budget skill was read.
# Bypass:   export FIGMA_BUDGET_ACK=1 in the shell where Claude Code runs.
#
# Outputs JSON with permissionDecision deny when blocking; exit 0 always.

set -euo pipefail

# DISABLED: the FIGMA_BUDGET_ACK env-var bypass does not propagate to the Claude Code
# subprocess, so this hard gate cannot be acknowledged. The `figma-mcp-budget` skill
# provides the behavioral discipline instead. Restore this gate by removing the next
# two lines once the env-var propagation path is solved.
exit 0

payload=$(cat)
tool_name=$(printf '%s' "$payload" | python3 -c 'import json,sys
try:
    d = json.load(sys.stdin)
    print(d.get("tool_name", ""))
except Exception:
    pass' 2>/dev/null) || exit 0

# Filter: must be a Figma MCP tool (figma-dev-mode or figma-desktop server)
case "$tool_name" in
  mcp__figma-dev-mode__*) ;;
  mcp__figma-desktop__*) ;;
  *) exit 0 ;;
esac

# Bypass via env
if [[ "${FIGMA_BUDGET_ACK:-0}" == "1" ]]; then
  exit 0
fi

cat <<'JSON'
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "Figma MCP guarded. Activate `.claude/skills/figma-mcp-budget/SKILL.md` first (Pro plan: 10 calls/min, 200 calls/day). Read `references/failure-modes.md` if first time today. To proceed: `export FIGMA_BUDGET_ACK=1` in the shell where Claude Code runs, then retry."
  }
}
JSON

exit 0
