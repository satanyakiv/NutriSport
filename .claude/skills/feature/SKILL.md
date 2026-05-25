---
name: feature
description: End-to-end feature pipeline for NutriSport. Use when the user types "/feature" or the legacy "/overnight-feature" alias, asks to "build feature autonomously", "spin up the auth/cart/profile flow", or hands you a Figma URL with a feature description. The skill runs an interactive plan-mode phase (clarifications → frontmatter plan), then a long-running per-screen interleaved phase (worktree → domain+network skeleton if missing → for each screen: Figma handoff + scaffold + visual preview-verify → NavGraph wiring → unit + composition tests → E2E backlog append → emulator screenshots → end-to-end visual fidelity check → codex review → console report). Manual invocation only — the user runs this when they have time and a fresh context budget. All status reports (gates, attention required, QA checklist, file lists) go to the chat console — no REPORT.md / QA_CHECKLIST.md / dated audit files / email drafts are created. Instrumented E2E tests are NOT executed — cases are appended to docs/E2E_BACKLOG.md (created by the testing/CI track) until the infra activation gate is met.
---

# feature — long-running feature pipeline

Composes [`new-feature`](../new-feature/SKILL.md), [`figma-handoff`](../figma-handoff/SKILL.md), [`figma-mcp-budget`](../figma-mcp-budget/SKILL.md), [`gen-test`](../gen-test/SKILL.md), `superpowers:using-git-worktrees`, `/codex:review`, and [`claude-in-mobile`](../claude-in-mobile/SKILL.md) (CLI) into a single long-running pipeline. One run = one feature flow (all screens of an `auth`, `cart`, `profile`, …).

**Per-screen interleaved cycle.** Phase B does NOT batch all Figma calls upfront. Instead, for each screen it runs a tight loop: fetch design context → scaffold the screen → visually compare `@Preview` against Figma → fix drift → next screen. This naturally spreads Figma MCP calls across implementation time (5-15 min between calls per screen), eliminating Pro-plan rate-limit bursts (10 calls/min, 200 calls/day). After all screens build and install, a final end-to-end visual fidelity check compares emulator screenshots against the cached Figma frames (zero additional Figma MCP calls).

This skill is the executable contract.

**Manual invocation only.** This skill is launched by the user explicitly — there is no scheduler, no cron, no autorun. "Long-running" means real wall-clock minutes burning real tokens in the foreground; the user controls when to start, when to checkpoint, and when to resume.

## When to use

Activate when the user:

- Types `/feature` (preferred) or the legacy alias `/overnight-feature`, or asks to "queue a feature pipeline"
- Hands you a feature description + Figma URL and expects a complete branch from a single batched build
- Says "build the auth flow autonomously", "spin up cart end-to-end", "do profile as one pass"

Do NOT use for: single-screen tweaks (use [`new-feature`](../new-feature/SKILL.md)), pure refactors, or anything that touches a real `:network` data source (the pipeline runs against `FakeXxxDataSource` only — see Group B in [`.claude/features/00-orchestrator.md`](../../features/00-orchestrator.md)).

## Two phases

```
Phase A — interactive (you at the keyboard, ~5–15 min)
  └─ clarifications, plan written to .claude/plans/feature-<name>.md
  └─ ExitPlanMode

Phase B — batched scaffold (long-running, context-heavy)
  └─ worktree → domain+network skeleton (if missing) → Figma handoff
  → ViewModels → NavGraph → unit + composition tests → E2E backlog
  → screenshots → review → console report
```

### Runtime expectations

Phase B is **long-running and context-heavy**. A typical run produces 50–80 files (domain models + use cases + DTOs + DataSources + Mapper + RepositoryImpl + 5-piece feature skeleton per frame + components + tests) and triggers 5–25 Figma MCP calls plus multiple Gradle invocations. **Plan accordingly:**

