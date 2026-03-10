# Testing Patterns Reference

## Fake Repository Pattern

Use `MutableSharedFlow` / `MutableStateFlow` for controllable Flow-based testing.

### When Mokkery vs Fake

| Approach | Use when |
|----------|----------|
| **Mokkery** (`mock<T>()`) | Need `verify {}` calls, simple one-shot returns, method call counting |
| **Fake class** (`FakeXxxRepository`) | Flow-based testing, need to emit multiple values over time, complex state scenarios |

### FakeRepository Template

```kotlin
class FakeProductRepository : ProductRepository {
    // Flow-based methods — controllable via MutableSharedFlow
    private val _productsFlow = MutableSharedFlow<DomainResult<List<Product>>>()
    override fun readProductsFlow(): Flow<DomainResult<List<Product>>> = _productsFlow

    // One-shot methods — controllable via mutable result property
    var updateResult: DomainResult<Unit> = Either.Right(Unit)
    override suspend fun updateProduct(product: Product): DomainResult<Unit> = updateResult

    // Helper to emit values in tests
    suspend fun emitProducts(result: DomainResult<List<Product>>) {
        _productsFlow.emit(result)
    }
}
```

### MutableStateFlow variant (when initial value needed)

```kotlin
class FakeCustomerRepository : CustomerRepository {
    private val _customerFlow = MutableStateFlow<DomainResult<Customer>>(
        Either.Right(fakeCustomer()),
    )
    override fun readCustomerFlow(): Flow<DomainResult<Customer>> = _customerFlow

    fun emitCustomer(result: DomainResult<Customer>) {
        _customerFlow.value = result
    }
}
```

## Test Recipes

### ViewModel Test (with Fake + Turbine)

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepo = FakeProductRepository()

    @BeforeTest
    fun setup() { Dispatchers.setMain(testDispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `should show products when repository emits success`() = runTest(testDispatcher) {
        // Arrange
        val products = listOf(fakeProduct())
        val viewModel = ProductListViewModel(fakeRepo)

        // Act & Assert
        viewModel.products.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            fakeRepo.emitProducts(Either.Right(products))
            val content = awaitItem() as UiState.Content
            assertThat(content.result.getOrNull()).isEqualTo(products)
        }
    }

    @Test
    fun `should show error when repository emits failure`() = runTest(testDispatcher) {
        // Arrange
        val viewModel = ProductListViewModel(fakeRepo)

        // Act & Assert
        viewModel.products.test {
            skipItems(1) // skip Loading
            fakeRepo.emitProducts(Either.Left(AppError.Network("timeout")))
            val content = awaitItem() as UiState.Content
            assertThat(content.result.isLeft).isTrue()
        }
    }
}
```

### Mapper Test (pure, no mocks)

```kotlin
class ProductMapperTest {

    @Test
    fun `should map all fields from domain to UI`() {
        // Arrange
        val product = Product(
            id = "1", title = "Whey", description = "desc",
            thumbnail = "url", category = "Protein", price = 29.99,
        )

        // Act
        val ui = product.toUi()

        // Assert
        assertThat(ui.id).isEqualTo("1")
        assertThat(ui.title).isEqualTo("Whey")
        assertThat(ui.formattedPrice).isEqualTo("$29.99")
    }

    @Test
    fun `should handle zero price`() {
        // Arrange
        val product = fakeProduct(price = 0.0)

        // Act
        val ui = product.toUi()

        // Assert
        assertThat(ui.formattedPrice).isEqualTo("$0.00")
    }
}
```

### UseCase Test (with repo dependency)

```kotlin
class CreateOrderUseCaseTest {

    private val fakeRepo = FakeOrderRepository()
    private val useCase = CreateOrderUseCase(fakeRepo)

    @Test
    fun `should return success when order created`() = runTest {
        // Arrange
        fakeRepo.createResult = Either.Right(Unit)

        // Act
        val result = useCase(fakeOrder())

        // Assert
        assertThat(result.isRight).isTrue()
    }

    @Test
    fun `should propagate repository error`() = runTest {
        // Arrange
        fakeRepo.createResult = Either.Left(AppError.Network("offline"))

        // Act
        val result = useCase(fakeOrder())

        // Assert
        assertThat(result.leftOrNull()?.message).isEqualTo("offline")
    }
}
```

### Repository Test (DTO mapping + error wrapping)

```kotlin
class ProductRepositoryTest {

    @Test
    fun `should map DTO to domain model`() = runTest {
        // Arrange
        val dto = ProductDto(id = "1", title = "Whey", price = 29.99)
        val mapper = ProductMapper()

        // Act
        val domain = mapper.map(dto)

        // Assert
        assertThat(domain.id).isEqualTo("1")
        assertThat(domain.price).isEqualTo(29.99)
    }
}
```

## Common Pitfalls

### 1. Dispatchers.IO in ViewModels breaks tests

`Dispatchers.setMain()` only overrides `Dispatchers.Main`. If ViewModel uses `Dispatchers.IO` directly, tests will use real IO dispatcher → flaky/hanging tests.

**Fix:** Use `Dispatchers.IO` only in data layer (repositories). ViewModels should use `viewModelScope` (which uses `Main`).

### 2. Missing setMain/resetMain

Without `Dispatchers.setMain(testDispatcher)`, ViewModel coroutines launched in `viewModelScope` will fail with "Module with the Main dispatcher had failed to initialize".

**Fix:** Always set up in `@BeforeTest` / tear down in `@AfterTest`.

### 3. runTest without testDispatcher

`runTest` creates its own dispatcher. If ViewModel uses a different dispatcher, state updates won't be synchronized.

**Fix:** Pass `testDispatcher` to `runTest(testDispatcher)` — same dispatcher everywhere.

### 4. Using .first() or .toList() on Flows

These can hang or miss intermediate states. Turbine gives precise control.

**Fix:** Always use `flow.test { awaitItem() }`.

### 5. MutableSharedFlow vs MutableStateFlow

- `MutableSharedFlow` — no initial value, replays nothing by default. Good for event streams.
- `MutableStateFlow` — requires initial value, always has current state. Good for state holders.

Use `MutableSharedFlow` in fakes when you want the test to control exactly when emissions happen (no auto-emission on collect).
