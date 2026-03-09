# 11 — Performance Optimization

Status: IDLE
Group: D (sequence: 11→12)
Depends on: none

## Context

No performance optimizations applied. Baseline Profiles speed up startup, `@Immutable`/`@Stable` annotations + `kotlinx-collections-immutable` reduce unnecessary recompositions. Important for portfolio — shows production-level thinking.

## Files to Create

- [ ] `androidApp/.../BaselineProfileGenerator.kt` — (androidTest) baseline profile generator
- [ ] `androidApp/src/main/baseline-prof.txt` — generated baseline profile rules

## Files to Modify

### Convention Plugins (Compose stability)
- [ ] `build-logic/convention/.../KmpLibraryPlugin.kt` — add compose compiler stability config

### State Classes (add @Immutable / @Stable)
- [ ] `shared/utils/.../domain/Product.kt` — `@Immutable`
- [ ] `shared/utils/.../domain/Customer.kt` — `@Immutable`
- [ ] `shared/utils/.../domain/CartItem.kt` — `@Immutable`
- [ ] `shared/utils/.../domain/Order.kt` — `@Immutable`
- [ ] `shared/ui/.../util/UiState.kt` — `@Immutable` on sealed class

### UI Model Classes
- [ ] `feature/*/model/*Ui.kt` — `@Immutable` on all UI models

### Screen Composables (use ImmutableList)
- [ ] `feature/home/.../HomeScreen.kt` — `ImmutableList<ProductUi>` in params
- [ ] `feature/cart/.../CartScreen.kt` — `ImmutableList<CartItemUi>` in params
- [ ] ViewModels — `.toImmutableList()` when emitting state

### Compose Compiler Reports
- [ ] `build-logic/convention/` — enable compose compiler metrics/reports in debug

## Dependencies (libs)

- `org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8` — immutable collections (official JetBrains, on kmp-awesome)

## Implementation Steps

1. Add `kotlinx-collections-immutable` to version catalog
2. Add dependency to convention plugin (available in all modules)
3. Annotate domain models with `@Immutable`:
   - All `data class` domain models that are used in Compose
   - `UiState` sealed class
4. Annotate UI models with `@Immutable`
5. Update Screen composable parameters:
   - `List<T>` → `ImmutableList<T>` for composable params
   - Add `.toImmutableList()` in ViewModels when collecting
6. Enable Compose compiler stability reports:
   - Add `composeCompiler { reportsDestination = layout.buildDirectory.dir("compose_reports") }` in convention plugin
7. Generate Baseline Profile:
   - Create `BaselineProfileGenerator` in androidTest
   - Critical user journeys: app start → home → product detail → cart
   - Generate `baseline-prof.txt`

## Verification

```bash
# Compile check
./gradlew :shared:utils:compileCommonMainKotlinMetadata
./gradlew assembleDebug

# Compose compiler reports (check for unstable classes)
./gradlew assembleRelease -PcomposeCompilerReports=true
# Check build/{module}/compose_reports/

# Baseline profile (requires connected device)
# ./gradlew :androidApp:generateBaselineProfile
```

## Conflict Zones

- Convention plugins — unique to this feature
- `libs.versions.toml` — also modified by 04, 05, 10
- Domain models — annotation-only changes, low conflict risk
