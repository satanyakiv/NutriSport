# Code Conventions

## Kotlin Style

- `jvmToolchain(21)` — Java 21 target everywhere
- Explicit return types on public API
- `internal` by default, `public` only when needed across modules
- No wildcard imports
- Trailing commas in multiline declarations
- Use `orZero()` / `.orEmpty()` instead of `?: 0.0` / `?: 0` / `?: ""` (see `shared/utils/.../util/NullSafety.kt`)

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
- Use `Dispatchers.IO` only in data layer, never in ViewModels

## Koin

- Use `viewModelOf(::ClassName)` shorthand
- Use `single<Interface> { Impl() }` for repositories
- Platform modules via `expect val targetModule: Module`
- Never inject in composables directly — pass via ViewModel

## Firebase

- All Firebase calls in `data` module only
- Wrap Firebase exceptions into `Either.Left(AppError.Network(...))`
- Collection names as constants in companion objects
- Never expose Firebase models outside `data` — map to domain models

## Git

- Branch naming: `feature/xxx`, `fix/xxx`, `refactor/xxx`
- Commit messages: imperative mood, max 72 chars first line
- One logical change per commit

## Module build.gradle.kts

- Always use convention plugin — no manual plugin application
- Only declare module-specific dependencies
- compileSdk/minSdk stay in each module (AGP limitation in precompiled plugins)
- Modules with `composeResources/` need `android { androidResources.enable = true }` (CMP-9547; `androidLibrary {}` deprecated in AGP 9.1+)

## Error Handling

- Use `DomainResult<T>` (`Either<AppError, T>`) in domain/data layers
- Use `UiState<T>` for async operations exposed to UI
- Use `Either.fold()` for branching on success/error
- Never swallow exceptions silently
- Log errors before wrapping into `Either.Left(AppError.*)`
- No callbacks (`onSuccess`/`onError`) in repository interfaces — return `DomainResult`
- When searching for third-party KMP libraries → first check https://github.com/terrakok/kmp-awesome
- Every refactoring must update `.claude/rules/` files if architecture changes
