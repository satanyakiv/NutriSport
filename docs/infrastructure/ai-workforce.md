# AI workforce

The Claude Code surface that lets one developer cover the work of a small team: skills (repeatable procedures), hooks (deterministic guardrails), and rules (always-on conventions) that keep the agent on-convention without re-explaining the project every session.

## 2026-05-25 — Skill + hook + rule surface as repeatable procedures

**Problem.** A solo developer re-explaining the same conventions (test pyramid, Route/Screen split, Figma handoff, R8 audit) to the agent every session wastes time and drifts. Ad-hoc prompting produces inconsistent results across sessions.

**Solution.** Codified the recurring work as a `.claude/` surface: 18 skills (feature pipeline, figma-handoff, compose-screen-splitter, r8-analyzer, claude-in-mobile, dev-jump, infra-weekly, firebase-ops, sentry-setup, kover-analyze, gen-test, …), 5 hook scripts, and 14 always-on rule files. A routing matrix in [`.claude/rules/methodology.md`](../../.claude/rules/methodology.md) §1 decides when each skill fires, overriding the default "invoke a skill before every reply" so the agent acts directly on mechanical work and only invokes process skills on real signals.

**Measurement.** Countable surface: 18 skills, 5 hooks, 14 rules. Impact on session quality is `qualitative only — to be measured` (no per-session time study yet).

**Status.** stable.

**Tradeoff.** The always-on rules + CLAUDE.md are loaded into every session's context (a fixed token cost, mitigated by prompt caching — see [token-economy.md](token-economy.md)). A large skill set also risks the agent invoking the wrong skill; the methodology matrix exists precisely to bound that.

**References.** [Methodology routing](../../.claude/rules/methodology.md) · [Skills dir](../../.claude/skills/) · [infra-weekly](../../.claude/skills/infra-weekly/SKILL.md)