- **Fresh context budget.** Start Phase B in a session with at least 500K tokens free. A session already loaded with planning chatter, prior conversations, or unrelated work will exhaust before completion. If unsure, `/clear` first.
- **Split execution.** When scope is large (≥4 frames OR domain layer needs creation), checkpoint every ~7–10 steps. Each checkpoint commits to the worktree branch and prints a continuation block to chat, then hands off to a fresh session via the **continuation-prompt protocol** in [`references/continuation-prompt.md`](references/continuation-prompt.md).
- **Foreground inline runtime.** The pipeline runs in the same conversation that approves the plan — there is no scheduler, no background worker, no cron. "Long-running" means real wall-clock minutes that consume real tokens. If the user wants a quick win, this is the wrong skill — push back.
- **Expected halt points.** Phase A clarifications (interactive `AskUserQuestion`), Phase B at any hard-fail gate, or at a self-declared checkpoint when context budget nears 50% used.

### Continuation handoff

When splitting across sessions, the closing turn of a partial session prints all state directly to the chat console — no REPORT.md or other state files are created. The console message contains:

1. **What landed** — commit hash + file list, exactly as Phase B produced them so far.
2. **What's pending** — continuation backlog grouped by skill step.
3. **A self-contained continuation prompt** built from the template in [`references/continuation-prompt.md`](references/continuation-prompt.md), with the "what landed" / "what's pending" content inlined. The user copies this whole block into a fresh session as the first prompt; the next session resumes from the named pending step with zero file-based state lookup.

The closing turn ALSO commits to the worktree branch (no push without explicit user authorization, per project safety rule). The commit message embeds the gates summary, so `git log` is the durable audit trail in lieu of a dated report file.

The continuation session does NOT re-run Phase A; it picks up Phase B from the named pending step.

## Phase A — interactive plan-mode

Goal: produce a frontmatter-rich plan at `.claude/plans/feature-<name>.md` with `status: approved`, ready for the autonomous phase.

### A.1 Pre-flight (fail fast)

- Check `git status` is clean on `main`. If not, ask: stash, commit, or abort.
- **Activate [`figma-mcp-budget`](../figma-mcp-budget/SKILL.md) rules** before any Figma MCP call (rate-limit, no-retry, anti-runaway, session cache discipline).
- Verify referenced use cases exist in `:domain` (search `domain/.../usecase/`). If any are missing, the pipeline scaffolds the full vertical slice: domain models + Repository interface + use cases + matching `:network` DTOs + `Fake*DataSource` (success-only per `.claude/rules/fake-data.md`) + `Remote*DataSource` stub + Mapper + RepositoryImpl + Koin module + Endpoints. Scope expands silently from "feature module only" to "full vertical slice" — record this expansion in the plan's `## Context` section so the user knows the run is bigger than feature-only and so the resulting commit message is accurate.
- **Resolve Figma frames.** Brief frontmatter accepts `figmaFrames: [{ name, nodeId }]` (one entry per screen). For every entry, normalize the `nodeId` to `"X:Y"` form (URL `?node-id=X-Y` → dash to colon) so the rest of the pipeline (steps 4 / 8 / 14) is unchanged.
- **Budget pre-check (per-screen interleaving).** Estimate `frames.size × 3` (design_context + verify + lazy validate). With per-screen interleaving (Phase B Step 4 mini-loop), 5-15 min between Figma calls eliminates burst risk. If `frames.size × 3 > 100` (50% of daily 200), `AskUserQuestion`:
  > "Brief contains N frames × 3 ≈ M MCP calls (X% денного бюджету). Розбити на 2 запуски або продовжити?"
- **Sanity-check ONE nodeId only** (NOT all). Call `mcp__figma-dev-mode__get_screenshot(fileKey, frames[0].nodeId)`. If it fails — bail with a clear error (file key wrong, MCP server down, auth lost). If it succeeds — proceed; the remaining nodeIds are validated lazily in Phase B Step 4a as each screen's cycle starts. Rationale: bursting N validation screenshots upfront is the prime cause of 10-calls/min 429s. We only verify the file/MCP layer is alive.

### A.2 Read context (every time, no caching)

