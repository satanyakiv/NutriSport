# NutriSport

KMP project (Android + iOS) with Compose Multiplatform.

## Rules

- [Architecture](.claude/rules/architecture.md) — module structure, dependency flow, clean architecture rules
- [Error Handling](.claude/rules/error-handling.md) — Either, AppError, DomainResult, UiState patterns
- [Models & Use Cases](.claude/rules/models.md) — naming table, mapper rules, use case patterns
- [Testing](.claude/rules/testing.md) — test stack, AAA pattern, coverage targets, Kover
- [Conventions](.claude/rules/conventions.md) — code style, Compose, coroutines, Git
- [Prompts](.claude/rules/prompts.md) — string resources and constants

## Commands

- `/fix <bug>` — TDD bug fixing (Red-Green-Refactor)
- `/refactor <scope>` — safe refactoring with test-first approach
- `/clean-arch <module>` — Clean Architecture compliance check
- `/debug-deps <error>` — dependency/build crash debugger (GitHub issues first!)

## Quick Reference

### Layers
- **Domain** (`:shared:utils`) — models (no suffix), repository interfaces, use cases, `Either`/`DomainResult`/`AppError`
- **Shared UI** (`:shared:ui`) — composable components, `UiState`, `DisplayResult`, resources, colors, fonts
- **Data** (`:data`) — DTOs (`Dto` suffix), mappers (`.toDomain()`), repository impls
- **Presentation** (`:feature/*`) — UI models (`Ui` suffix), mappers (`.toUi()`), ViewModels

### Error Handling
- **Domain/Data**: `DomainResult<T>` = `Either<AppError, T>` — type-safe errors
- **Presentation**: `UiState<T>` — `Idle`, `Loading`, `Content(DomainResult<T>)`
- **AppError**: `Network`, `NotFound`, `Unauthorized`, `Unknown`

### Key Commands
```bash
./gradlew :{module}:allTests --tests "*TestClass"  # run specific test
./gradlew koverHtmlReport                           # coverage report
./gradlew koverVerify                               # check thresholds
```

### Convention Plugins
- `nutrisport.kmp.library` — base KMP + Compose
- `nutrisport.kmp.feature` — + Koin + messagebar
- `nutrisport.kmp.feature.full` — + Coil + navigation + Ktor
