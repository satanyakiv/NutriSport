# Performance

NutriSport uses Baseline Profiles for AOT compilation of hot paths, reducing cold startup time and improving scroll/navigation jank. A dedicated `:benchmark` module generates profiles against real Compose UI using fake data — no Firebase dependency, fully deterministic and offline.

Compose stability optimizations (`@Immutable`, `ImmutableList`) are planned but not yet implemented.

## Performance Stack

| Tool                            | Version       | Purpose                                       |
| ------------------------------- | ------------- | --------------------------------------------- |
| Baseline Profile Plugin         | 1.5.0-alpha04 | Gradle plugin for profile generation          |
| `benchmark-macro-junit4`        | 1.5.0-alpha04 | Macrobenchmark test runner                    |
| UIAutomator                     | 2.3.0         | UI interaction during profile generation      |
| ProfileInstaller                | 1.4.1         | Installs baseline profile on first app launch |
| `kotlinx-collections-immutable` | —             | Immutable collections for Compose (planned)   |

## How It Works

```
:benchmark module
  │
  │  targetProjectPath = ":androidApp"
  │  build type = "benchmark" (inherits release + debug signing)
  │
  ▼
:androidApp (benchmark build type)
  │
  │  BuildConfig.USE_FAKE_DATA = true
  │
  ▼
NutrisportApplication.onCreate()
  │
  ├─ initializeKoin(useFakeData = true)
  │    └─ fakeNetworkModule (6 products, 1 customer, 2 cart items)
  │       replaces: networkModule + databaseModule
  │
  ├─ Firebase.initialize() — SKIPPED (USE_FAKE_DATA = true)
  │
  ▼
Real Compose UI with fake data
  │
  ▼
BaselineProfileRule.collect() / MacrobenchmarkRule.measureRepeated()
  │
  ▼
baseline-prof.txt (generated in androidApp/src/release/generated/)
```

**Why fake data:** the benchmark build type sets `USE_FAKE_DATA=true`, which swaps real Firebase repositories for in-memory fakes from `:shared:testing`. This ensures deterministic results, no network dependency, and exercises the same Compose UI code paths that real users hit.

## Module Structure

```
benchmark/
  build.gradle.kts                          — com.android.test + baselineprofile plugin
  src/main/kotlin/.../benchmark/
    BaselineProfileGenerator.kt             — UI journeys for profile collection
    StartupBenchmarks.kt                    — cold startup timing (None vs Partial compilation)

di/src/commonMain/kotlin/.../di/
  FakeNetworkModule.kt                      — Koin module: 4 fake repos, 6 products, 1 customer
  KoinModule.kt                             — useFakeData flag routes to fake or real modules

shared/testing/src/commonMain/kotlin/.../test/
  FakeAdminRepository.kt                    — AdminRepository stub (created for benchmark)

androidApp/
  src/release/generated/baselineProfiles/
    baseline-prof.txt                       — generated profile rules (~1.9 MB)
```

## Benchmark Journeys

### BaselineProfileGenerator

Collects method/class traces for AOT compilation:

| Step           | Actions                                              | Why                                      |
| -------------- | ---------------------------------------------------- | ---------------------------------------- |
| Cold start     | `pressHome()` → `startActivityAndWait()`             | Profile app initialization + first frame |
| Home scroll    | Scroll down 2x → scroll up 2x                        | Profile LazyColumn/LazyGrid rendering    |
| Product detail | Tap first product → wait for load → press back       | Profile navigation + detail screen       |
| Bottom nav     | Iterate all tabs via `bottom_navigation` resource ID | Profile tab switching + screen creation  |

### StartupBenchmarks

Measures cold startup time with `StartupTimingMetric`:

| Test                        | CompilationMode | Iterations | Purpose                                |
| --------------------------- | --------------- | ---------- | -------------------------------------- |
| `startupNoCompilation`      | `None()`        | 5          | Worst case — no AOT, fully interpreted |
| `startupPartialCompilation` | `Partial()`     | 5          | With baseline profile applied          |

Both use `StartupMode.COLD` with `pressHome()` in setup block.

## Running

```bash
# Generate baseline profile (requires connected device/emulator, API 28+)
./gradlew :androidApp:generateBaselineProfile

# Run startup benchmarks (requires connected device/emulator)
./gradlew :benchmark:connectedBenchmarkAndroidTest

# Build benchmark APK (for manual testing)
./gradlew :androidApp:assembleBenchmark
```

> **Note:** all benchmark commands require a physical device or emulator. They are not part of CI — see "Not Covered" section.

## Build Types

| Build type  | Base      | Signing | Debuggable | `USE_FAKE_DATA` | Firebase | R8 (minify) |
| ----------- | --------- | ------- | ---------- | --------------- | -------- | ----------- |
| `debug`     | —         | debug   | yes        | `false`         | yes      | no          |
| `release`   | —         | release | no         | `false`         | yes      | yes         |
| `benchmark` | `release` | debug   | no         | **`true`**      | **no**   | yes         |

The `benchmark` type inherits R8/shrinkResources from `release` but uses debug signing (required by `BaselineProfileRule`) and enables fake data to eliminate Firebase as a variable.

## Compose Stability (Planned)

**Status:** NOT STARTED — tracked in `.claude/features/11-performance.md`

### What will change

1. **Domain models** — add `@Immutable` to `Product`, `Customer`, `CartItem`, `Order`
2. **UI state** — add `@Immutable` to `UiState` sealed class
3. **UI models** — add `@Immutable` to all `*Ui` data classes in feature modules
4. **Screen parameters** — `List<T>` → `ImmutableList<T>` for composable params
5. **ViewModels** — `.toImmutableList()` when emitting state
6. **Compose compiler reports** — enable `reportsDestination` in convention plugin for stability verification

### Dependencies to add

```toml
# gradle/libs.versions.toml
kotlinx-collections-immutable = "0.3.8"

# Convention plugin (nutrisport.kmp.library)
implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:$version")
```

### Verification (when implemented)

```bash
# Check compose compiler reports for unstable classes
./gradlew assembleRelease -PcomposeCompilerReports=true
# Inspect build/{module}/compose_reports/ for "unstable" markers

# Compile check after annotations
./gradlew :domain:compileCommonMainKotlinMetadata
./gradlew assembleDebug
```

## Not Covered

- **Warm/Hot startup benchmarks** — cold start is the primary metric; warm/hot show diminishing returns
- **Frame timing / jank metrics** — fake data set (6 products) is too small for meaningful scroll perf data
- **iOS profiling** — no Baseline Profile equivalent on iOS; Instruments profiling is manual-only
- **CI-integrated benchmarks** — requires a physical device or emulator with API 28+; not available in GitHub Actions runners
- **Compose compiler reports in CI** — will be added alongside Compose stability work (plan 11)

## Related

- [Testing Guide](TESTING.md) — test stack, coverage, running tests
- [CI/CD](CI.md) — workflow pipelines, build configurations
- [Plan 11 — Performance Optimization](../.claude/features/11-performance.md) — remaining Compose stability tasks