- `:domain/.../usecase/` — confirm available use cases
- `:network/.../FakeXxxDataSource` — confirm fakes exist for the repository
- `:shared:utils/.../navigation/Screen.kt` — confirm route names don't already exist
- `:navigation/.../NavGraph.kt` — confirm the `composable<Screen.X>` wiring pattern and the navigation-callback shape used by existing Routes
- `:shared:ui/.../Colors.kt`, `Fonts.kt`, `component/` — palette, typography, components
- [`.claude/rules/conventions.md`](../../rules/conventions.md) — Route/Screen separation contract

### A.3 Figma sweep — REMOVED (per-screen interleaving)

The bulk Figma sweep that previously ran here has been **folded into the per-screen mini-loop in Phase B Step 4**. Each screen pulls its own `get_design_context` + `get_screenshot` immediately before its scaffold step, separated by 5-15 minutes of implementation work from the previous screen's calls. This eliminates the 10-calls/min burst that triggered Pro-plan 429s.

Component / token decisions (was: "if frame references missing `:shared:ui` component, ask the user to extract"; "flag inline styles as candidate tokens") are now made **per-screen during Step 4d implementation**, not upfront. Console-log unresolved component questions to chat as they arise; don't block the whole pipeline at plan time.

### A.4 Scenarios — propose, then confirm

Write three categories per frame:

- **positive** — happy paths logged to [`docs/E2E_BACKLOG.md`](../../../docs/E2E_BACKLOG.md) (created by the testing/CI track) and verified manually via the QA checklist until the E2E activation gate is met
- **negative** — error paths the UI must show clearly (inline errors, ErrorCard, OfflineBanner); covered today by composition tests, mirrored in the backlog
- **neutral** — slow network, rotation, back-button (composition tests only)

User confirms or edits. The `flow.positive` and `flow.negative` arrays are appended to `docs/E2E_BACKLOG.md` at step 11 (one bullet per scenario, prospective `snake_case` test method name + one-line description).

### A.5 Proactive thin-spot questions

The pipeline raises these via `AskUserQuestion` BEFORE writing the plan:

- "Empty state isn't drawn for `<frame>` — fall back to generic `ErrorCard`?"
- "After successful sign-in: navigate to `Screen.HomeGraph` with `popUpTo<Screen.Auth> { inclusive = true }`?"
- "Is this destination top-level or a child of an existing graph?"
- "Social login (Apple, Google) — in scope this run? If not, document as `Not Covered`."

Pipeline does not silently invent answers. Skipped answers default to "flag in the chat `## Attention required` block at Step 15" — never to "guess".

### A.6 Write the plan

`.claude/plans/feature-<name>.md` — frontmatter spec in [`references/plan-template.md`](references/plan-template.md). Set `status: planning` while drafting; flip to `status: approved` after `ExitPlanMode`.

### A.7 ExitPlanMode → Phase B

User reviews the plan in their editor. Approves with `Enter` (or edits and re-approves). On approval, Phase B begins in the same conversation. The user is responsible for ensuring the session has fresh context budget per the runtime expectations above; if they're already deep into another conversation, push back and suggest `/clear` first.

## Phase B — batched scaffold (long-running)

Each step prints its outcome to the chat console in the current session. Hard fails stop the pipeline; soft fails warn and continue. No step is silently skipped. **No status files are created** — no `REPORT.md`, no `QA_CHECKLIST.md`, no `.claude/reports/<date>-<name>.md`, no email drafts. The chat IS the report; the commit message is the durable artifact.

