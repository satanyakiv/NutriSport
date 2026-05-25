---
name: figma-handoff
description: Translate Figma frames into Compose Multiplatform code following NutriSport's design system. Use this skill whenever the user asks to implement a UI screen, build a component from a Figma frame, "translate this Figma to Compose", "use design from Figma", or references the project's Figma file (URL, frame name, or node ID). Handles Figma Dev Mode MCP calls (selection check, get_design_context, get_variable_defs, get_screenshot), color/font/component mapping to :shared:ui, dark theme verification, preview and test scaffolding.
---

# Figma Handoff — Figma → Compose Multiplatform

## Context

The project's design source of truth is the Figma file "NutriSport-Mobile-Yakiv" (file key `5r0fI3ij4KxETz5HNK5qfc`). It contains both design-system primitives (Colors, Fonts, Buttons, Cards, …) and app screens.

This skill enforces a 13-step protocol for translating a Figma frame into Compose Multiplatform code that respects the `:shared:ui` design system. The user has a paid Figma plan, so the Dev Mode MCP must be used on every UI task — never hand-eyeball values from screenshots.

## When to use

Activate when the user:

- Asks to implement a screen, component, or design token from Figma
- Says "translate this Figma frame to Compose", "build UI for X", "use design from Figma"
- References the project's Figma file (URL, frame name, or node ID)
- Pastes a Figma URL like `https://www.figma.com/design/.../...?node-id=X-Y`

## Prerequisites

- Figma Desktop app open with file "NutriSport-Mobile-Yakiv"
- Figma Dev Mode MCP enabled (the `mcp__figma-dev-mode__*` server)
- User authenticated as `yakiv.bondar@gmail.com`

For full MCP technical details, tool list, and selection rules, see [references/figma-protocol.md](references/figma-protocol.md).

For a color/font/component mapping cheat-sheet, see [references/cmp-mapping.md](references/cmp-mapping.md).

## 13-step Protocol

### 0. Activate budget discipline

