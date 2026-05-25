# Process & workflow

How the work is structured so a solo developer stays consistent: convention plugins that keep 25 modules uniform, plan files that survive across sessions, and CI that gates the cheap-but-important checks.

## 2026-04-01 — Three convention plugins drive 25 modules

**Problem.** 25 Gradle modules each hand-rolling their KMP + Compose + test + Kover setup is unmaintainable; a version bump means editing 25 files and drift is inevitable.

**Solution.** `build-logic/convention` defines three precompiled plugins — `nutrisport.kmp.library` (KMP + Compose + Mokkery + compose.uiTest + Robolectric + Kover), `nutrisport.kmp.feature` (library + Koin Compose), `nutrisport.kmp.feature.full` (feature + navigation + Ktor). Every module applies one of them; module `build.gradle.kts` files declare only their own dependencies.

**Measurement.** 3 plugins drive 25 modules. A toolchain or test-stack change is a 1-file edit in `build-logic` instead of 25.

**Status.** stable.

**Tradeoff.** Precompiled plugins can't host `compileSdk`/`minSdk` (AGP limitation), so those stay per-module. Convention-plugin changes invalidate the whole build cache.

**References.** [Architecture](../../.claude/rules/architecture.md) · [Conventions](../../.claude/rules/conventions.md)

## 2026-05-25 — Plan-mode + methodology routing for cross-session continuity

**Problem.** Multi-day work loses state between sessions; the agent re-derives the plan and re-makes the same decisions.

**Solution.** Non-trivial work is captured as numbered plan files in `.claude/features/` with explicit status markers, and the [`methodology.md`](../../.claude/rules/methodology.md) routing matrix decides when to brainstorm/plan/execute vs act directly. Repo artifacts (code, docs, commits) are written in English regardless of the working-conversation language.

**Measurement.** `qualitative only` — continuity is structural; no metric on re-derivation time saved.

**Status.** stable.

**Tradeoff.** Plan files go stale if not updated; the discipline depends on the developer (or `/infra-weekly`) keeping status markers current.

**References.** [Plan-mode rule](../../.claude/rules/plan-mode.md) · [Methodology](../../.claude/rules/methodology.md) · [CI](../CI.md)