| #    | Step                                                         | Tool                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | Hard/Soft                               | Verifies                                                                                                                                                                                     |
| ---- | ------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1    | Worktree                                                     | `superpowers:using-git-worktrees` → `feature/<name>` in `.claude/worktrees/<name>/`. A PostToolUse hook copies `.claude/settings.json` + `local.properties` into the new worktree (no manual `cp` needed).                                                                                                                                                                                                                                                                                                                                                                                          | hard                                    | clean tree + settings.json present                                                                                                                                                           |
| 2    | Scaffold module + routes                                     | `Skill new-feature <name>` per [`new-feature`](../new-feature/SKILL.md) generates the empty `:feature:<name>` Gradle module with convention plugin. ALSO append all `@Serializable` routes from the plan to `:shared:utils/.../navigation/Screen.kt` upfront in this single step. `git rebase main` first if another branch already shipped Screens. NO Figma calls yet.                                                                                                                                                                                                                            | hard                                    | module exists, `Screen.kt` compiles                                                                                                                                                          |
| 3    | Per-screen mini-loop (interleaved Figma + scaffold + verify) | **Repeat for each frame** — see "Per-screen mini-loop" section below. One iteration handles one screen end-to-end: Figma fetch → render → preview verify → fix → save Figma PNG to disk for Step 13. Implementation work between Figma calls (5-15 min/iter) naturally spaces MCP traffic so 10/min Pro cap is never hit.                                                                                                                                                                                                                                                                           | hard                                    | per-screen 5-piece compiles + `screenshots/<frame>__figma.png` exists                                                                                                                        |
| 4    | NavGraph wiring                                              | append `composable<Screen.X> { XRoute(...) }` per screen at the END of Step 3 loop (or in this dedicated step, depending on rebase ordering); wire the Route's navigation callbacks to `navController.navigate(...)` / `navigateUp()` per the existing NavGraph pattern                                                                                                                                                                                                                                                                                                                             | hard                                    | NavGraph compiles                                                                                                                                                                            |
| 5    | Koin aggregation                                             | `<Name>FeatureModule.kt` — `viewModelOf(::<Frame>ViewModel)` per VM; aggregate from `:di/.../KoinModule.kt`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         | hard                                    | DI resolves                                                                                                                                                                                  |
| 6    | Compile (whole module)                                       | `./gradlew :feature:<name>:compileCommonMainKotlinMetadata`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         | hard                                    | green                                                                                                                                                                                        |
| 7    | Detekt                                                       | `./gradlew detekt`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | hard                                    | 0 critical                                                                                                                                                                                   |
| 8    | Unit (commonTest)                                            | `gen-test` per VM with Mokkery + Turbine + assertk; assert state transitions and navigation-callback invocations match `flow[]`                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | hard                                    | `:feature:<name>:allTests` green                                                                                                                                                             |
| 9    | Composition (androidHostTest, Robolectric)                   | `<Frame>ScreenTest.kt` — render Loading / Content / Error states                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | soft                                    | `:feature:<name>:testDebugUnitTest` green — Robolectric is JVM-host UI test, intentionally lower priority than commonTest unit gates; flag misses in Attention required, never silently drop |
| 10   | Assemble + install                                           | `:androidApp:assembleDebug` → assert an emulator is up (`adb devices`; if none, `android emulator start <avd>` boots one and blocks until ready — see [`claude-in-mobile`](../claude-in-mobile/SKILL.md) "Emulator / simulator boot") → `android run --apks=<apk>`                                                                                                                                                                                                                                                                                                                                  | hard (soft-skip if no device: cloud/CI) | install OK                                                                                                                                                                                   |
| 11   | E2E backlog append                                           | append a `### <name>` section to [`docs/E2E_BACKLOG.md`](../../../docs/E2E_BACKLOG.md) (created by the testing/CI track) with `Positive` / `Negative` arrays — one bullet per `flow.positive` and `flow.negative` scenario, prospective `snake_case` test method name + one-line description (which navigation fires, which UI state asserts). Tests are NOT scaffolded as Kotlin files — the backlog is the parking lot until the activation gate is met.                                                                                                                                          | soft                                    | section appended, file compiles as Markdown                                                                                                                                                  |
| 12   | Emulator screenshot capture                                  | `android screen capture -o /tmp/cap.png && cwebp -q 75 /tmp/cap.png -o screenshots/<frame>__emulator__light.webp && rm /tmp/cap.png` per frame, light-mode. Captured PNG is ~1.3 MB → ~95 KB after WebP @ q=75 (≈13× smaller). The WebP still counts against the per-thread image budget when the agent `Read`s it — see [`media-budget.md`](../../rules/media-budget.md).                                                                                                                                                                                                                          | soft (skipped if no emulator/binary)    | files exist                                                                                                                                                                                  |
| 13   | **End-to-end visual fidelity check**                         | for each frame: load both `screenshots/<frame>__figma.png` (cached from Step 3 mini-loop) and `screenshots/<frame>__emulator__light.webp` (from Step 12) → agent compares both side-by-side in chat → log discrepancies (padding, colors, alignment, font weight, missing icons) → flag drift > threshold in `## Attention required` block. **Zero Figma MCP calls** — Figma PNGs are reused from Step 3 disk cache.                                                                                                                                                                                | soft                                    | chat output: per-screen PASS/diff verdict                                                                                                                                                    |
| 13.5 | **Structural audit**                                         | for each `<Frame>Screen.kt` produced by Step 3, invoke [`compose-screen-splitter`](../compose-screen-splitter/SKILL.md) in audit-only mode. 🟢 → continue. 🟡 → log warning to `## Attention required`, continue. 🔴 → execute the splitter's full protocol on that file, then re-run gates 1+4 (compile + composition test) before resuming the pipeline. This catches the case where the pipeline itself emits a 600-line Screen (typical for complex forms with ≥4 sections); without this gate, the new feature ships with technical debt baked in. The skill costs ~1s per file in audit mode. | soft                                    | per-Screen 🟢/🟡/🔴 verdict in chat                                                                                                                                                          |
| 14   | Codex review (mandatory)                                     | `codex review --uncommitted` (or per `codex-review` skill). If the `codex` CLI is unavailable / errors out / returns non-zero — fall back to **agent self-review**: spawn a `superpowers:code-reviewer` (or `general-purpose` Agent if subagent missing) reading the diff, project rules, and emit the same Findings/Suggestions/Risk-flag verdict format Codex would. NEVER skip silently. Document tool path used (Codex vs self-review) in the verdict header.                                                                                                                                   | hard                                    | verdict logged + classified (`Codex` or `Self-review fallback`)                                                                                                                              |
| 15   | Attention-required pass                                      | pipeline scans for design gaps, requirement gaps, architecture thin spots, test gaps, navigation conflicts → prints `## Attention required` block to chat console (merging items from Step 13 fidelity drift)                                                                                                                                                                                                                                                                                                                                                                                       | hard                                    | console output present in chat                                                                                                                                                               |
| 16   | Manual QA checklist                                          | print `## Manual QA` block to chat — positive / negative / neutral / design follow-ups / navigation contract checks. User reads from chat, no file written                                                                                                                                                                                                                                                                                                                                                                                                                                          | hard                                    | console output present in chat                                                                                                                                                               |
| 17   | Final report + commit                                        | print FULL report to chat (gates table, what landed, attention required, manual QA, visual fidelity verdicts, file list, branch name, screenshot paths) — this is the durable artifact along with the commit. Then ONE commit on `feature/<name>` whose message embeds the gates summary (NO push without explicit user authorization — project safety rule). NO email draft, NO REPORT.md, NO QA_CHECKLIST.md — chat console only.                                                                                                                                                                 | hard                                    | console report visible + commit landed                                                                                                                                                       |

