# 11 — Performance Optimization

Status: IN_PROGRESS
Group: D (sequence: 11→12)
Depends on: none

## Context

No performance optimizations applied. Baseline Profiles speed up startup, `@Immutable`/`@Stable` annotations + `kotlinx-collections-immutable` reduce unnecessary recompositions. Important for portfolio — shows production-level thinking.

## Files to Create

- [x] `benchmark/.../BaselineProfileGenerator.kt` — macrobenchmark baseline profile generator (created in `:benchmark` module)
- [x] `benchmark/.../StartupBenchmarks.kt` — startup timing benchmarks (cold/warm/hot)
- [x] `androidApp/src/release/generated/baselineProfiles/baseline-prof.txt` — generated baseline profile rules (1.9 MB, real device)
- [x] `di/.../FakeNetworkModule.kt` — Koin module with fake repos for benchmark build type
- [x] `shared/testing/.../FakeAdminRepository.kt` — fake AdminRepository

## Files to Modify

### Build Config (Baseline Profile infra)

- [x] `androidApp/build.gradle.kts` — benchmark build type + baselineprofile plugin
- [x] `build.gradle.kts` + `settings.gradle.kts` — `:benchmark` module registration
- [x] `gradle/libs.versions.toml` — profileinstaller, benchmark-macro, uiautomator
- [x] `di/KoinModule.kt` — `useFakeData` param, conditional fake/real modules
- [x] `NutrisportApplication.kt` — conditional Firebase init

### Convention Plugins (Compose stability) — NOT STARTED

- [ ] `build-logic/convention/.../KmpLibraryPlugin.kt` — add compose compiler stability config

### State Classes (add @Immutable / @Stable) — NOT STARTED

- [ ] `domain/.../domain/Product.kt` — `@Immutable`
- [ ] `domain/.../domain/Customer.kt` — `@Immutable`
- [ ] `domain/.../domain/CartItem.kt` — `@Immutable`
- [ ] `domain/.../domain/Order.kt` — `@Immutable`
- [ ] `shared/ui/.../util/UiState.kt` — `@Immutable` on sealed class

### UI Model Classes — NOT STARTED

- [ ] `feature/*/model/*Ui.kt` — `@Immutable` on all UI models

### Screen Composables (use ImmutableList) — NOT STARTED

- [ ] `feature/home/.../HomeScreen.kt` — `ImmutableList<ProductUi>` in params
- [ ] `feature/cart/.../CartScreen.kt` — `ImmutableList<CartItemUi>` in params
- [ ] ViewModels — `.toImmutableList()` when emitting state

### Compose Compiler Reports — NOT STARTED

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
./gradlew :domain:compileCommonMainKotlinMetadata
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
