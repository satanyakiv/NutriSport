# Architecture Rules

## Project Structure

NutriSport is a KMP (Kotlin Multiplatform) project targeting Android + iOS.

### Module Hierarchy

```
androidApp          — Android entry point (Activity, Application)
composeApp          — Shared KMP UI entry (AppContent)
navigation          — NavHost + type-safe routing (SetupNavGraph)
domain              — Pure domain layer: models, repo interfaces, use cases, Either, AppError, NullSafety
shared/utils        — App utilities: Constants, AppConfig, FormatPrice, Log, Screen.kt (routes)
shared/ui           — Shared UI: composable components, UiState, DisplayResult, resources
shared/testing      — Test fixtures: fake data factories, fake repositories
network             — Data layer: Firebase repos, DTOs, mappers to domain
database            — Room KMP: entities, DAOs, local cache
di                  — Koin DI configuration (sharedModule + platform modules)
analytics           — Abstract analytics tracking (CompositeTracker)
feature/*           — Presentation layer: ViewModels, Screens, UI models
build-logic/        — Convention plugins (library, feature, feature.full)
```

### Dependency Flow (top → bottom, never upward)

```
androidApp / iosApp
  └─ composeApp
       ├─ navigation (depends on all feature modules + :domain)
       ├─ di (depends on all feature modules + :network + :database + :domain)
       ├─ domain (pure — no project deps)
       ├─ shared:utils (no project deps)
       ├─ shared:ui (depends on :domain + :shared:utils)
       └─ network (depends on :domain + :database)

feature modules → :domain + :shared:utils + :shared:ui (never depend on each other)
network → :domain + :database (never :shared:ui)
shared:testing → :domain
di → :domain + :network + :database + all features
navigation → :domain + features
analytics → :domain only
```

### Clean Architecture Layers

| Layer         | Module            | Contains                                                                             |
| ------------- | ----------------- | ------------------------------------------------------------------------------------ |
| Domain        | `:domain`         | Models, repo interfaces, use cases, `Either`/`AppError`/`DomainResult`, `NullSafety` |
| Utilities     | `:shared:utils`   | Constants, AppConfig, FormatPrice, Log, Screen.kt (navigation routes)                |
| Shared UI     | `:shared:ui`      | Composables, `UiState`, `DisplayResult`, resources                                   |
| Data          | `:network`        | DTOs (`Dto`), mappers, repo implementations, Firebase                                |
| Test Fixtures | `:shared:testing` | Fake data factories, fake repositories                                               |
| Presentation  | `:feature/*`      | UI models (`Ui`), ViewModels, Screens                                                |

### Convention Plugins

| Plugin                        | Applies                                                                               |
| ----------------------------- | ------------------------------------------------------------------------------------- |
| `nutrisport.kmp.library`      | KMP + Compose + Mokkery + Kover + compose.uiTest + Robolectric (iOS, jvmToolchain 21) |
| `nutrisport.kmp.feature`      | library + Koin Compose + messagebar                                                   |
| `nutrisport.kmp.feature.full` | feature + Coil + compose-navigation + Ktor clients                                    |

## Rules

1. **No circular deps.** Feature modules NEVER depend on each other.
2. **:domain is pure.** No platform code, no Firebase, no network, no project deps.
3. **shared:utils is app utilities.** Constants, AppConfig, FormatPrice, Log, Screen.kt. No project deps.
4. **shared:ui is for reusable Compose components.** Depends on `:domain` + `:shared:utils`.
5. **:network owns all external I/O.** Firebase, APIs, file storage — only here.
6. **di module** is the only place that wires repositories and ViewModels.
7. **navigation module** is the only module that knows about all features.
8. **ViewModels** live in feature modules. One ViewModel per screen (max).
9. **Max 150 lines/file, max 20 lines/function.** No god classes.
10. **Convention plugins** — always use them. Never duplicate plugin config.
11. **DTOs never leak** outside `:network`. Map to domain before returning.
12. **Domain models never leak** into Composables. Map to UI models.
13. **Build-type deps via Strategy pattern** — never `if (isDebug)` or `if (USE_FAKE_DATA)` in Application/commonMain. Use polymorphic implementations per source set (`src/debug/`, `src/release/`, `src/benchmark/`) wired through `DebugModuleProvider`. Examples: `DebugToolkit`, `FirebaseConfigurator`.

## Naming

- Modules: lowercase, Gradle path nesting (`:feature:home:cart`)
- ViewModels: `{Feature}ViewModel`
- Repositories: `{Entity}Repository` (interface) + `{Entity}RepositoryImpl`
- Screens: `{Feature}Screen`
- Routes: `{Feature}Route`
- DTOs: `{Entity}Dto`, UI models: `{Entity}Ui`
- Mappers: `{Entity}Mapper` or `.toDomain()` / `.toUi()` extensions

## Adding a New Feature

1. Create module under `feature/` with appropriate convention plugin
2. Add `@Serializable` route to `Screen` sealed class in `:shared:utils`
3. Create UI models in `feature/.../model/` if needed
4. Create ViewModel with mappers in `feature/.../`
5. Add composable destination in `navigation/NavGraph.kt`
6. Register ViewModel in `di/KoinModule.kt`
7. Add module to `settings.gradle.kts`
8. Add deps: `implementation(project(":domain"))` + `implementation(project(":shared:utils"))` + `implementation(project(":shared:ui"))`
9. If feature needs build-type-specific behavior → implement via Strategy pattern (`DebugToolkit`, `FirebaseConfigurator`), not `if (isDebug)`. See [Conventions](conventions.md#build-type-dependencies-strategy-pattern)

## Related

- [Error Handling](error-handling.md) — Either, AppError, DomainResult, UiState patterns
- [Models & Use Cases](models.md) — naming table, mapper rules, use case patterns
