---
name: gen-test
description: Generate tests for a Kotlin class following NutriSport conventions. Use this skill whenever the user asks to write tests, add test coverage, create unit tests, or says "test this", "cover this", "write tests for X". Also use when user mentions a specific ViewModel, UseCase, Mapper, or Repository and wants it tested. Handles both pure use case tests and ViewModel tests with Turbine/Mokkery.
disable-model-invocation: true
---

Generate tests for: $ARGUMENTS

## Context

This is a KMP project. Tests go in `src/commonTest/kotlin/` mirroring source structure. The project uses `kotlin.test`, Turbine for Flows, Mokkery for mocking, assertk for assertions.

## What to test by class type

| Type | What to test | Mocking | Flow testing |
|------|-------------|---------|--------------|
| **UseCase** (pure) | invoke() success/error paths | Fake repository | No (unless returns Flow) |
| **UseCase** (with repo) | invoke() delegates correctly, error propagation | Fake repository | Turbine if returns Flow |
| **ViewModel** | State transitions (Loading→Content), error handling, user actions | Fake repos via constructor | Turbine for StateFlow |
| **Mapper** | Field mapping, edge cases (null, empty), enum conversion | None (pure functions) | No |
| **Repository** | DTO→Domain mapping, error wrapping | Mock data source | Turbine for Flow returns |

## Process

1. Read the source file to understand what to test
2. Determine the class type (see table above) to pick the right testing approach
3. Check for existing tests and fake data factories in the module's `commonTest`
4. Reference [examples/usecase-test.kt](examples/usecase-test.kt) for pure UseCase tests
5. Reference [examples/viewmodel-test.kt](examples/viewmodel-test.kt) for ViewModel tests with Turbine
6. Create fake data factories (`fakeProduct()`, `fakeCustomer()`) if not yet present
7. Write tests covering: happy path, error path, edge cases
8. Run: `./gradlew :{module}:allTests --tests "*{TestClass}"`
9. Verify all pass

## Conventions

- **Naming**: `` `should X when Y` `` backtick format
- **AAA pattern**: `// Arrange` → `// Act` → `// Assert` comments
- **Flows**: Always Turbine (`test { awaitItem() }`) — never `.first()` or `.toList()`
- **Mocking**: Mokkery — `every { }`, `verify { }`, `mock<Interface>()`
- **Coroutines**: `runTest` + `StandardTestDispatcher()` + `Dispatchers.setMain()`
- **Assertions**: assertk — `assertThat(x).isEqualTo(y)`, `isInstanceOf<Type>()`
- **Fakes**: Prefer `Fake{Entity}Repository` classes over inline `object : Interface` for reuse
- **One assertion per test** (or one logical group)
- **No real I/O** — mock everything external