### Per-screen mini-loop (Step 3 expansion)

The heart of the rate-limit-safe pipeline. **One iteration per screen.** Figma MCP calls are physically separated by 5-15 min of implementation work, eliminating the 10-calls/min burst risk that bulk Figma sweeps cause.

For each frame in declaration order:

| Sub-step | What                                         | Figma MCP calls                                                   | Notes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| -------- | -------------------------------------------- | ----------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 3a       | **Lazy validate nodeId**                     | 1× `get_screenshot`                                               | SKIP for the first frame — already validated in A.1 sanity check. For all other screens this doubles as "screen exists" check + visual context for the agent.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 3b       | **Fetch design context**                     | 1× `get_design_context`                                           | Pull layout / colors / fonts / variants. The PNG response is also the screenshot — keep it for Step 3e.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| 3c       | ~~Fetch variables~~                          | 0×                                                                | **SKIP by default — restore only if a frame is proven to use custom Figma variables.** Real tokens usually live as inline frame styles, not centralized Variables. Saves ~5 sec + 1 MCP slot per screen.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 3d       | **Implement 5-piece skeleton + PreviewData** | 0                                                                 | Write `<Frame>UiState.kt`, `<Frame>Action.kt`, `<Frame>ViewModel.kt`, `<Frame>Route.kt`, `<Frame>Screen.kt`, `<Frame>PreviewData.kt`. The Previews live **inline** at the bottom of `<Frame>Screen.kt` inside a `// region Previews` ... `// endregion` block per [`.claude/rules/preview.md`](../../rules/preview.md) Cardinal rule — at minimum a light + dark `<Frame>ScreenPreview` wrapped in `NutriSportPreview { ... }`, calling `fake<Frame>StateContent()` from `<Frame>PreviewData.kt` (which composition tests reuse). Add `@file:Suppress("UnusedPrivateMember")` at the top of `<Frame>Screen.kt`. **No separate `<Frame>ScreenPreview.kt` file is created.** 5-15 min wall time. This is the spacer that makes interleaving work. |
| 3e       | **Visual preview-verify**                    | 1× `get_screenshot` (or REUSE 3a/3b PNG if still in conversation) | Render `@Preview` in mind, compare to Figma. **MUST save the Figma PNG to disk:** `screenshots/<frame>__figma.png`. Step 13 of the main pipeline reuses this file with zero additional Figma calls.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| 3f       | **Fix drift**                                | 0                                                                 | Adjust the Compose code based on the comparison. Reuse the cached `get_design_context` from 3b — never re-fetch.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |

