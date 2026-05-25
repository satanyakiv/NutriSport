# Figma MCP Budget — Extended Rules (7–11)

Behavioral rules that complement the 6 core rules in [`SKILL.md`](../SKILL.md). Kept here so the inline payload of the skill stays small; load this file when you need the full discipline (multi-screen pipelines, bootstrap passes, Code Connect questions).

## 7. Session cache discipline (behavioral)

Within one conversation, if `get_design_context(fileKey="A", nodeId="B")` was already called and returned content — **reference the previous result** from your context window. Don't re-call.

This is NOT a server-side cache — it's a behavioral rule. The MCP server does not deduplicate.

For drawer/state variants of the same parent screen, fetch the parent once, then fetch only the variant nodeId (the parent context already covers shared layout).

## 8. Code Connect — not on Pro

Tools `add_code_connect_map`, `get_code_connect_map`, `send_code_connect_mappings`, `get_code_connect_suggestions`, `get_context_for_code_connect` are NOT available on Pro tier. Do not call them. They will error or return empty.

## 9. Batching — no native endpoint

Figma MCP does not have batch endpoints. Multiple `get_design_context` calls cost per-call regardless of how they are issued. Savings come from rules 1+7 (scope + session cache), not from batching.

## 10. Bootstrap budget cap

For bootstrap-style enumeration (per-page metadata across a whole file): hard cap **80 calls** (40% of 200 daily). Append-only persist after every 5–10 pages (resume-friendly). Run in the early UTC day to leave buffer for debug.

## 11. Per-screen interleaving (feature pipeline rule)

For multi-screen workflows (the [`feature`](../../feature/SKILL.md) pipeline, manual handoffs of ≥3 frames at once, any "do all screens at once" request):

- **NEVER** fire >3 Figma MCP calls in a row without an implementation block between them. Implementation = ≥1 written/edited Kotlin file OR a Gradle build run OR a non-trivial code review.
- **NEVER** bulk-prefetch (`get_design_context` for ALL N screens upfront). One screen = one full cycle (validate → context → vars → impl → verify) before moving to the next.
- The 5-15 min implementation gap between screens is the rate-limit guarantee — without it, even 4 screens × 4 calls each = 16 calls in <60s = guaranteed 429.
- Validate one nodeId upfront (sanity check); validate the rest lazily as each screen's cycle starts.
- Reuse `get_design_context` PNG response across the per-screen `get_screenshot` verify step when conversation context still holds it — saves 1 call per screen.

This rule is enforced in [`feature`](../../feature/SKILL.md) Phase B Step 3 (per-screen mini-loop) and [`figma-handoff`](../../figma-handoff/SKILL.md) Step 13 (visual verify with PNG cache to disk).

## Related

- [`SKILL.md`](../SKILL.md) — core 6 rules + anti-patterns
- [`failure-modes.md`](failure-modes.md) — 3 real failure cases with mitigations
- [`rate-limits.md`](rate-limits.md) — full table by plan
