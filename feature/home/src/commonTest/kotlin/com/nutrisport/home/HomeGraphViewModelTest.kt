package com.nutrisport.home

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.domain.usecase.CalculateCartTotalUseCase
import com.nutrisport.shared.domain.usecase.EnrichCartWithProductsUseCase
import com.nutrisport.shared.domain.usecase.ObserveEnrichedCartUseCase
import com.nutrisport.shared.domain.usecase.SignOutUseCase
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeGraphViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun product(id: String, price: Double) = Product(
        id = id, title = "P", description = "D",
        thumbnail = "T", category = "Protein", price = price,
    )

    private fun customer(cart: List<CartItem> = emptyList()) = Customer(
        id = "user-1", firstName = "John", lastName = "Doe",
        email = "j@e.com", cart = cart,
    )

    private fun fakeCustomerRepo(
        customerState: DomainResult<Customer> = Either.Right(customer()),
        signOutResult: DomainResult<Unit> = Either.Right(Unit),
    ) = object : CustomerRepository {
        override fun getCurrentUserId() = "user-1"
        override fun readCustomerFlow(): Flow<DomainResult<Customer>> = flowOf(customerState)
        override suspend fun createCustomer(
            uid: String,
            displayName: String?,
            email: String?,
        ): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun updateCustomer(customer: Customer): DomainResult<Unit> =
            Either.Right(Unit)
        override suspend fun addItemToCart(cartItem: CartItem): DomainResult<Unit> =
            Either.Right(Unit)
        override suspend fun updateCartItemQuantity(
            id: String,
            quantity: Int,
        ): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun deleteCartItem(id: String): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun deleteAllCartItems(): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun signOut(): DomainResult<Unit> = signOutResult
    }

    private val fakeProductRepo = object : ProductRepository {
        override fun readDiscountedProducts() = flowOf(Either.Right(emptyList<Product>()))
        override fun readNewProducts() = flowOf(Either.Right(emptyList<Product>()))
        override fun getCurrentUserId() = "user-1"
        override fun readProductByIdFlow(id: String): Flow<DomainResult<Product>> =
            flowOf(Either.Right(product(id, 0.0)))
        override fun readProductsByIdsFlow(ids: List<String>) =
            flowOf(Either.Right(emptyList<Product>()))
        override fun readProductsByCategoryFlow(category: ProductCategory): Flow<DomainResult<List<Product>>> =
            flowOf(Either.Right(emptyList()))
    }

    private fun createViewModel(
        customerRepo: CustomerRepository = fakeCustomerRepo(),
        productRepo: ProductRepository = fakeProductRepo,
    ): HomeGraphViewModel {
        val enrichUseCase = EnrichCartWithProductsUseCase()
        val observeUseCase = ObserveEnrichedCartUseCase(customerRepo, productRepo, enrichUseCase)
        val calculateUseCase = CalculateCartTotalUseCase()
        val signOutUseCase = SignOutUseCase(customerRepo)
        return HomeGraphViewModel(customerRepo, observeUseCase, calculateUseCase, signOutUseCase)
    }

    @Test
    fun `should emit customer state`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.customer.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val result = awaitItem()
            assertThat(result).isInstanceOf<UiState.Content<Customer>>()
            val data = (result as UiState.Content).result.getOrNull()
            assertThat(data).isNotNull()
            assertThat(data!!.firstName).isEqualTo("John")
        }
    }

    @Test
    fun `should emit error when customer fails`() = runTest(testDispatcher) {
        val repo = fakeCustomerRepo(customerState = Either.Left(AppError.Network("Network error")))
        val viewModel = createViewModel(customerRepo = repo)

        viewModel.customer.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val result = awaitItem()
            assertThat(result).isInstanceOf<UiState.Content<Customer>>()
            val content = result as UiState.Content<Customer>
            assertThat(content.result).isInstanceOf<Either.Left<AppError>>()
        }
    }

    @Test
    fun `should call onSuccess when sign out succeeds`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var successCalled = false

        viewModel.signOut(onSuccess = { successCalled = true }, onError = {})
        advanceUntilIdle()

        assertThat(successCalled).isEqualTo(true)
    }

    @Test
    fun `should call onError when sign out fails`() = runTest(testDispatcher) {
        val repo = fakeCustomerRepo(signOutResult = Either.Left(AppError.Unknown("Sign out failed")))
        val viewModel = createViewModel(customerRepo = repo)
        var errorMessage: String? = null

        viewModel.signOut(onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()

        assertThat(errorMessage).isEqualTo("Sign out failed")
    }
}