**Per-screen MCP cost:** 3 calls (validate + context + verify), with further skips:

- first frame: skip 3a → **2 calls**
- Reusing 3b's PNG for 3e (when conversation hasn't been compacted): skip 3e → **1-2 calls**
- Step 3c (`get_variable_defs`) is skipped by default — see row above.

**Total per pipeline run:** `1 (A.1 sanity) + ~3N (per-screen loop) ≈ 3N+1 calls`. For `N=10` screens: ~31 calls = 15% of daily budget.

**Burst characteristic:** Each iteration fires 2-3 MCP calls within ~30 seconds, then 5-15 min of implementation work in 3d. Even pessimistically (3 calls in 30s), the per-minute rate is ~6 calls/min — under the 10/min Pro cap.

**Anti-pattern:** Running 3a-3b for ALL screens upfront (bulk-prefetch) defeats the entire design — that's exactly the burst the old A.1+A.3+Step 4 caused. The per-screen rule is enforced by `figma-mcp-budget` per-screen interleaving.

Console-output formats live in [`references/output-templates.md`](references/output-templates.md).

## 5-piece contract (mandatory per frame) + sibling PreviewData

The pipeline produces five Kotlin artifacts per `<Frame>`, plus one shared fake-state factory file. None is optional. Previews live INLINE at the bottom of `<Frame>Screen.kt` per [`.claude/rules/preview.md`](../../rules/preview.md) Cardinal rule — never as a separate `<Frame>ScreenPreview.kt`.

```
feature/<name>/src/commonMain/kotlin/com/nutrisport/<name>/
  model/
    <Frame>UiState.kt         — full snapshot, data class | sealed class
    <Frame>Action.kt          — sealed interface, user events
  <Frame>ViewModel.kt         — state: StateFlow, onAction(action)
  <Frame>Route.kt             — stateful: koinViewModel + collectAsStateWithLifecycle + navigation callbacks
  <Frame>Screen.kt            — stateless: (state, onAction) -> Unit
                              — bottom: `// region Previews` ... `// endregion` with light + dark @Preview
  <Frame>PreviewData.kt       — fake-state factories (fake<Frame>StateContent(), …)
                              — reused by composition tests in androidHostTest
```

`@file:Suppress("UnusedPrivateMember")` is mandatory at the top of `<Frame>Screen.kt` so detekt does not flag the inline Previews as dead code.

Forbidden in `<Frame>Screen.kt` (outside the `// region Previews` block): `koinInject`, `koinViewModel`, navigation calls, `LaunchedEffect` reading DI, `NavController` parameter. Navigation is invoked only from the Route via callback lambdas that the NavGraph wires.

