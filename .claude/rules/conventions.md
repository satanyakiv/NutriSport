# Code Conventions

## Kotlin Style

- `jvmToolchain(21)` — Java 21 target everywhere
- Explicit return types on public API
- `internal` by default, `public` only when needed across modules
- No wildcard imports
- Trailing commas in multiline declarations
- Use `orZero()` / `.orEmpty()` instead of `?: 0.0` / `?: 0` / `?: ""` (see `domain/.../util/NullSafety.kt`)

## Compose

- Composable functions: PascalCase (`ProductCard`, `CartScreen`)
- State hoisting: Screen composable receives state + event callbacks
- Preview functions: `@Preview` + `{Component}Preview` naming
- No business logic in composables — delegate to ViewModel
- Domain→UI mapping happens in ViewModel, never in Composables (Screens receive `UiState<XxxUi>`)
- **Route-Screen separation**: Route handles DI + state collection, Screen is pure UI
- Route naming: `{Feature}Route`, Screen naming: `{Feature}Screen`
- Navigation calls Route, never Screen directly
- Simple screens without ViewModel — skip Route, use Screen directly
- New features → Route-Screen. Existing → migrate during refactoring

## Coroutines

- ViewModels use `viewModelScope`
- Repositories return `Flow<T>` (cold streams)
- One-shot operations return `suspend fun`
- Never use `GlobalScope`
- Never hardcode `Dispatchers.*` — inject `CoroutineDispatcherProvider` via constructor
- `CoroutineDispatcherProvider` interface in `:domain`, `DefaultCoroutineDispatcherProvider` for production
- In tests: `TestCoroutineDispatcherProvider(testDispatcher)` from `:shared:testing`

## Koin

- Use `viewModelOf(::ClassName)` shorthand
- Use `single<Interface> { Impl() }` for repositories
- Platform modules via `expect val targetModule: Module`
- Never inject in composables directly — pass via ViewModel

## Firebase

- All Firebase calls in `network` module only
- Wrap Firebase exceptions into `Either.Left(AppError.Network(...))`
- Collection names as constants in companion objects
- Never expose Firebase models outside `network` — map to domain models

## Git

- Branch naming: `feature/xxx`, `fix/xxx`, `refactor/xxx`
- Commit messages: imperative mood, max 72 chars first line
- One logical change per commit

## Module build.gradle.kts

- Always use convention plugin — no manual plugin application
- Only declare module-specific dependencies
- compileSdk/minSdk stay in each module (AGP limitation in precompiled plugins)
- Modules with `composeResources/` need `android { androidResources.enable = true }` (CMP-9547; `androidLibrary {}` deprecated in AGP 9.1+)

## Build-Type Dependencies (Strategy Pattern)

**Rule: use polymorphism instead of `if/else` for build-type-specific behavior.** No `if (isDebug)`, `if (USE_FAKE_DATA)`, or `BuildConfig.*` checks in `Application.onCreate()` — delegate to Strategy implementations per build type.

### Pattern

1. **Interface** in `androidApp/src/main/` (Android-only) or `:navigation/commonMain/` (KMP) — defines contract
2. **NoOp implementation** alongside interface — default for builds that don't need the feature
3. **Build-type implementations** in `androidApp/src/{debug,release,benchmark}/` — concrete behavior
4. **Wiring** via `DebugModuleProvider` (exists in each source set) — registers impl in Koin
5. **Usage** in `Application.onCreate()` — `getKoin().get<Interface>().method()` — polymorphic call

### Existing Strategies

| Interface              | Debug                     | Release                     | Benchmark                | Location               |
| ---------------------- | ------------------------- | --------------------------- | ------------------------ | ---------------------- |
| `DebugToolkit`         | TraceyDebugToolkit        | NoOpDebugToolkit            | NoOpDebugToolkit         | `:navigation` (KMP)    |
| `FirebaseConfigurator` | DebugFirebaseConfigurator | ReleaseFirebaseConfigurator | NoOpFirebaseConfigurator | `androidApp/src/main/` |

### Rules

- Debug-only libraries (Tracey, LeakCanary, etc.) — `debugImplementation` in `androidApp` only
- Never import debug libraries in `commonMain` — use Strategy interface
- Build-type source sets: `src/debug/`, `src/release/`, `src/benchmark/` for variant-specific code
- `DebugModuleProvider` in each source set — Koin modules per build type
- `initializeKoin(additionalModules = DebugModuleProvider.modules)` — wires DI
- Adding new build-type behavior → create new Strategy, add to `DebugModuleProvider`, call polymorphically
- Koin `getOrNull<T>()` for optional dependencies (e.g., `FirebaseAnalyticsProcessor` absent in benchmark)

## Error Handling

- Use `DomainResult<T>` (`Either<AppError, T>`) in domain/data layers
- Use `UiState<T>` for async operations exposed to UI
- Use `Either.fold()` for branching on success/error
- Never swallow exceptions silently
- Log errors before wrapping into `Either.Left(AppError.*)`
- No callbacks (`onSuccess`/`onError`) in repository interfaces — return `DomainResult`
- When searching for third-party KMP libraries → first check https://github.com/terrakok/kmp-awesome
- Every refactoring must update `.claude/rules/` files if architecture changes

## Build Gotchas

### Gradle / AGP

- Kover #772: `merge { allProjects() }` causes `variantName null` crash with `withHostTest {}` — apply Kover in convention plugin AFTER android, use `dependencies { kover() }` in root
- `ComposeExtension` is project-level, NOT nested inside `KotlinMultiplatformExtension`
- `iosMain` source set: use `sourceSets.maybeCreate("iosMain")` in plugins
- Compose deps in convention plugin: use `ComposeExtension.dependencies` accessor (NOT direct Maven coords)
- KSP is decoupled from Kotlin version — use latest KSP with current Kotlin
- `detekt` extension in root build.gradle.kts needs `import io.gitlab.arturbosch.detekt.extensions.DetektExtension`
- Detekt `maxIssues: -1` (report only) — existing codebase has violations
- KLIB resolver duplicate warnings (AndroidX vs JetBrains fork) — unfixable, ignore

### Firebase

- `firebase-common` in androidApp is USED (Firebase.initialize) — don't remove
- Crashlytics plugin: do NOT declare in root `build.gradle.kts` with `apply false` — only apply in `androidApp/build.gradle.kts`
- Crashlytics plugin 2.9.x uses `applicationVariants` removed in AGP 9.1+ — use 3.0.6+

### Room KMP

- `:database` uses `com.android.kotlin.multiplatform.library` via convention plugin
- Room KMP constructor: `expect object` MUST declare `override fun initialize()` + `@Suppress("KotlinNoActualForExpect")`

### Module-specific

- `feature:adminPanel` needs `firebase-storage` + BOM directly
- `:di` module needs `room-runtime` + `sqlite-bundled` deps for NutriSportDatabase access
- `androidApp` needs explicit `implementation(project(":shared:utils"))` for AppConfig
- Debug build has `applicationIdSuffix = ".debug"` — google-services.json must match
- Compose Resources in shared:ui generate package `nutrisport.shared.ui.generated.resources`

### Claude Code

- Hooks: PreToolUse blocks edits to `google-services.json`, `local.properties`, `*.keystore`
