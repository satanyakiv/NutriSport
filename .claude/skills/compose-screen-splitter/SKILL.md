---
name: compose-screen-splitter
description: Audit and split monolithic Compose Screen files in the NutriSport KMP project before they become unmanageable. Use when a Screen file feels too long, when you see warnings like "DetailsScreen.kt is 568 lines", when the user says "split this Screen", "розпиляй екран", "decompose composable file", "екран став некерованим", "audit screen size", "файл занадто довгий", "extract sections from this Screen", or after the `feature` pipeline generates a new Screen ≥300 lines. Detects threshold breaches (lines, composable count, nesting, distinct UI concerns), proposes a project-conventional split that respects Route↔Screen contract, Preview discipline, `:core:*` cross-feature rules, and `:shared:ui` design-system rules, then either reports (audit-only) or executes the refactor with compile + composition test verification. Always invoke this skill instead of free-handing a Screen split — it bakes in the project's split-point heuristics so the result lands in the right module first try.
---

# Compose Screen Splitter

## Why this skill exists

Compose Screens accumulate UI sections (header, list, summary, dialog, sheet, …) until the file becomes hard to navigate, hard to review, and hard to preview. The Medium article ["How to split Compose screens before they become unmanageable"](https://androidmeda.medium.com/how-to-split-compose-screens-before-they-become-unmanageable-67d61465da56) names the symptoms (600-800 lines, 3-4 levels of nesting, six concerns sharing one file) and the cure (Screen↔Content split, name-driven extraction, feature-local component packages).

The NutriSport project has already adopted most of the cure as rules — Route↔Screen separation in [`conventions.md`](../../rules/conventions.md), `:core:<name>` for cross-feature reuse in [`architecture.md`](../../rules/architecture.md) Rule 12, design-system primitives in [`:shared:ui/component/`](../../../shared/ui/src/commonMain/kotlin/com/nutrisport/shared/component), Preview discipline in [`preview.md`](../../rules/preview.md). What's missing is an executor: nobody is responsible for noticing when a Screen crossed the threshold and applying these rules without breaking the contract surface (DI in Routes, no DI in Screens, Preview light+dark for Screens, `internal` reuse never `public`).

This skill is that executor. It works in two modes — **audit-only** (read-only report) and **execute** (run the full split with compile + test verification).

## When to invoke

Trigger phrases (English + Ukrainian, dictation-friendly):

- "split this Screen", "розпиляй цей екран", "decompose this composable file"
- "DetailsScreen is too long", "файл занадто довгий"
- "audit screen size", "перевір розмір екрана"
- "extract sections from XxxScreen", "винеси секції в окремі файли"
- After the `feature` pipeline finishes and the new Screen is ≥300 lines (Phase 4 wires this in automatically — see [`feature/SKILL.md`](../feature/SKILL.md) Step 19.5).

If the user mentions a `*Screen.kt` file in any of these contexts, invoke the skill.

## Detection thresholds (project-tuned)

The article cites 600-800 lines as the trigger. NutriSport tunes lower because the project already has a rich `component/` track and `:shared:ui` library — sections that would be inline in a vanilla Compose project belong in those modules here.

| Status       | Effective lines | `@Composable` count | Max nesting | Distinct concerns |
| ------------ | --------------- | ------------------- | ----------- | ----------------- |
| 🟢 OK        | ≤250            | ≤6                  | ≤3          | ≤2                |
| 🟡 Warn      | 251-400         | 7-10                | 4           | 3                 |
| 🔴 Split now | >400            | >10                 | ≥5          | ≥3                |

### Effective lines (load-bearing definition)

Per [`preview.md`](../../rules/preview.md) "Effective line count" section, Preview functions live inline at the bottom of the same file as the composable they show, fenced by `// region Previews` ... `// endregion`. The audit subtracts that block from the total:

```bash
TOTAL=$(wc -l < "$FILE")
PREVIEW=$(awk '/\/\/ region Previews/,/\/\/ endregion/' "$FILE" | wc -l)
EFFECTIVE=$((TOTAL - PREVIEW))
```

A 700-line file with a 350-line Previews region is 350 effective lines → 🟢 OK, do not split. The composable count and concerns count also exclude Previews — `@Preview` functions are not part of the runtime surface.

If a file has Preview functions but no `// region Previews` fence, treat the whole file as effective lines and recommend the user add the fence as part of the audit fix. Do not auto-add it during a split — that's a separate concern.

**Concerns** = semantically distinct UI buckets in one file (excluding the Previews region). Pattern-match on these prefixes/suffixes inside composable names: `*Header*`, `*TopBar*`, `*List*`, `*Card*`, `*Section*`, `*Block*`, `*Sheet*`, `*Dialog*`, `*Empty*State*`, `*Error*State*`, `*Loading*`, `*Footer*`, `*Form*`, `*Field*`, `*Filters*`, `*Summary*`, `*Badge*`. Three or more distinct concerns alone trigger 🔴 even if line count is fine, because the file is doing too many jobs.

**Auto-skip** (don't audit, output 🟢 with note): if a sibling `component/` directory exists with ≥4 files AND the Screen file is <300 lines, the Screen has already been split — leave it alone unless the user explicitly asks for a re-audit.

## 5-step protocol

### Step 1 — Audit

`Read` the target file. Compute:

```bash
# Total lines
TOTAL=$(wc -l < "$FILE")

# Preview region size (0 if no fence — flag for the user to add it)
PREVIEW=$(awk '/\/\/ region Previews/,/\/\/ endregion/' "$FILE" | wc -l)

# Effective lines — the threshold metric
EFFECTIVE=$((TOTAL - PREVIEW))

# Composable count, excluding @Preview-annotated functions
grep -B1 "^@Composable" "$FILE" | grep -v "^@Preview" | grep -c "^@Composable"
# (or fallback) grep -cE "^(private |internal |public )?fun [A-Z]" <path>  -- filter @Preview manually

# Approx max nesting (heuristic) — ignore the Previews region
awk 'BEGIN{max=0;d=0;skip=0}
     /\/\/ region Previews/{skip=1}
     /\/\/ endregion/{skip=0; next}
     skip{next}
     /\{[[:space:]]*$/{d++; if(d>max)max=d}
     /^[[:space:]]*\}/{d--}
     END{print max}' "$FILE"
```

Then read the file and tally distinct concerns by pattern-matching composable names against the prefix/suffix list above (excluding `@Preview` functions). Produce this exact table:

```
| Metric | Value | Threshold | Status |
| ------ | ----- | --------- | ------ |
| Total lines | 700 | — | (info) |
| Previews region | 300 | — | (info) |
| Effective lines | 400 | 400 | 🟡 |
| @Composable count (non-Preview) | 13 | 10 | 🔴 |
| Max nesting | 4 | 4 | 🟡 |
| Distinct concerns | 5 (Header, Block, Row, Settings, Legal) | 3 | 🔴 |
```

If the Previews region is missing (`grep -c "// region Previews" "$FILE"` returns 0) and `@Preview` functions exist in the file, add a `🟡 add fence` row and recommend the user wraps the existing previews. Continue auditing as if `Previews region = 0`.

Then list every `@Composable` in the file with its visibility, line range, and a one-line tag describing what it does. This list feeds Step 2.

### Step 2 — Classify each composable

For each composable in the file, route it through [`references/decision-tree.md`](references/decision-tree.md). Each composable lands in exactly one of:

- **Leave inline** — single-use, <15 lines, no own state. Don't extract.
- **`feature/<x>/.../<screen>/component/Xxx.kt`** — used only by this Screen but ≥15 lines or has own state.
- **`feature/<x>/.../component/Xxx.kt`** — reusable across screens of the same feature.
- **`:core:<name>/.../component/Xxx.kt`** — reusable across two or more `:feature:*` modules. Add Rule 12 dependency wiring.
- **`:shared:ui/.../component/Xxx.kt`** — generic visual primitive (separator, section heading, status badge with no domain types in its signature). Verify via `grep -r "fun XxxName" :shared:ui` first — if it already exists, reuse instead of duplicating.

Visibility rule per [`architecture.md`](../../rules/architecture.md) §"Naming" + project Kotlin conventions: `private` for inline-only, `internal` for cross-file reuse within a module, `public` only when crossing a module boundary.

### Step 3 — Propose split

Present the proposal to the user via `AskUserQuestion` with three preset variants:

1. **Aggressive** — extract everything classified as ≥component-tier, including borderline cases. Maximum file count, smallest Screen file. Risk: over-extraction (anti-pattern from the article).
2. **Balanced (recommended)** — extract everything `≥15 lines` or `multi-use` or `multi-concern`, leave true single-line wrappers inline. Medium file count, Screen drops to ~30-40% of original.
3. **Conservative** — extract only the obvious top-3 sections (largest, most state-bearing, most reused). Screen drops to ~60-70% of original. Keeps risk low if the Screen is mid-development.

Each variant lists target files with their `path → composable name` and the package they end up in. Show the user before executing.

**Forbidden filenames**: `XxxComponents.kt`, `XxxHelpers.kt`, `XxxUtils.kt`, `XxxParts.kt`, `XxxBits.kt`. Article §4 names this directly: "use specific file names; avoid vague terms". Project precedent agrees: `feature/home/cart/.../component/CartItemCard.kt`, not `CartComponents.kt`. **One file = one publicly-named composable** (helpers it uses internally are fine in the same file).

### Step 4 — Execute

After the user picks a variant, perform the moves:

1. For each extracted composable, `Write` a new file at the target path with full imports, package declaration matching the directory, the composable itself promoted to `internal` (or kept `private` if it's truly file-local sub-helper), and a single light-mode `@Preview` per [`preview.md`](../../rules/preview.md). Components don't need dark previews; only Screens do.
2. `Edit` the original `XxxScreen.kt` to remove the extracted composables and replace call sites with imports.
3. **Preserve the `// region Previews` block in `XxxScreen.kt`** — Previews live inline at the bottom of `XxxScreen.kt` per [`preview.md`](../../rules/preview.md) Cardinal rule. They depend on `XxxScreen` + `XxxPreviewData`, not on the extracted internals. Light + dark variants stay there. If the Screen still has a legacy sibling `XxxScreenPreview.kt`, the audit flagged it in Step 1 — fold it inline as part of the split (recipe in `preview.md` "Migration from sibling `XxxScreenPreview.kt`").
4. **Preserve `XxxPreviewData.kt`** as-is — fake-state factories are still used by the inline Previews in `XxxScreen.kt` AND by composition tests.
5. If a composable goes to `:core:<name>`, also:
   - Verify the `:core:<name>` module exists; if not, ABORT and tell the user a new `:core:*` module is needed (out of scope for this skill — see `architecture.md` Rule 12 for the bootstrap procedure).
   - Add `implementation(project(":core:<name>"))` to the feature module's `build.gradle.kts` if not already present.
6. If a composable goes to `:shared:ui`, also:
   - Read `docs/design-system.md` first to confirm naming aligns with the design system.
   - Add the new file to the `component/` listing in `CLAUDE.md` if it deserves discovery.

### Step 5 — Verify

Run, in order:

```bash
./gradlew :feature:<name>:compileAndroidMain
```

(KMP task naming — Android target's main compilation. NOT `compileDebugKotlinAndroid`, which is the AGP-variant name and doesn't exist in this project's KMP convention plugins. iOS counterparts: `compileKotlinIosArm64`, `compileKotlinIosSimulatorArm64`. Run iOS only if the split touched anything in `iosMain`.)

If it fails, read the error, fix imports/visibility, retry once. If it still fails, `git restore` the changes and report to the user.

If the feature has composition tests:

```bash
./gradlew :feature:<name>:allTests --tests "*ScreenTest"
```

Report a before→after table to the user:

```
| Metric | Before | After | Δ |
| ------ | ------ | ----- | - |
| Lines (XxxScreen.kt) | 568 | 142 | -75% |
| Files in feature | 5 | 9 | +4 |
| Largest extracted | — | 87 lines | — |
| Compile | n/a | ✅ | — |
| Tests | n/a | ✅ 12/12 | — |
```

## Anti-patterns (what NOT to do)

These come straight from the article §7 and from project conventions; violating them creates worse code than the monolithic Screen.

1. **Over-extraction.** A 3-line `Spacer` wrapper or a 5-line single-use `Row { Icon(...); Text(...) }` does not need its own file. The article: _"twenty tiny single-use functions [are] harder to follow than one medium-sized function"_. If asked to extract such a thing, refuse and explain.
2. **Feature-creep into `:shared:ui`.** A composable that takes `order: OrderUi` or `event: EventUi` as a parameter is not a generic primitive — it's a domain-shaped UI. It belongs in `:core:<name>`, not `:shared:ui`. The article: _"a component that starts taking feature-specific parameters... is no longer a shared component"_.
3. **Breaking Route↔Screen contract.** Routes own DI (`koinViewModel`, `koinInject`). Screens are pure `(state, onAction) -> Unit`. The split must happen INSIDE the Screen layer — extracted components also stay pure. Never inject Koin into a `component/Xxx.kt` file. See [`conventions.md`](../../rules/conventions.md) §"State Hoisting".
4. **Vague filenames.** `XxxComponents.kt`, `XxxHelpers.kt`, `XxxUtils.kt`, `XxxParts.kt` — banned. Each file is named after its single public composable.
5. **Breaking Preview discipline.** Every extracted component gets a `@Preview` (light only) per [`preview.md`](../../rules/preview.md), inside a `// region Previews` block at the bottom of the new component file. The Screen's inline Previews (`// region Previews` in `XxxScreen.kt`) keep their light+dark variants. Never delete a Preview "to clean up". If a separate `XxxScreenPreview.kt` exists from before this rule, fold it inline as part of the split.
6. **Duplicating across `:shared:ui`.** Before extracting a generic primitive (separator, section heading, badge, status indicator), `grep -r "fun XxxName" :shared:ui` to check. If the same visual primitive (separator, section heading, status badge) starts appearing across two or more Screens, it should consolidate to `:shared:ui`, not multiply across `component/` directories.
7. **Refusing to leave good code alone.** A 280-line Screen with two clear sections and no nesting is fine. Don't split it just because it crossed the 250-line 🟡 line. The 🟡 zone is a signal to monitor, not to act.

## Cross-references (read these before executing)

| Rule file                                        | Why it matters                                                                            |
| ------------------------------------------------ | ----------------------------------------------------------------------------------------- |
| [`architecture.md`](../../rules/architecture.md) | Module dependency graph; `:core:*` cross-feature track (Rule 12); `:shared:ui` boundaries |
| [`conventions.md`](../../rules/conventions.md)   | Route↔Screen contract; visibility of internal composables                                 |
| [`preview.md`](../../rules/preview.md)           | `@Preview` requirements for components vs Screens                                         |
| [`testing.md`](../../rules/testing.md)           | Composition test target after a split                                                     |
| [`models.md`](../../rules/models.md)             | Where `*Ui` types live (controls whether a composable is `:core:*` vs `:shared:ui`)       |

## Reference files

- [`references/article-summary.md`](references/article-summary.md) — full distillation of the Medium article (works offline, immune to article URL rot).
- [`references/refactor-recipes.md`](references/refactor-recipes.md) — five archetype recipes (index, long form, list, multi-section detail, "good already") anchored to NutriSport screens. None currently breach the threshold — they are templates for when a Screen grows.
- [`references/decision-tree.md`](references/decision-tree.md) — extraction destination decision tree with project examples.

## Modes

**Audit-only mode** — triggered when the user asks "audit", "should I split", "how big is", "is this OK", or when invoked from the `feature` pipeline Step 19.5. Run Steps 1-3, then STOP. Do not write any files. Output the proposal as a chat report.

**Execute mode** — triggered when the user asks "split", "refactor", "розпиляй", "винеси", or explicitly approves an audit-mode proposal. Run all 5 Steps. Verify compile + tests before reporting done.

When in doubt, ask: "Audit-only or execute?"
