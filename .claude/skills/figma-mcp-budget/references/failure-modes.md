# Figma MCP failure modes

Three real failure modes seen in the wild + how the budget skill rules mitigate each.

## 1. Runaway loop on missing nodeId

**Scenario** (forum.figma.com case): agent has a stale or wrong nodeId. `get_metadata(nodeId="X")` returns 404. Agent decides "maybe wrong format" and retries:

```
get_metadata(nodeId="X")        → 404
get_metadata(nodeId="X-1")      → 404
get_metadata(nodeId="X:1")      → 404
get_metadata(nodeId="X.0")      → 404
get_metadata(nodeId="123:X")    → 404
... (continues silently)
```

Burns daily quota in 5 minutes. User sees nothing until next attempted Figma call returns 429 — by then it's too late.

**Root cause**: agent treats 404 as "input format issue" instead of "nodeId doesn't exist".

**Mitigation**: rule 6 (anti-runaway guard). On 2 consecutive errors of the same tool with the same args → STOP. Surface to user. Do not iterate.

Implementation cue: track `(tool, args)` tuples per session. On 2nd identical error → halt and report.

## 2. Truncation on page-level get_design_context

**Scenario**: agent calls `get_design_context(fileKey, nodeId="0:1")` (root page of a large file). Response is truncated mid-way; metadata only.

Agent then "tries again" with `get_metadata(nodeId="0:1")` (also times out) or starts walking children one by one — which is the right idea but takes 30+ calls.

**Root cause**: `get_design_context` is meant for frame-level (a screen, a component), not page-level. Page-level is what `get_metadata` is for.

**Mitigation**: rule 2 (`get_metadata` only as fallback). Use `get_design_context` on a specific frame nodeId (Content frame of a screen page, not page frame). For exploring a file structure, use the saved tree in `.claude/figma/SCREEN_FRAME_MAP.md` first; only fall back to `get_metadata` for pages not yet in the map.

## 3. Missing-nodeId-after-Figma-rename

**Scenario**: `.claude/figma/SCREEN_FRAME_MAP.md` has `Auth.LogIn` → Content nodeId `2037:10177`. Designer makes structural changes in Figma — frame replaced (not renamed). NodeId changes. Map is now stale. Next `get_design_context(nodeId="2037:10177")` returns 404.

**Why this is rare**: Figma usually preserves nodeId on rename and most edits. The cases where nodeId changes:

- Frame replaced wholesale (Cmd+V over the original)
- Imported from a different file
- Component instance detached and re-instanced

**Mitigation**:

- Scheduled `figma-map-sync` GitHub Action (planned in `.claude/features/02-figma-map-sync.md`) — weekly diff between SCREEN_FRAME_MAP and live Figma metadata. Open PR if drift detected.
- Manual: if `get_design_context(nodeId from map)` returns 404, surface to user immediately (rule 5). Don't retry or guess. Re-fetch via Figma URL paste, update the map row.

## Why these matter

Each failure mode has the same shape: **agent treats an error as a recoverable situation that needs more attempts**, when actually it needs the human to intervene. The discipline (rules 5, 6) is to fail fast and surface, not to retry.

## Related

- `.claude/skills/figma-mcp-budget/SKILL.md` — rules 5, 6, 7
- `.claude/skills/figma-mcp-budget/references/rate-limits.md` — what 200/day actually buys you
- `.claude/figma/SCREEN_FRAME_MAP.md` — first-line defense (no MCP call needed for known routes)