Forbidden as a file: `<Frame>ScreenPreview.kt`. The pipeline never creates one. Migrate any legacy sibling preview file lazily per `preview.md` "Migration" recipe whenever its Screen is touched, never as a sweep.

## Quality gates (fail-fast order)

| #   | Gate                     | Command                                                                                                                                                                               | Cold        | Warm        | Action               |
| --- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- | ----------- | -------------------- |
| 1   | Compile                  | `./gradlew :feature:<name>:compileCommonMainKotlinMetadata` (also verifies that every inline `// region Previews` block in `<Frame>Screen.kt` compiles — Previews live in commonMain) | ~10 s       | ~5 s        | hard                 |
| 2   | Detekt                   | `./gradlew detekt`                                                                                                                                                                    | ~30 s       | ~5 s        | hard                 |
| 3   | Unit                     | `./gradlew :feature:<name>:allTests`                                                                                                                                                  | 1–2 min     | ~30 s       | hard                 |
| 4   | Composition              | `./gradlew :feature:<name>:testDebugUnitTest`                                                                                                                                         | ~30 s       | ~10 s       | hard                 |
| 5   | Assemble + install       | `:androidApp:assembleDebug` + `android run --apks=<apk>` (emulator must already be booted — manual local-dev prerequisite)                                                            | 4–6 min     | 2 min       | hard                 |
| 6   | E2E backlog append       | edit `docs/E2E_BACKLOG.md`, append `### <name>` section with `Positive` / `Negative` arrays                                                                                           | <5 s        | <5 s        | soft                 |
| 7   | Screenshots              | `android screen capture -o /tmp/cap.png && cwebp -q 75 /tmp/cap.png -o screenshots/<frame>__emulator__light.webp && rm /tmp/cap.png` per frame                                        | ~30 s/frame | ~30 s/frame | soft                 |
| 8   | Codex review (mandatory) | `codex review --uncommitted`; fallback to self-review Agent on failure                                                                                                                | ~30 s       | ~30 s       | hard (with fallback) |

Hard fail → pipeline halts, the chat output captures the failure verbatim and the console report header reads `partial`. Soft fail → continue, the chat output notes the skip, console report header still reads `success` if compile / detekt / unit / composition all green.

## Conflict zones (handle automatically)

When two pipeline plans both touch the same shared file, the second one rebases on `main` before commit. Per [`.claude/features/00-orchestrator.md`](../../features/00-orchestrator.md):

- `:shared:utils/.../navigation/Screen.kt` — append-only `@Serializable` lines
- `:navigation/.../NavGraph.kt` — append-only `composable<Screen.X>` lines
- `:di/.../KoinModule.kt` — append-only `add(<name>FeatureModule)` lines

If auto-rebase fails, the pipeline halts at step 3 and prints the conflict (file paths + offending hunks) to chat so the human resolves manually before retrying.

## Console report (Step 17 output)

Header: `[feature-pipeline YYYY-MM-DD] <name> — success|partial (X frames, Y unit, Z composition, K E2E backlog cases, N screenshots)`

Body printed verbatim to chat: gates table (status per step), file list (what landed, grouped by module), branch name, screenshot paths under `.claude/worktrees/<name>/screenshots/`, top 3 `## Attention required` items, full `## Manual QA` checklist, `## Visual fidelity` verdicts (per-screen PASS / drift summary). The same content is also embedded in the commit message — git log is the durable record. NO email is created, NO file is written.

## Anti-patterns

