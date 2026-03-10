# Architecture Rules

## Project Structure

NutriSport is a KMP (Kotlin Multiplatform) project targeting Android + iOS.

### Module Hierarchy

```
androidApp          — Android entry point (Activity, Application)
composeApp          — Shared KMP UI entry (AppContent)
navigation          — NavHost + type-safe routing (SetupNavGraph)
shared/utils        — Domain layer: models, interfaces, use cases, Either, AppError
shared/ui           — Shared UI: composable components, UiState, DisplayResult, resources
data                — Data layer: Firebase repos, DTOs, mappers to domain
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
       ├─ navigation (depends on all feature modules + shared:utils)
       ├─ di (depends on all feature modules + data + analytics + shared:utils)
       ├─ shared:utils (domain layer — no external deps)
       ├─ shared:ui (depends on shared:utils)
       └─ data (depends on shared:utils + database)

feature modules → shared:utils + shared:ui (never depend on each other)
data → shared:utils (never shared:ui)
di, analytics, navigation → shared:utils only
```

### Clean Architecture Layers

| Layer | Module | Contains |
|-------|--------|----------|
| Domain | `:shared:utils` | Models, repo interfaces, use cases, `Either`/`AppError` |
| Shared UI | `:shared:ui` | Composables, `UiState`, `DisplayResult`, resources |
| Data | `:data` | DTOs (`Dto`), mappers, repo implementations, Firebase |
| Presentation | `:feature/*` | UI models (`Ui`), ViewModels, Screens |

### Convention Plugins

| Plugin | Applies |
|--------|---------|
| `nutrisport.kmp.library` | KMP + Compose + Mokkery + Kover (iOS, jvmToolchain 21) |
| `nutrisport.kmp.feature` | library + Koin Compose + messagebar |
| `nutrisport.kmp.feature.full` | feature + Coil + compose-navigation + Ktor clients |

## Rules

1. **No circular deps.** Feature modules NEVER depend on each other.
2. **shared:utils is pure domain.** No platform code, no Firebase, no network.
3. **shared:ui is for reusable Compose components.** Depends only on shared:utils.
4. **data owns all external I/O.** Firebase, APIs, file storage — only here.
5. **di module** is the only place that wires repositories and ViewModels.
6. **navigation module** is the only module that knows about all features.
7. **ViewModels** live in feature modules. One ViewModel per screen (max).
8. **Max 150 lines/file, max 20 lines/function.** No god classes.
9. **Convention plugins** — always use them. Never duplicate plugin config.
10. **DTOs never leak** outside `:data`. Map to domain before returning.
11. **Domain models never leak** into Composables. Map to UI models.

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
2. Add `@Serializable` route to `Screen` sealed class in `shared:utils`
3. Create UI models in `feature/.../model/` if needed
4. Create ViewModel with mappers in `feature/.../`
5. Add composable destination in `navigation/NavGraph.kt`
6. Register ViewModel in `di/KoinModule.kt`
7. Add module to `settings.gradle.kts`
8. Add deps: `implementation(project(":shared:utils"))` + `implementation(project(":shared:ui"))`

## Related

- [Error Handling](error-handling.md) — Either, AppError, DomainResult, UiState patterns
- [Models & Use Cases](models.md) — naming table, mapper rules, use case patterns
