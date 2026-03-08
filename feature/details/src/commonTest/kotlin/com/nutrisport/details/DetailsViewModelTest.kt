package com.nutrisport.details

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import androidx.lifecycle.SavedStateHandle
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class DetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun product(id: String = "prod-1") = Product(
        id = id, title = "WHEY PROTEIN", description = "desc",
        thumbnail = "url", category = "Protein", price = 29.99,
        flavors = listOf("Chocolate", "Vanilla"),
    )

    private var addToCartError: String? = null

    private val fakeProductRepo = object : ProductRepository {
        override fun readDiscountedProducts() = flowOf(Either.Right(emptyList<Product>()))
        override fun readNewProducts() = flowOf(Either.Right(emptyList<Product>()))
        override fun getCurrentUserId() = "user-1"
        override fun readProductByIdFlow(id: String): Flow<DomainResult<Product>> =
            flowOf(Either.Right(product(id)))
        override fun readProductsByIdsFlow(ids: List<String>) =
            flowOf(Either.Right(emptyList<Product>()))
        override fun readProductsByCategoryFlow(category: ProductCategory): Flow<DomainResult<List<Product>>> =
            flowOf(Either.Right(emptyList()))
    }

    private val fakeCustomerRepo = object : CustomerRepository {
        override fun getCurrentUserId() = "user-1"
        override fun readCustomerFlow(): Flow<DomainResult<Customer>> =
            flowOf(Either.Right(Customer(id = "user-1", firstName = "John", lastName = "Doe", email = "j@e.com")))
        override suspend fun createCustomer(
            uid: String,
            displayName: String?,
            email: String?,
        ): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun updateCustomer(customer: Customer): DomainResult<Unit> =
            Either.Right(Unit)
        override suspend fun addItemToCart(cartItem: CartItem): DomainResult<Unit> {
            return addToCartError?.let { Either.Left(AppError.Unknown(it)) }
                ?: Either.Right(Unit)
        }
        override suspend fun updateCartItemQuantity(
            id: String,
            quantity: Int,
        ): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun deleteCartItem(id: String): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun deleteAllCartItems(): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun signOut(): DomainResult<Unit> = Either.Right(Unit)
    }

    private fun createViewModel(productId: String = "prod-1"): DetailsViewModel {
        val savedState = SavedStateHandle(mapOf("id" to productId))
        return DetailsViewModel(fakeProductRepo, fakeCustomerRepo, savedState)
    }

    @Test
    fun `should load product by id from saved state`() = runTest(testDispatcher) {
        val viewModel = createViewModel("prod-1")

        viewModel.product.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val result = awaitItem()
            assertThat(result).isInstanceOf<UiState.Content<Product>>()
            val data = (result as UiState.Content).result.getOrNull()
            assertThat(data).isNotNull()
            assertThat(data!!.title).isEqualTo("WHEY PROTEIN")
        }
    }

    @Test
    fun `should update quantity`() {
        val viewModel = createViewModel()

        viewModel.updateQuantity(5)

        assertThat(viewModel.quantity).isEqualTo(5)
    }

    @Test
    fun `should update selected flavor`() {
        val viewModel = createViewModel()

        viewModel.updateFlavor("Chocolate")

        assertThat(viewModel.selectedFlavor).isEqualTo("Chocolate")
    }

    @Test
    fun `should call onSuccess when adding item to cart`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var successCalled = false

        viewModel.addItemToCart(onSuccess = { successCalled = true }, onError = {})
        advanceUntilIdle()

        assertThat(successCalled).isEqualTo(true)
    }

    @Test
    fun `should call onError when adding item to cart fails`() = runTest(testDispatcher) {
        addToCartError = "Cart is full"
        val viewModel = createViewModel()
        var errorMessage: String? = null

        viewModel.addItemToCart(onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()

        assertThat(errorMessage).isEqualTo("Cart is full")
    }
}