- Silently expanding scope — when the pipeline scaffolds domain+network because they were missing, the plan's `## Context` section MUST flag the expansion explicitly so the user understands the run is bigger than feature-only
- Inventing scenarios silently — every gap goes through `AskUserQuestion` or the `## Attention required` block in chat
- Skipping the worktree — pipeline always runs in `.claude/worktrees/<name>/`, never on `main`
- Touching real `:network` data sources — the pipeline runs against `FakeXxxDataSource` only
- Creating an email draft, REPORT.md, QA_CHECKLIST.md or any dated `.claude/reports/` file — chat console + commit message are the only artifacts
- Creating a `:core:*` module on its own — `:core:<name>` is bootstrapped deliberately when two or more features share a UI artefact; the pipeline must NOT spin one up unprompted
- Running on a branch that already exists — `git worktree add` fails noisily; pipeline aborts
- Putting `koinViewModel` in `Screen.kt` — contract violation, hard fail at step 6
- Pushing the branch without explicit user authorization — commits stay local until the user says push
- **Skipping unit tests "for time" (Step 8)** — Step 8 is hard. CommonTest coverage of state transitions and navigation-callback assertions is not optional. Composition (Step 9, Robolectric) and on-device E2E are soft because they're slower / flakier; pure JVM unit tests are fast and have no excuse.
- **Skipping Codex review silently (Step 14)** — Step 14 is hard. If `codex` CLI is broken or returns non-zero, fall back to a self-review Agent (read diff + project rules, emit Findings + Suggestions + Risk-flag verdict) and label the verdict `Self-review fallback (Codex unavailable)`. NEVER drop the review entirely.

## Risks

| #   | Risk                      | Mitigation                                                                                                                                                                               |
| --- | ------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Context exhausted mid-run | Split via continuation-prompt protocol — commit checkpoint, fresh session resumes from named pending step (see [`references/continuation-prompt.md`](references/continuation-prompt.md)) |
| 2   | Codex offline             | `/codex:review` has graceful fallback; pipeline logs `codex: skipped`                                                                                                                    |
| 3   | Emulator wedged           | reboot once, then soft-skip emulator-dependent steps; chat report flags it                                                                                                               |
| 4   | Screen.kt rebase conflict | hard halt at step 3, manual resolve before retrying                                                                                                                                      |
| 5   | Figma frame missing       | hard halt at A.1, plan is never written                                                                                                                                                  |
| 6   | Domain layer missing      | scope expands silently to scaffold full vertical slice; flag in plan `## Context`                                                                                                        |

Three concurrent worktrees max — hard cap.

## Verification (smoke run)

```bash
git worktree list
ls .claude/worktrees/

# manual run on the smallest feature
/feature
# > "Profile flow: form + save"
# > [paste Figma URL]
# (interactive, plan-mode, you confirm)
# Enter → Phase B begins in the same conversation

ls .claude/worktrees/profile/screenshots/
# expect: <frame>__figma.png AND <frame>__emulator__light.webp per screen
git -C .claude/worktrees/profile log --oneline

# contract checks
grep -r "koinViewModel\|koinInject" feature/profile/src/commonMain/.../Screen.kt
# → empty (Screen is clean)
grep -r "NavController" feature/profile/
# → empty (Route uses navigation callbacks, not NavController)
grep "composable<Screen.Profile" navigation/.../NavGraph.kt
# → present
```

## Related

- [`.claude/rules/conventions.md`](../../rules/conventions.md) — Route/Screen separation contract
- [`.claude/rules/architecture.md`](../../rules/architecture.md) — module dependency graph
- [`.claude/rules/testing.md`](../../rules/testing.md) — test pyramid (Unit / Composition / E2E)
- [`.claude/features/00-orchestrator.md`](../../features/00-orchestrator.md) — groups, sequence rule, conflict zones
- [`.claude/skills/new-feature/SKILL.md`](../new-feature/SKILL.md) — 5-piece scaffold
- [`.claude/skills/figma-handoff/SKILL.md`](../figma-handoff/SKILL.md) — Figma → Compose protocol
- [`.claude/skills/gen-test/SKILL.md`](../gen-test/SKILL.md) — test recipes
- [`.claude/skills/compose-screen-splitter/SKILL.md`](../compose-screen-splitter/SKILL.md) — Step 13.5 audit + on-demand fix for any Screen ≥300 lines
- [`.claude/skills/claude-in-mobile/SKILL.md`](../claude-in-mobile/SKILL.md) — Steps 10/12/13 emulator install + screenshot capture (CLI, local-dev only)
- [`references/plan-template.md`](references/plan-template.md) — plan frontmatter spec
- [`references/output-templates.md`](references/output-templates.md) — chat-console report formats
- [`references/continuation-prompt.md`](references/continuation-prompt.md) — handoff template for split-session execution
