# Testing Rules

## Stack

- **Unit tests:** `kotlin.test` (built-in KMP)
- **Flow testing:** `app.cash.turbine:turbine`
- **Mocking:** `dev.mokkery:mokkery-plugin` (compiler plugin)
- **Assertions:** `com.willowtreeapps.assertk:assertk`
- **Coroutines:** `kotlinx-coroutines-test` (runTest, TestDispatcher)
- **UI tests:** `compose.uiTest` (Compose Multiplatform, commonTest)
- **Coverage:** `org.jetbrains.kotlinx.kover:0.9.7` (JVM/Android only)

## Test Pyramid

```
        /  E2E  \        — compose.uiTest: critical user journeys (commonTest)
       /----------\
      / Integration \    — Repository tests with mocked data sources
     /----------------\
    /    Unit Tests     \ — ViewModels, UseCases, Mappers, pure functions
```

## AAA Pattern (Arrange-Act-Assert)

```kotlin
@Test
fun `should return products when repository succeeds`() = runTest {
    // Arrange
    val products = listOf(fakeProduct())
    every { repository.getProducts() } returns flowOf(Either.Right(products))

    // Act
    viewModel.loadProducts()

    // Assert
    viewModel.state.test {
        assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
        val content = awaitItem() as UiState.Content
        assertThat(content.result.getOrNull()).isEqualTo(products)
    }
}
```

## Rules

1. **One assertion per test** (or one logical group). For full test recipes and Fake repository pattern → see [Testing Patterns](../../references/testing-patterns.md)
2. **Test names describe behavior:** `should X when Y`.
3. **No test interdependence.** Each test owns its state.
4. **No real I/O in unit tests.** Mock repos, use TestDispatcher.
5. **Use Turbine** for all Flow assertions — never `.first()` or `.toList()`.
6. **Fake data factories:** `fakeProduct()`, `fakeCustomer()`, etc.
7. **Fakes in separate files.** Fake repositories, fake data factories, test doubles — all live in dedicated files (`Fake*.kt`), not inside test classes. Test files contain only tests.
8. **Never `./gradlew test` without `--tests` filter** — too slow.
9. **Tests mirror source:** `src/commonTest/kotlin/` ↔ `src/commonMain/kotlin/`.
10. **Mappers always tested:** `toDomain()` and `toUi()` are pure — easy to test.

## What to Test

| Layer      | What                              | How                        |
|------------|-----------------------------------|----------------------------|
| ViewModel  | State transitions, error handling | Turbine + mock repository  |
| Repository | DTO→Domain mapping, error wrap    | Mock data source           |
| UseCases   | Business logic, validation        | Pure unit tests (no mocks) |
| Mappers    | Field mapping, edge cases         | Pure unit tests            |
| Navigation | Route resolution                  | Verify Screen destinations |
| UI (E2E)   | Critical user journeys            | compose.uiTest             |

## What NOT to Test

- Firebase SDK internals
- Koin wiring (one integration test is enough)
- Simple data classes without logic
- Platform-specific code (instrumented tests if needed)

## UI Tests (compose.uiTest)

Cross-platform in `commonTest`. Same API as Jetpack Compose Testing.

```kotlin
@OptIn(ExperimentalTestApi::class)
@Test
fun `should show error when cart is empty`() = runComposeUiTest {
    setContent { CartScreen(state = CartState.Empty) }
    onNodeWithTag("empty_cart_message").assertIsDisplayed()
    onNodeWithTag("checkout_button").assertDoesNotExist()
}
```

**Use for:** critical user flows (auth, checkout, cart operations).
**Don't use for:** every screen — too slow, too brittle.

## Test Organization

```
feature/cart/src/commonTest/kotlin/com/nutrisport/feature/cart/
    CartViewModelTest.kt
    CartMapperTest.kt
    FakeCartData.kt

data/src/commonTest/kotlin/com/nutrisport/data/
    ProductRepositoryTest.kt
    ProductMapperTest.kt
    FakeFirestoreData.kt
```

## Running Tests

```bash
# Single test class
./gradlew :feature:cart:allTests --tests "*CartViewModelTest"

# All tests in a module
./gradlew :feature:cart:allTests

# Coverage report (merged, all modules)
./gradlew koverHtmlReport

# Verify coverage thresholds
./gradlew koverVerify
```

## Coverage (Kover)

- Applied in convention plugin (after android) — NOT via `merge { allProjects() }` (causes Kover #772)
- Root merges via `dependencies { kover(project(...)) }` for each module
- All modules have `withHostTest {}` — runs commonTest on JVM for coverage
- Measures JVM/Android only (iOS/Native not supported)
- Excludes: Compose-generated code, DI modules, BuildConfig

**Targets:**
- ViewModels: 80%+
- Repositories: 70%+
- Mappers: 90%+
- Shared utils: 90%+
- UI composables: not measured (use E2E for critical paths)
