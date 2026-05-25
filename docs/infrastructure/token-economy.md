# Token economy

Keeping the agent's context window cheap: cache the fixed cost, filter the verbose, and bound the expensive (Figma MCP, screenshots). On a solo project the constraint is real — a runaway loop or an un-cached rules reload burns budget that buys nothing.

## 2026-05-25 — Keep the always-on rules surface prompt-cacheable

**Problem.** The `.claude/` rules surface (`CLAUDE.md` + 14 rule files) is loaded into every session. Re-reading it uncached on each turn is slow and expensive.

**Solution.** Authored the rules to stay inside Anthropic's `cache_control: ephemeral` window: no per-session timestamps, no conditional prose, stable file ordering (append new rules at the end, never insert mid-file). Ephemeral state lives in plan files (`.claude/features/`) and memory, which are outside the cached block. The discipline is documented in [`methodology.md`](../../.claude/rules/methodology.md) §"Prompt caching policy".

**Measurement.** `qualitative only` — the cache-hit rate is not instrumented on this repo. The mechanism (Anthropic ephemeral cache, 5-min TTL, ~10% cost on hits) is provided by the harness; the project's contribution is keeping the surface stable enough to hit it.

**Status.** stable (authoring discipline).

**Tradeoff.** Template/skeleton edits (renaming a rule section) are unavoidable cache-busters; they are batched to invalidate once rather than weekly.

**References.** [Methodology §caching](../../.claude/rules/methodology.md)

## 2026-05-25 — Filter verbose Bash output before it reaches context

**Problem.** A cached `./gradlew … compile` or a wide `find` dumps tens of KB of mostly-noise into the agent's context per call.

**Solution.** Documented an opt-in `rtk` allowlist in [`bash-output.md`](../../.claude/rules/bash-output.md) (`rtk err` for gradle compile, `rtk test` for tests, `rtk find` for enumeration) with an explicit exclusion list and a fallback-to-raw protocol. The agent calls `rtk` voluntarily based on the rule; no PreToolUse hook.

**Measurement.** `qualitative only — to be benchmarked on this repo`. The allowlist is documented but a per-repo byte/token compression measurement has not been run here.

**Status.** experimental — rule documented, benchmark pending.

**Tradeoff.** `rtk err` collapses a successful build to a one-liner, hiding KLIB/deprecation warnings. For active debugging, bypass rtk and run gradle raw (documented in the rule).

**References.** [Bash output rule](../../.claude/rules/bash-output.md) · [Media budget](../../.claude/rules/media-budget.md)
