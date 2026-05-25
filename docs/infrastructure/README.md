# NutriSport — Infrastructure Portfolio

Solo developer (Yakiv Bondar) building a Kotlin Multiplatform + Compose Multiplatform e-commerce app (Android + iOS) as a portfolio piece. 25 Gradle modules, Clean Architecture, offline-first Room SSOT, dual crash observability. Heavy AI-tooling investment (Claude Code skills, MCP servers, fail-safe hooks) to move fast as a one-person team. This directory documents the infrastructure decisions behind that — every entry carries measured impact, not just "we did X".

## Topics

| Topic                                             | What lives here                                                                           | Cumulative impact (so far)                                            |
| ------------------------------------------------- | ----------------------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| [AI workforce](ai-workforce.md)                   | Claude Code skills (18), MCP servers, hooks (5), rules (14), plan-mode workflow           | qualitative — countable surface, impact to be measured                |
| [Token economy](token-economy.md)                 | Prompt-cacheable rules surface, rtk output filter, Figma MCP discipline, image budget     | qualitative — discipline documented, not yet benchmarked on this repo |
| [Wall-clock economy](wall-clock-economy.md)       | Robolectric (no emulator), Baseline Profiles, gradle daemon                               | full suite ~30s on JVM, 0 emulator boots                              |
| [Reliability & safety](reliability-and-safety.md) | Fail-safe hooks, sensitive-file block, fake-data discipline, ComponentRegistrar keep rule | qualitative — fail-safe by construction                               |
| [Process & workflow](process-and-workflow.md)     | Plan-mode discipline, convention plugins, CI pipelines, English-only artifacts            | 3 convention plugins drive 25 modules                                 |
| [Lessons learned](lessons-learned.md)             | Anti-patterns, failed experiments, things that did NOT work                               | (anti-patterns)                                                       |

## Cadence

**Sunday evening, ~15 minutes.** Run `/infra-weekly` (project skill at [`.claude/skills/infra-weekly/SKILL.md`](../../.claude/skills/infra-weekly/SKILL.md)) — Claude scans the last 7 days of commits + modified infra files (`.claude/{hooks,skills,rules,settings*}`, `gradle/libs.versions.toml`, `build-logic/`), drafts entries against [`templates/entry.md`](templates/entry.md), and updates [`WEEKLY_LOG.md`](WEEKLY_LOG.md). Developer work: confirm + edit drafts.

Real-time capture: [`.claude/rules/methodology.md`](../../.claude/rules/methodology.md) §1 has a row that fires on infra-file edits — Claude proactively asks "document this?" so changes don't slip past the weekly review.

## How to add an entry

1. Pick the right topic file from the table above. When in doubt, prefer a more specific topic over `lessons-learned.md`.
2. Copy the skeleton from [`templates/entry.md`](templates/entry.md), paste as a new H2 section into the topic file.
3. Fill in **Problem · Solution · Measurement · Status · Tradeoff · References**.
4. Mention in the next weekly review (or run `/infra-weekly` immediately and let it pick up the new entry).

## Why this exists

Without this directory:

- Six months from now the motivation behind a specific hook or rule is gone — only the git log remains, and it does not capture "why".
- For a portfolio interview ("how did you ship a KMP app solo?") narratives get reconstructed from memory under pressure. Pre-write them once, polish over weekly reviews.
- Each new infra experiment risks replaying old mistakes that were never formally recorded.

## Related

- [`docs/PERFORMANCE.md`](../PERFORMANCE.md) — quantitative benchmark log; portfolio entries link back to it for numbers
- [`docs/TESTING.md`](../TESTING.md) · [`docs/CI.md`](../CI.md) — measured test + CI facts
- [`.claude/rules/methodology.md`](../../.claude/rules/methodology.md) — skill-routing matrix (drives the infra-weekly trigger)
- [`.claude/skills/infra-weekly/SKILL.md`](../../.claude/skills/infra-weekly/SKILL.md) — weekly review automation
