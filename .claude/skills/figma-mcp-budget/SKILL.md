---
name: figma-mcp-budget
description: Discipline rules для Figma MCP на Pro-плані ($20: 10 calls/min, 200 calls/day). Prefer get_design_context, scope by nodeId, anti-runaway guard, no-retry on 429, behavioral session cache, per-screen interleaving (Rule 11). Activate before any series of Figma MCP calls — особливо bootstrap-сценарії, feature-pipeline runs з ≥5 screens, або довгі handoff-сесії. Triggers: "Figma MCP", "rate limit", "get_design_context", "get_metadata", "node-id", "figma file key", "Figma бюджет", "429", any sequence of mcp__figma-dev-mode__* calls.
---

# Figma MCP Budget — discipline rules

## Context

Figma Pro plan ($20/month) caps: **10 calls/min** (rolling 60s) and **200 calls/day** (UTC rollover). Personal-account caps; team plan does not help. One runaway loop on a missing nodeId burns the daily budget in 5 minutes — see [`references/failure-modes.md`](references/failure-modes.md). This skill is orthogonal to `figma-handoff`: handoff defines WHAT to call, this defines HOW to call safely.

## When to activate

- Before any series of ≥3 Figma MCP calls in a turn
- Before bootstrap operations (e.g. enumerating pages of a Figma file)
- At the start of `feature` Phase A.1 (regardless of `screens[]` count)
- When user says "Figma бюджет вичерпано" / "rate limit hit" / "429"
- Whenever you load `figma-handoff` (it cross-references this skill in Step 0)

## Rules (core, inline)

### 1. Prefer `get_design_context` for scope-bound nodes

`get_design_context(fileKey, nodeId)` returns code + screenshot + metadata in one call — cheaper than separate `get_screenshot` + `get_metadata` + `get_variable_defs` chain. Use when you have a specific nodeId.

### 2. `get_metadata` only as fallback

Use `get_metadata` ONLY when (a) `get_design_context` returned a truncation error and you need to drill into children, or (b) you're enumerating a known file as part of a one-time bootstrap (with hard cap, see [`references/extended-rules.md`](references/extended-rules.md) §10). Don't use it for exploration ("let me see what's in this page") — it times out on large pages and is wasteful when `get_design_context` would do.

### 3. Always pass `nodeId`

Never call `get_design_context(fileKey)` or `get_metadata(fileKey)` without a `nodeId`. Only exception: bootstrap pass against per-page nodeIds collected from a saved tree (e.g. `.claude/figma/SCREEN_FRAME_MAP.md` Phase 1 dump).

### 4. `whoami` once per session

`whoami` is **exempt from rate limit** — call it once per session for seat-type verification (Pro vs Org) and to confirm authenticated user. Do not re-call within the same conversation.

### 5. No-retry on 429

On `"rate limit exceeded"` / 429 response: STOP. Do not retry, even with backoff. Surface to user honestly:

> "Figma MCP rate limit hit. Daily budget likely exhausted. Pausing this task. Resume after UTC midnight rollover (in N hours) or upgrade to Org tier."

Retrying burns more cycles trying — this is what wrecks daily budgets in seconds.

### 6. Anti-runaway guard

On 2 consecutive errors of the same MCP-tool with the same args (e.g. `get_metadata(nodeId="X")` errored twice in a row): STOP. Surface to user. Do not iterate. This catches the most common failure mode: agent decides nodeId is "wrong format" and retries with slight variations (`X-1`, `X:1`, `X.0`), burning the budget.

## Extended rules (load on demand)

For multi-screen workflows, bootstrap enumerations, Code Connect questions, and behavioral session cache discipline → [`references/extended-rules.md`](references/extended-rules.md) covers Rules 7–11:

- **Rule 7** — Session cache discipline (behavioral; don't re-call already-fetched nodeIds)
- **Rule 8** — Code Connect tools are NOT available on Pro tier
- **Rule 9** — No native batching; savings come from rules 1+7
- **Rule 10** — Bootstrap budget cap (80 calls hard limit, 40% of daily)
- **Rule 11** — Per-screen interleaving for the `feature` pipeline

Read it whenever a `feature` Phase B run is about to start or whenever ≥3 frames will be touched in one turn.

## Anti-patterns

- ❌ "Let me check what's in this page" → `get_metadata(page nodeId)` for exploration
- ❌ Retry-on-error loop after 429 ("maybe it's a transient blip")
- ❌ Re-calling `get_design_context` on a nodeId already fetched this session
- ❌ Calling Code Connect tools on Pro tier
- ❌ Calling `whoami` more than once per session
- ❌ Not informing user when budget is exhausted
- ❌ Passing `fileKey` without `nodeId` outside the bootstrap pass
- ❌ Bulk-prefetch all N screens of a feature upfront — violates Rule 11; the single largest cause of 429 in production runs

## Related

- [`references/extended-rules.md`](references/extended-rules.md) — Rules 7–11 (load on demand)
- [`references/rate-limits.md`](references/rate-limits.md) — full table by plan
- [`references/failure-modes.md`](references/failure-modes.md) — 3 real failure cases with mitigations
- [`.claude/skills/figma-handoff/SKILL.md`](../figma-handoff/SKILL.md) — what to call (Step 1.5 lookup, 13-step handoff protocol). Step 0 activates this skill.
- [`.claude/skills/feature/SKILL.md`](../feature/SKILL.md) — Phase A.1 budget pre-check
- [`.claude/figma/SCREEN_FRAME_MAP.md`](../../figma/SCREEN_FRAME_MAP.md) — pre-resolved Screen ↔ nodeId map (zero MCP calls for known routes)