Before any Figma MCP call, activate [`figma-mcp-budget`](../figma-mcp-budget/SKILL.md). It defines orthogonal rules: prefer `get_design_context` for scope-bound nodes, `get_metadata` only as fallback for truncation, **no-retry on 429**, **anti-runaway guard** (2 same-args errors → STOP), behavioral session cache (don't re-call `get_design_context` on same nodeId in a conversation), `whoami` once per session. Pro plan caps: **10 calls/min, 200 calls/day**. Skipping this step risks burning the daily budget on a single runaway loop — see [`figma-mcp-budget/references/failure-modes.md`](../figma-mcp-budget/references/failure-modes.md).

### 1. Gather inputs

Inputs can take three forms — handle in this order:

1. **Screen route name** — e.g. `Details`, `Checkout`, `Profile`. Skip to Step 1.5 to resolve via the frame map.
2. **Figma URL** — e.g. `https://www.figma.com/design/<fileKey>/<name>?node-id=X-Y`. Extract `fileKey` (segment after `/design/`) and `nodeId` (convert URL `?node-id=X-Y` → `"X:Y"`, dash → colon).
3. **Bare nodeId** (`X:Y` format) — assumes `fileKey` defaults to `5r0fI3ij4KxETz5HNK5qfc` ("NutriSport-Mobile-Yakiv"). Confirm before continuing if the user's brief implies a different file.

If none is provided, ask the user for the screen name, frame URL, or nodeId.

### 1.5 Resolve nodeId from Screen route

If the input is a `Screen` route name:

1. Read [`.claude/figma/SCREEN_FRAME_MAP.md`](../../figma/SCREEN_FRAME_MAP.md).
2. Find the row whose `Screen route` matches the input and use its `Content nodeId`.
3. If that row's nodeId is still `TBD` (the map is a stub until frames are captured), `AskUserQuestion`: "`<route>` has no Figma nodeId yet — paste the frame URL and I'll record it in the map." Then continue with the URL and backfill the row.
4. If no row matches, ask the user for the frame URL or nodeId.

### 2. Selection check

- If you have `nodeId`, first call `mcp__figma-dev-mode__get_screenshot(fileKey, nodeId)` — does NOT require Figma selection, gives visual context cheaply.
- Before `get_design_context` or `get_variable_defs`, confirm selection. If a previous call returned `"You currently have nothing selected"`, ask:
  > "Open `NutriSport-Mobile-Yakiv` in Figma Desktop and click frame `<name>`. Tell me when selected — I'll re-run the request."

### 3. Pull design context

```
mcp__figma-dev-mode__get_design_context(fileKey, nodeId)
```

Returns CMP-like code suggestion + screenshot + metadata. **Do NOT copy verbatim** — Figma often returns React/HTML/SwiftUI; translate idiomatically to Compose Multiplatform.

### 4. Pull variables — SKIP by default

**Do NOT call `get_variable_defs` unless a frame is proven to use custom Figma variables.** Many frames use iOS/macOS system libraries only, with real tokens living as inline frame styles. Calling `get_variable_defs` on such a frame returns an empty payload while consuming one of the 200/day Figma MCP slots and ~5 sec of round-trip latency.

If a frame is proven to use custom Variables, restore the call locally for that one handoff:

```
mcp__figma-dev-mode__get_variable_defs(fileKey, nodeId)
```

Verify by inspecting the Figma frame's variable bindings before re-enabling.

### 5. Re-read our design system

The design-system source of truth is the code itself:

- `shared/ui/src/commonMain/kotlin/com/nutrisport/shared/Colors.kt` — palette
- `shared/ui/src/commonMain/kotlin/com/nutrisport/shared/Fonts.kt` — typography (`BebasNeueFont()`, `RobotoFont()`, `FontSize`)
- `shared/ui/src/commonMain/kotlin/com/nutrisport/shared/component/` — list of reusable components

(A design-system doc may be added later by the design-system track; until then, read the code.) Quick mapping table is in [references/cmp-mapping.md](references/cmp-mapping.md).

### 6. Map colors

- Every Figma hex → `MaterialTheme.colorScheme.<slot>` or a `com.nutrisport.shared.<token>` from `Colors.kt`
- If no token exists → add to `Colors.kt`
- **Forbidden:** inline hex (`Color(0xFFAABBCC)`) in any Composable

### 7. Map fonts

- Display → `BebasNeueFont()`
- Body → `RobotoFont()`
- Sizes → `FontSize.X` only (EXTRA_SMALL=10, SMALL=12, REGULAR=14, EXTRA_REGULAR=16, MEDIUM=18, EXTRA_MEDIUM=20, LARGE=30, EXTRA_LARGE=40)
- Prefer Material3 typography slots (`MaterialTheme.typography.headlineMedium`, etc.) where a `MaterialTheme` slot fits

### 8. Component reuse

Check `:shared:ui/component/` first:

| Figma element           | Code              |
| ----------------------- | ----------------- |
| Primary CTA button      | `PrimaryButton`   |
| Text input              | `CustomTextField` |
| Inline-alert text input | `AlertTextField`  |
| Loading skeleton        | `LoadingCard`     |
| Error state             | `ErrorCard`       |
| Info banner             | `InfoCard`        |
| No-network banner       | `OfflineBanner`   |
| Product tile            | `ProductCard`     |
| Profile fields          | `ProfileForm`     |
| Quantity stepper        | `QuantityCounter` |

If nothing fits → add to `:shared:ui/component/`. **Never inline a one-off composable in a feature module if it could be reused.**

Icons: NutriSport uses Material `Icons.Default.X` directly — no icon-indirection layer.

### 9. Route/Screen separation

Per `.claude/rules/conventions.md`:

- `{Name}Route` — handles DI + state collection (`koinViewModel<{Name}ViewModel>()` + `collectAsStateWithLifecycle()`) and navigation callbacks
- `{Name}Screen` — pure UI, all callbacks via parameters

Navigation calls Route, never Screen directly.

### 10. Dark theme verification

Verify the screen renders correctly under `MaterialTheme`'s dark color scheme:

- If broken → fix the color mapping or add a dark-specific token
- Use the light + dark `@Preview` pair (next step) to render both modes

### 11. Preview

Scaffold Preview functions per [`.claude/rules/preview.md`](../../rules/preview.md) — that file is the single source of truth for the contract (variant policy, naming, the `NutriSportPreview` helper, and what's forbidden inside a Preview body).

Quick recap for Screens: light + dark via two separate `@Preview` functions wrapping the Screen in `NutriSportPreview { ... }`, with hand-built fake state (no DI, no `koinViewModel`, no navigation). New design-system components added during this handoff use light theme only + every significant state.

### 12. Test

For critical user journeys (auth, checkout, cart, payment), add a `compose.uiTest` smoke test in `androidHostTest`:

```kotlin
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class {Name}ScreenTest {
    @Test
    fun `should render content state`() = runComposeUiTest {
        setContent { {Name}Screen(state = ...) }
        onNodeWithTag("...").assertIsDisplayed()
    }
}
```

Verify: `./gradlew :feature:{name}:allTests --tests "*{Screen}Test"`

### 13. Visual verify (preview-based) — and SAVE the Figma PNG to disk

After the Composable is written and `@Preview` renders cleanly, do a **visual diff check** against the source Figma frame. This is the per-screen quality gate that the [`feature`](../feature/SKILL.md) pipeline relies on (see Step 3e of its per-screen mini-loop).

Procedure:

1. **Re-fetch the Figma screenshot** (or reuse the PNG already in conversation context from Step 3 `get_design_context`):
   ```
   mcp__figma-dev-mode__get_screenshot(fileKey, nodeId)
   ```
2. **MANDATORY — save the PNG to disk:** `screenshots/<frame>__figma.png` (relative to the worktree or repo root). The [`feature`](../feature/SKILL.md) pipeline's Step 13 (end-to-end visual fidelity check) loads this file at the end of the run to compare against an emulator screenshot — this saves N additional Figma MCP calls.
3. **Compare** the `@Preview` render (Studio preview pane) against the Figma PNG side-by-side. Look for:
   - Padding / spacing / margins
   - Font weight, size, line-height
   - Colors (background, text, borders) — verify token names, not hex
   - Icon size and placement
   - Component variants (button states, input states)
4. **Fix drift** without making further Figma calls. The `get_design_context` response from Step 3 is still in conversation context — re-read it instead of re-fetching.

Verify: `screenshots/<frame>__figma.png` exists; `@Preview` matches Figma to a reasonable tolerance (small subpixel padding differences are OK; missing icons / wrong colors / wrong font are not).

### Step 13 fidelity branch — lightweight tools only

When the goal is **only** a visual diff (no code generation, just compare an emulator/preview screenshot vs the Figma render), do NOT call `get_design_context`. Use the two cheap tools instead:

- `mcp__figma-dev-mode__get_screenshot(fileKey, nodeId)` — render image, ~5s, ~3 kB response
- `mcp__figma-dev-mode__get_metadata(fileKey, nodeId)` — XML structure (rows / labels / heading text), ~2s, ~1 kB

Why: `get_design_context` bundles code + screenshot + metadata; on dense frames it routinely times out (>30s) and the code part is unused for fidelity work. The single-purpose tools are cheaper, faster, and don't burn budget on truncation/timeout retries.

`get_design_context` remains **mandatory** for Steps 3–7 (initial implementation pass) — there you need code suggestions and variable definitions in one shot.

**Timeout fallback:** if `get_design_context` times out during the implementation pass, do NOT retry. Fall back to `get_screenshot` + `get_metadata` for the missing piece (visual or structural) and continue. Per [`figma-mcp-budget`](../figma-mcp-budget/SKILL.md) Rule 5, retrying after a timeout is the same anti-pattern as retrying after 429.

## Anti-patterns

- Copying React/HTML/SwiftUI snippets verbatim from `get_design_context` output
- Inline hex colors (`Color(0xFFAABBCC)`) in Composables
- `FontFamily(Font(R.font.X))` Android-style — use `BebasNeueFont()` / `RobotoFont()`
- Skipping dark theme check
- One-off composables in feature module that duplicate `:shared:ui/component/`
- Calling `get_design_context` without verifying Figma selection — wastes a request

## Related

- [`.claude/rules/architecture.md`](../../rules/architecture.md) — module structure, dependency flow
- [`.claude/rules/conventions.md`](../../rules/conventions.md) — Route/Screen pattern, Compose style
- [references/figma-protocol.md](references/figma-protocol.md) — Figma MCP details
- [references/cmp-mapping.md](references/cmp-mapping.md) — color/font/component mapping cheat-sheet
