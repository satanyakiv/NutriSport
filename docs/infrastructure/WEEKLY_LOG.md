# Weekly log

Chronological journal of the Sunday infrastructure reviews. Each section is one week: what commits were scanned, which entries were added, and three reflection answers. Append-only.

## 2026-05-25 — Week 1 (bootstrap)

**Commits scanned.** This week's portfolio-tooling branch: rules expansion, fail-safe hooks, Sentry dual setup (Android), and a 9-skill port — `c73feb4`, `59c6f7d`, `bb609d9`, `5997274`, `b29e285`, `6e659f1`. Full repo history: 143 commits since 2025-11-20.

**Modified infra files.** `.claude/skills/` (+9 ported), `.claude/hooks/` (+figma-budget-guard, wired), `.claude/rules/` (14 total), `.claude/settings.json`, `.claude/figma/SCREEN_FRAME_MAP.md` (new stub).

**Entries added.**

- [wall-clock-economy](wall-clock-economy.md): Robolectric JVM suite (~30s, 0 emulator), Baseline Profiles harness.
- [ai-workforce](ai-workforce.md): 18 skills / 5 hooks / 14 rules surface.
- [token-economy](token-economy.md): cacheable rules surface, rtk allowlist.
- [reliability-and-safety](reliability-and-safety.md): fail-safe hooks, deterministic fake-data path.
- [process-and-workflow](process-and-workflow.md): 3 convention plugins → 25 modules, plan-mode continuity.
- [lessons-learned](lessons-learned.md): disabled figma-budget-guard; caught ComponentRegistrar R8 gap.

**Entries deferred.**

- Baseline Profile before→after startup numbers — needs a device run; re-evaluate once captured.
- rtk per-repo compression benchmark — not yet run on this repo.

**Reflection.**

1. _What worked well?_ Mirroring a proven infra-docs structure and seeding it with already-measured facts (test runtime, coverage, module/plugin counts) rather than inventing numbers.
2. _What did we try and drop? Why?_ Carrying over benchmark numbers from elsewhere — dropped, because portfolio entries must use this repo's own measurements; un-measured fields are marked `qualitative only`.
3. _What to try next week?_ Capture a real Baseline-Profile device run and a per-repo rtk benchmark to replace the two `qualitative only` placeholders.
