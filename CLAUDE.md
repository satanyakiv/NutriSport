# NutriSport

KMP project (Android + iOS) with Compose Multiplatform.

## Rules

- [Architecture](.claude/rules/architecture.md) — module structure, dependency flow, clean architecture rules
- [Error Handling](.claude/rules/error-handling.md) — Either, AppError, DomainResult, UiState patterns
- [Models & Use Cases](.claude/rules/models.md) — naming table, mapper rules, use case patterns
- [Testing](.claude/rules/testing.md) — test stack, AAA pattern, coverage targets, Kover
- [Conventions](.claude/rules/conventions.md) — code style, Compose, coroutines, Git
- [Prompts](.claude/rules/prompts.md) — string resources and constants
- [Plan Mode](.claude/rules/plan-mode.md) — feature plan files, status markers, orchestration
- [Documentation](.claude/rules/docs.md) — docs/ file style, structure, formatting conventions

## Commands

- `/fix <bug>` — TDD bug fixing (Red-Green-Refactor)
- `/refactor <scope>` — safe refactoring with test-first approach
- `/clean-arch <module>` — Clean Architecture compliance check
- `/debug-deps <error>` — dependency/build crash debugger (**USE THIS for any build/compile error**)
- `/security-audit` — OWASP Mobile Security audit (secrets, auth, encryption, permissions)
- `/debug-crash <dump.json>` — analyze Tracey crash dump, correlate with code, suggest fix

## Skills

- `/gen-test <class>` — generate tests following AAA/Turbine/Mokkery conventions
- `/new-feature <name>` — scaffold feature module with full boilerplate
- `/kover-analyze [module]` — coverage analysis and prioritized recommendations
- `/orchestrate-features <cmd>` — parallel feature development orchestration
- `/replay-session <dump.json>` — reconstruct user journey from Tracey dump, identify failure point

## Quick Reference

### Layers

- **Domain** (`:domain`) — models (no suffix), repository interfaces, use cases, `Either`/`DomainResult`/`AppError`, `NullSafety`
- **Utilities** (`:shared:utils`) — Constants, AppConfig, FormatPrice, Log, Screen.kt (navigation routes)
- **Shared UI** (`:shared:ui`) — composable components, `UiState`, `DisplayResult`, resources, colors, fonts
- **Data** (`:network`) — DTOs (`Dto` suffix), mappers (`.toDomain()`), repository impls
- **Test Fixtures** (`:shared:testing`) — fake data factories, fake repositories
- **Presentation** (`:feature/*`) — UI models (`Ui` suffix), mappers (`.toUi()`), ViewModels

### Error Handling

- **Domain/Data**: `DomainResult<T>` = `Either<AppError, T>` — type-safe errors
- **Presentation**: `UiState<T>` — `Idle`, `Loading`, `Content(DomainResult<T>)`
- **AppError**: `Network`, `NotFound`, `Unauthorized`, `Unknown`

### Key Commands

```bash
./gradlew :{module}:allTests --tests "*TestClass"  # run specific test
./gradlew :{module}:compileCommonMainKotlinMetadata # quick compile check (common)
./gradlew assembleDebug                             # full Android debug build
./gradlew :composeApp:compileIosMainKotlinMetadata  # iOS compile check
./gradlew koverHtmlReport                           # coverage report
./gradlew koverVerify                               # check thresholds
./gradlew detekt                                     # code style check (also runs pre-commit)
```

### Screen Pattern (Route-Screen)

- **Route**: `{Feature}Route` — DI (koinViewModel), collectAsState, passes to Screen
- **Screen**: `{Feature}Screen` — pure UI, receives state + callbacks, no DI
- Navigation → Route → Screen

### Convention Plugins

- `nutrisport.kmp.library` — base KMP + Compose + compose.uiTest + Robolectric
- `nutrisport.kmp.feature` — + Koin + messagebar
- `nutrisport.kmp.feature.full` — + Coil + navigation + Ktor

### Debug Tooling (Strategy Pattern)

- **DebugToolkit** interface in `:navigation` — polymorphic debug behavior
- **NoOpDebugToolkit** — release/iOS default (no-op)
- **TraceyDebugToolkit** — debug: Tracey session recording (`androidApp/src/debug/`)
- **DebugModuleProvider** — Koin modules per build type (`src/debug/` vs `src/release/`)
- Add new debug tools → only modify `androidApp/src/debug/`, zero changes in common

### Build Gotchas

- CMP Compose deps: use `ComposeExtension.dependencies` accessor, NOT direct Maven coordinates
- Room KMP: `expect object` must declare `override fun initialize()` + `@Suppress("KotlinNoActualForExpect")`
- `androidLibrary {}` deprecated in AGP 9.1+ → use `android {}` inside `kotlin {}`
- KLIB resolver duplicate warnings (AndroidX vs JetBrains fork) — unfixable, ignore
- UI tests: `androidHostTest` + Robolectric (not `commonTest`) — CMP compose.uiTest needs Android context
- Never hardcode `Dispatchers.*` — inject `CoroutineDispatcherProvider`. Hardcoded dispatchers cause flaky tests
- Hooks: PreToolUse blocks edits to `google-services.json`, `local.properties`, `*.keystore`
- Debug deps: never `implementation(libs.tracey)` in KMP modules — use `debugImplementation` in androidApp + `DebugToolkit`
- `DebugModuleProvider` must exist in BOTH `src/debug/` AND `src/release/` (same class, different impl)

## References

- [Testing Patterns](.claude/references/testing-patterns.md) — Fake repos, test recipes, pitfalls
