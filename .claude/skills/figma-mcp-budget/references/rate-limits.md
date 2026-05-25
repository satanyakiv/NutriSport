# Figma MCP rate limits by plan

Caps observed for the Figma Dev Mode MCP server (`mcp__figma-dev-mode__*`). These are **personal-account caps**, not team-account caps — upgrading the team plan alone does not lift them.

| Plan       | Cost   | Per-min          | Daily          | Notes                                      |
| ---------- | ------ | ---------------- | -------------- | ------------------------------------------ |
| Free       | $0     | exempt-tier only | very low (~50) | Effectively `whoami` only                  |
| Pro        | $20/mo | 10               | 200            | Поточний наш план                          |
| Org        | $45/mo | 30               | 1000           | Upgrade target if budget exhausted >2/week |
| Enterprise | custom | custom           | custom         | Out of scope for v1                        |

`whoami` is exempt from rate limit at all tiers. Use once per session for seat verification.

## Reset cadence

- Per-minute: rolling 60-second window. Hitting 10 calls in 30 seconds → next call 30 seconds later, not after a minute from the first call.
- Daily: hard rollover at **UTC midnight** (00:00 UTC). Plan bootstrap runs in early UTC hours so a debug retry has full daily budget available.

## When to upgrade

If we hit the daily limit **>2 times per week**, three signals dominate:

1. Bootstrap operations exceeding 100+ calls (e.g. enumerating large Figma files)
2. Multi-feature `feature` pipeline runs (≥10 screens) routinely
3. Manual handoff sessions on >5 frames each, multiple times per week

In that case, request Org upgrade ($45/mo): 5× daily cap, 3× per-minute. Org tier also unlocks Code Connect tooling (`add_code_connect_map`, etc.).

## Pro tier in numbers

What 200 calls/day affords (rough budget):

| Operation                                                                        | Calls      | Per-day capacity                     |
| -------------------------------------------------------------------------------- | ---------- | ------------------------------------ |
| Single screen handoff (`get_design_context` + `get_variable_defs` + dark verify) | ~3–5       | ~40–60 screens/day                   |
| Bootstrap (page-level `get_metadata`)                                            | 1 per page | ~200 pages/day if doing nothing else |
| Feature pipeline (5 screens × 5 calls + buffer)                                  | ~25–30     | ~6–8 features/day                    |

A single autonomous loop on a missing nodeId can consume 100+ calls in minutes — see `failure-modes.md`.

## Sources

- Figma MCP server changelog (private to authenticated users)
- forum.figma.com discussions (anecdotal)
- Anthropic plugin documentation: `claude-plugins-official` repo, `figma/mcp-server-guide` skill
