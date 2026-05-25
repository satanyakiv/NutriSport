# Article summary — How to split Compose screens before they become unmanageable

Source: <https://androidmeda.medium.com/how-to-split-compose-screens-before-they-become-unmanageable-67d61465da56>
Author: Android Meda (Medium).

This file is a faithful, offline-safe distillation. The skill consults this instead of fetching the article on every invocation. If the article changes materially, update both this file and any contradicted SKILL.md heuristics.

## 1. Signs a file needs splitting

Five warning indicators. Any one of them is reason to reach for the split tools:

- **Line count threshold**. Files reaching 600-800 lines become hard to read end-to-end and hard to review in PRs. (NutriSport tunes this lower — see SKILL.md "Detection thresholds".)
- **Nesting depth**. _"Three or four levels of nested composables inside a single function is a signal that the function is doing too much."_
- **Multiple UI concerns coexisting**. Six distinct concerns in one file (sections, dialogs, bottom sheets, loading states, headers, list items) means the file is responsible for too many things at once.
- **Code review friction**. Diffs are too large for reviewers to load all context at once; PRs slow down or get rubber-stamped.
- **Preview degradation**. Setting up `@Preview` becomes painful because the composable depends on too much surrounding context — so previews stop being maintained.

## 2. Core splitting strategies

### The Screen / Content split

The single most important architectural decision the article makes is separating two layers:

- **Screen composable** — connects ViewModel to UI. Collects state, hands navigation events back to the ViewModel. Makes no rendering decisions itself.
- **Content composable** — receives state as parameters, renders UI. No ViewModel, no side effects, no DI.

This makes the Content trivially previewable: a Preview just builds a state object and hands it in. No Koin, no LaunchedEffect, no coroutines.

NutriSport implements an even cleaner version: Route↔Screen separation per [`conventions.md`](../../../rules/conventions.md). Route = DI host, Screen = pure `(state, onAction) -> Unit`. The article's Screen/Content split corresponds exactly to the project's Route/Screen split — same idea, project-specific names.

### Four extraction criteria

When deciding whether to break a chunk of UI out into its own composable:

1. **Distinct responsibility.** If the chunk is _"a header, a list, a summary card, a form section"_, it deserves its own name and its own composable.
2. **Nesting exceeds manageable depth.** If extracting a chunk drops the parent's max nesting from 5 to 3, do it.
3. **The same pattern appears multiple times.** Two occurrences is the trigger; don't wait for three.
4. **Don't over-extract.** _"A two-line composable that is only used once... does not need to be its own function."_ Single-use, tiny composables stay inline.

## 3. File organization

### Small-to-medium screens

```
order/
    OrderScreen.kt          // Screen and content composables
    OrderComponents.kt      // Screen-specific sub-composables
    OrderUiState.kt         // UI state models
    OrderViewModel.kt       // ViewModel
```

(NutriSport deviates here: instead of `OrderComponents.kt`, the project uses one file per public composable inside a sibling `component/` directory — see SKILL.md "Forbidden filenames" and the `feature/home/cart/.../component/CartItemCard.kt` precedent.)

### Complex features

```
order/
    list/
        OrderListScreen.kt
        OrderListComponents.kt
        OrderListViewModel.kt
    detail/
        OrderDetailScreen.kt
        OrderDetailComponents.kt
        OrderDetailViewModel.kt
    shared/
        OrderCard.kt
        OrderStatusBadge.kt
```

(NutriSport's equivalent: per-screen subdirectory inside the feature, plus `:core:<name>` for the cross-screen shared layer instead of a `shared/` folder.)

### Genuinely-reusable shared UI

```
ui/
    components/
        LoadingIndicator.kt
        ErrorState.kt
        PrimaryButton.kt
    theme/
        Theme.kt
        Typography.kt
        Colors.kt
```

(NutriSport's equivalent: `:shared:ui/.../component/`, `:shared:ui/.../theme/`. Same idea, different module name.)

## 4. Naming conventions

- **Use specific file names.** `OrderComponents.kt` is OK because it clearly contains "components for the Order feature." Avoid vague terms — `OrderUtils.kt`, `OrderHelpers.kt`, `OrderParts.kt` say nothing.
- **Composable names describe what they do.** _"You can give a composable a name that describes what it does"_ is one of the four extraction criteria.
- **Screen-specific composables indicate their scope through the name.** `OrderHeader` is clearly part of the Order feature.
- **Generic reusable components have no feature-specific terminology.** `PrimaryButton`, not `OrderPrimaryButton`. The moment a "shared" component starts taking a feature-specific parameter (`order: OrderUi`), it's not shared anymore.

## 5. Threshold numbers

- **Line count**: 600-800 lines triggers manageability concerns.
- **Nesting depth**: 3-4 levels signals need for flattening.
- **Duplication frequency**: Extract immediately when the pattern appears twice.
- **No minimum threshold for extraction**: single-use composables are acceptable if genuinely distinct.

## 6. Before / after example

### Before — monolithic

```kotlin
@Composable
fun OrderScreenContent(uiState: OrderUiState, ...) {
    // 600+ lines: header, list, summary, button all inline
    Column { ... }
}
```

### After — split responsibly

```kotlin
@Composable
fun OrderSuccessContent(...) {
    Column(modifier = Modifier.fillMaxSize()) {
        OrderHeader(...)
        OrderItemList(...)
        OrderSummary(...)
        SubmitButton(...)
    }
}
```

The refactored version reads _"like a description of the screen layout."_ That's the article's success criterion: the top-level composable should read as an outline of what the screen contains, not as the implementation of every detail.

## 7. Anti-patterns to avoid

1. **Feature creep in shared components.** _"A component that starts taking feature-specific parameters... is no longer a shared component."_ If `PrimaryButton` grows an `order: OrderUi` parameter to render an order-specific label, it stopped being a button.
2. **Over-extraction.** _"Twenty tiny single-use functions [are] harder to follow than one medium-sized function."_ The cure is worse than the disease when every `Spacer` lives in its own file.
3. **Mixed concerns.** Putting feature-specific logic inside genuinely-generic UI components — same anti-pattern as #1, framed differently.
4. **Inadequate previews.** Skipping `@Preview` maintenance because setup became too complex. The fix is to make Content composables Preview-friendly (no DI, no ViewModel) — not to abandon Previews.
5. **Copy-paste reuse.** Duplicating a pattern across screens because it's buried inside a screen-specific file. The article: _"if a section recurs, give it a name and put it where both screens can find it."_

## Closing principle

> _"Good structure is just what gets you there."_

The author is explicit that the goal is maintainability and developer velocity, not structure for its own sake. Don't split a 280-line Screen because someone said the threshold is 250. Do split a 320-line Screen with five repeated card patterns because the structure is hurting you, even though the line count is "fine." Judgment over arithmetic.
