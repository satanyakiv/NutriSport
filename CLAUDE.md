# NutriSport

KMP project (Android + iOS) with Compose Multiplatform.

## Rules

- [Architecture](.claude/rules/architecture.md) — Clean Architecture layers, model mapping, module structure
- [Testing](.claude/rules/testing.md) — test stack, AAA pattern, coverage targets, Kover
- [Conventions](.claude/rules/conventions.md) — code style, Compose, coroutines, Git
- [Prompts](.claude/rules/prompts.md) — string resources and constants

## Commands

- `/fix <bug>` — TDD bug fixing (Red-Green-Refactor)
- `/refactor <scope>` — safe refactoring with test-first approach
- `/clean-arch <module>` — Clean Architecture compliance check

## Quick Reference

### Layers
- **Domain** (`:shared`) — models (no suffix), repository interfaces, use cases
- **Data** (`:data`) — DTOs (`Dto` suffix), mappers (`.toDomain()`), repository impls
- **Presentation** (`:feature/*`) — UI models (`Ui` suffix), mappers (`.toUi()`), ViewModels

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
