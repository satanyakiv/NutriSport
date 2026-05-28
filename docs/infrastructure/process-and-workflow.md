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

## 2026-05-26 — Plan-mode exploration caught a cross-platform deep-link bug before code

**Problem.** Porting a deep-link pattern across a different navigation architecture risks a silent mismatch — a resolver that compiles and unit-tests green but never matches the URLs the team actually fires.

**Solution.** Plan-mode ran three parallel `Explore` agents (source pattern, navigation/DI state, platform entrypoints) before any code. Exploration surfaced that the source's throwaway-host URL convention would not match NutriSport's natural `nutrisport://products/123` form, plus two topology conflicts (Cart is an inner `HomeNavHost` tab; Checkout needs a cart-derived amount). The registry was designed right on the first pass — scheme-aware host folding in the resolver, `/cart` and `/checkout` dropped. `codex review` gated each commit; the Android flow was verified live on the emulator (cold + warm).

**Measurement.** 3 parallel Explore agents; 3 design conflicts caught pre-code → 0 rework commits. 5 commits (`f48fee9`…`b158cf2`), 38 unit tests (34 module + 4 auth) green, 2 codex reviews (1 P2 fixed, 0 P1 after triage). Cold `nutrisport://categories/protein` → CategorySearch and warm `nutrisport://profile` → Profile confirmed on a Pixel emulator.

**Status.** stable.

**Tradeoff.** Plan-mode + parallel exploration + per-commit review add wall-clock before the first line of code — justified for a feature spanning `:domain`, `:network`, both platform entrypoints, and navigation; overkill for a one-file change. iOS was compile-verified but not simulator-run this session.

**References.** [Deep links](../DEEPLINKS.md) · [ADR-0007](../adr/0007-deeplink-architecture.md) · [Navigation rule](../../.claude/rules/navigation.md) · Commits `f48fee9`…`b158cf2` (local, unpushed)
