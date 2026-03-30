# Testing Guide

## Testing Strategy

All tests run on **JVM** — no emulator, no device, no iOS simulator required. Typical full run takes ~30s.

**Test pyramid:**

- **Unit tests** (domain + ViewModel + mapper) — bulk of coverage, fast, isolated
- **UI smoke tests** (Robolectric) — critical screen rendering and interactions
- **No E2E** — all tests are isolated, no integration or end-to-end suites

**Fakes over mocks:** the project prefers Fake repositories from `:shared:testing` over mocking frameworks. Mokkery is available but rarely used.

**Why `androidHostTest` for UI tests:** `compose.uiTest` was initially in `commonTest`, but CMP resources and fonts require Android context to resolve. Tests were moved to `androidHostTest` with Robolectric to run on JVM while having access to Android APIs. Never put UI smoke tests in `commonTest`.

## Test Stack

| Tool              | Version    | Purpose                                                 |
| ----------------- | ---------- | ------------------------------------------------------- |
| `kotlin.test`     | (built-in) | Assertions and test annotations                         |
| Turbine           | 1.2.1      | Flow testing (`test {}`, `awaitItem()`)                 |
| Assertk           | 0.28.1     | Fluent assertions (`assertThat(...).isEqualTo(...)`)    |
| Mokkery           | 3.2.0      | Mocking compiler plugin (used sparingly)                |
| Robolectric       | 4.14.1     | Android context on JVM for UI tests                     |
| `compose.uiTest`  | (CMP)      | Compose testing API (`onNodeWithText`, `onNodeWithTag`) |
| `coroutines-test` | 1.10.2     | `runTest`, `StandardTestDispatcher`                     |
| Kover             | 0.9.7      | Code coverage (JVM/Android only)                        |

## Test Layers

| Layer          | Source set        | What                                                       | Count |
| -------------- | ----------------- | ---------------------------------------------------------- | ----- |
| Domain unit    | `commonTest`      | Either, DomainResult, 7 use cases, ProductCategory         | 10    |
| ViewModel unit | `commonTest`      | 9 ViewModels: state transitions, error handling, Turbine   | 9     |
| Mapper unit    | `commonTest`      | CartMappers, CalculateTotalPrice                           | 2     |
| UI smoke       | `androidHostTest` | 6 screens: rendering, states, interactions via Robolectric | 6     |

### Not Covered (and Why)

- **Network/Data layer (0%)** — Firebase repos need emulator or extensive mocking, diminishing returns
- **Database module (0%)** — Room DAOs excluded from Kover, would need instrumented tests
- **Auth ViewModel** — depends on third-party Firebase Auth, hard to isolate
- **Checkout + PaymentCompleted ViewModels** — only UI smoke tests exist, no ViewModel unit tests
- **Analytics (0%)** — CompositeTracker is untested
- **iOS UI tests** — CMP `compose.uiTest` doesn't run on iOS targets
- **Integration/E2E** — none, all tests are isolated unit or smoke tests

## Coverage (Kover)

Convention plugin applies Kover after android configuration. Root `build.gradle.kts` merges modules via `dependencies { kover(project(...)) }`.

<!-- coverage:start -->
| Package | Line coverage |
| ------- | ------------- |
| domain:usecase | 98.8% |
| feature:productsOverview | 90.3% |
| feature:details | 90.1% |
| feature:cart | 86.6% |
| domain:models | 84.0% |
| analytics:core | 77.4% |
| shared:utils | 76.9% |
| feature:profile | 65.0% |
| feature:categories:search | 46.4% |
| analytics:firebase | 45.2% |
| feature:adminPanel | 30.0% |
| feature:manageProduct | 22.2% |
| feature:auth | 13.5% |
| feature:home | 9.5% |
| network | 0.0% |
| feature:paymentCompleted | 0.0% |
| feature:checkout | 0.0% |

> Overall line coverage: 37.5%. Low aggregate reflects untested generated code, UI composables, and data layer — tested packages average 80%+.

**Report:** 2026-03-30 | Regenerate: `./gradlew koverXmlReport` | HTML: `build/reports/kover/html/index.html`

### What Kover Excludes

| Category | Patterns |
| -------- | -------- |
| UI composables | `*Screen*`, `*Preview*`, `*ComposableSingletons*`, `component.*` |
| Generated code | `Resources*`, `*BuildConfig*` |
| Infrastructure | `di.*`, `navigation.*`, `database.*` (DAOs, entities, converters) |
| Platform | `MainActivity*`, `NutrisportApplication*` |
| Design tokens | `Alpha*`, `Colors*`, `Fonts*`, `Constants*` |

Exclusions configured in root `build.gradle.kts`.
<!-- coverage:end -->

## Running Tests

```bash
# All tests (JVM, ~30s)
./gradlew testAndroidHostTest

# Single module
./gradlew :domain:allTests

# Single test class
./gradlew :domain:allTests --tests "*EitherTest"

# Coverage HTML report → build/reports/kover/html/
./gradlew koverHtmlReport

# Verify coverage thresholds (none configured yet)
./gradlew koverVerify
```

## CI Integration

| Workflow          | Trigger      | Tests                 | Coverage          |
| ----------------- | ------------ | --------------------- | ----------------- |
| `pr.yml`          | PR to main   | `testAndroidHostTest` | `koverHtmlReport` |
| `debug.yml`       | push to main | `testAndroidHostTest` | `koverHtmlReport` |
| `release.yml`     | tag `v*`     | none                  | none              |
| `ios-release.yml` | manual       | none                  | none              |

## Adding New Tests

### ViewModel test (`commonTest`)

```kotlin
class MyViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `should emit content when repository succeeds`() = runTest {
        // Arrange
        val fakeRepo = FakeMyRepository(result = Either.Right(fakeData()))
        val viewModel = MyViewModel(fakeRepo)

        // Act & Assert
        viewModel.state.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val content = awaitItem() as UiState.Content
            assertThat(content.result.getOrNull()).isEqualTo(fakeData())
        }
    }
}
```

### Use case test (`domain/commonTest`)

Pure unit tests — no mocks, no dispatchers, no Turbine (unless the use case returns a Flow).

```kotlin
class CalculateCartTotalUseCaseTest {
    @Test
    fun `should return total price`() {
        val useCase = CalculateCartTotalUseCase()
        val result = useCase(cartItems = listOf(fakeCartItem()), products = listOf(fakeProduct()))
        assertThat(result).isEqualTo(29.99)
    }
}
```

### UI smoke test (`androidHostTest`)

```kotlin
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class MyScreenTest {
    @Test
    fun `should display content`() = runComposeUiTest {
        setContent { MyScreen(state = UiState.Content(Either.Right(fakeUiData()))) }
        onNodeWithText("Expected text").assertIsDisplayed()
    }
}
```

> **Never** put UI smoke tests in `commonTest` — CMP resources and fonts require Android context provided by Robolectric in `androidHostTest`.
