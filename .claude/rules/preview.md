# Preview Discipline

Why: the user iterates on visuals faster through Android Studio's Preview pane than through dialogue. Every public composable should be visually accessible without launching the app.

## Cardinal rule — Previews live IN the same file as the composable

`@Preview` functions for a composable MUST be declared in the same `.kt` file that defines it. **No separate `XxxScreenPreview.kt` files.** Clicking a preview thumbnail jumps to the file containing the `@Preview`; if it lives in a sibling file, you still have to open the real Screen to edit. Inline previews collapse this to one click.

Sibling `XxxPreviewData.kt` files for **fake-state factories** are allowed and encouraged. They are reused by composition tests in `androidHostTest`. Only the `@Preview`-annotated functions live in the composable's own file.

## Region marker — Previews go to the bottom of the file, fenced

Every preview block is wrapped in a region marker so the IDE collapses it and `cat <file>` skips past it:

```kotlin
// region Previews

@Preview
@Composable
private fun ProductCardPreview_Default() {
  NutriSportPreview {
    ProductCard(/* ... */)
  }
}

// endregion
```

The exact strings `// region Previews` and `// endregion` are load-bearing for any future file-size tooling. Do not reuse the name `Previews` for non-preview regions.

## Where Previews are MANDATORY

1. **All public composables in `shared/ui/src/commonMain/kotlin/com/nutrisport/shared/component/`** — at least one `@Preview` per public composable, in a `// region Previews` block at the bottom of that same file.
2. **Every `XxxScreen.kt` in a `feature/*` module** — at least one `@Preview` per Screen, inline.
3. **Public reusable feature composables** (e.g. a shared card used by two screens) — at least one `@Preview` in the same file.

## Where Previews are FORBIDDEN

- **Separate `XxxScreenPreview.kt` files** — see Cardinal rule.
- `XxxRoute.kt` — Route holds DI (`koinViewModel()`); preview rendering breaks on missing Application context. Previews for the visual surface go inline in `XxxScreen.kt`.
- Internal / private helpers inside a Screen — covered transitively by the parent Screen preview.

## Pattern — reuse the helper, never inline `MaterialTheme {}`

`NutriSportPreview` lives in `shared/ui/src/commonMain/kotlin/com/nutrisport/shared/preview/Previews.kt` and wraps `MaterialTheme { Surface { Box(padding) { ... } } }`. Always use it.

```kotlin
@Preview
@Composable
private fun PrimaryButtonPreview_Enabled() {
  NutriSportPreview {
    PrimaryButton(text = "Add to cart", onClick = {}, enabled = true)
  }
}

@Preview
@Composable
private fun PrimaryButtonPreview_Disabled() {
  NutriSportPreview {
    PrimaryButton(text = "Add to cart", onClick = {}, enabled = false)
  }
}
```

## Variant policy

- **Design-system component** (`shared/ui/component/*`): cover every `enum` / sealed variant, enabled vs disabled, selected vs unselected, and every semantically distinct state (`Loading`, `Empty`, `Error`).
- **Screen**: at least one Content preview; add Loading / Error / Empty if the UI state exposes them.

Dark-theme previews are **deferred** until the dark theme lands (roadmap: "Material 3 Dynamic Color, dark theme"). `NutriSportPreview` will gain a `darkTheme` parameter at that point; until then, light-only.

## Naming

`private fun {Component}Preview_{Variant}()`. One preview function per variant. Do NOT collapse variants into a single function with `for` loops or parameter providers.

## Forbidden inside a preview body

- `koinInject()`, `koinViewModel()` — DI breaks rendering.
- Navigation calls.
- `LaunchedEffect` reading DI / network / DataStore.
- `kotlin.random.Random` or anything non-deterministic — preview rendering must be reproducible.

## Fake state

- **Component previews** — inline literals inside the preview function (5-10 lines is fine).
- **Screen previews** — keep fake-state factories in a sibling `XxxPreviewData.kt`; composition tests reuse them. The preview calls `fakeXxxStateContent()` from that file.

## File-level suppress

Add `@file:Suppress("UnusedPrivateMember")` at the top of any file hosting previews. Detekt does not realize the IDE Preview pane consumes the function and would flag it as dead code.

## Related

- [conventions.md](conventions.md) — Compose conventions (Route vs Screen separation).
- [testing.md](testing.md) — composition tests reuse the same fake-state factories.
- `shared/ui/src/commonMain/kotlin/com/nutrisport/shared/preview/Previews.kt` — the `NutriSportPreview` helper (do not reimplement).
