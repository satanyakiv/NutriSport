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
- `/debug-crash-live [issue-id]` — live Crashlytics crash analysis via Firebase MCP

## Skills

- `/gen-test <class>` — generate tests following AAA/Turbine/Mokkery conventions
- `/new-feature <name>` — scaffold feature module with full boilerplate
- `/kover-analyze [module]` — coverage analysis and prioritized recommendations
- `/orchestrate-features <cmd>` — parallel feature development orchestration
- `/replay-session <dump.json>` — reconstruct user journey from Tracey dump, identify failure point

## Quick Reference

### Key Commands

```bash
./gradlew :{module}:allTests --tests "*TestClass"  # run specific test
./gradlew :{module}:compileCommonMainKotlinMetadata # quick compile check (common)
./gradlew assembleDebug                             # full Android debug build
./gradlew :composeApp:compileIosMainKotlinMetadata  # iOS compile check
./gradlew koverHtmlReport                           # coverage report
./gradlew koverVerify                               # check thresholds
./gradlew detekt                                     # code style check
```

### Crash Reporting

- **Firebase Crashlytics** in `androidApp` — release only, disabled in debug
- **Tracey** in `androidApp/src/debug/` — flight recorder (debug only)
- **Firebase MCP plugin** — `crashlytics:connect` for automated crash analysis
- `/debug-crash-live` — live Crashlytics analysis via MCP
- `/debug-crash` — offline Tracey dump analysis

## References

- [Testing Patterns](.claude/references/testing-patterns.md) — Fake repos, test recipes, pitfalls
- [Offline-First Architecture](docs/OFFLINE_FIRST.md) — SSOT, ConnectivityObserver, sync vs refresh, price tracking
