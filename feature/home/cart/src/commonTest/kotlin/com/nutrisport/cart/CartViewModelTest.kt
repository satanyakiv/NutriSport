package com.nutrisport.cart

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.domain.usecase.EnrichCartWithProductsUseCase
import com.nutrisport.shared.domain.usecase.ObserveEnrichedCartUseCase
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
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
class CartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private var updateQuantityError: String? = null
    private var deleteItemError: String? = null

    private val fakeCustomerRepo = object : CustomerRepository {
        override fun getCurrentUserId() = "user-1"
        override fun readCustomerFlow(): Flow<DomainResult<Customer>> =
            flowOf(Either.Right(Customer(
                id = "user-1", firstName = "John", lastName = "Doe", email = "j@e.com",
            )))
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
        ): DomainResult<Unit> {
            return updateQuantityError?.let { Either.Left(AppError.Unknown(it)) }
                ?: Either.Right(Unit)
        }
        override suspend fun deleteCartItem(id: String): DomainResult<Unit> {
            return deleteItemError?.let { Either.Left(AppError.Unknown(it)) }
                ?: Either.Right(Unit)
        }
        override suspend fun deleteAllCartItems(): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun signOut(): DomainResult<Unit> = Either.Right(Unit)
    }

    private fun createViewModel(): CartViewModel {
        val enrichUseCase = EnrichCartWithProductsUseCase()
        val fakeProductRepo = object : ProductRepository {
            override fun readDiscountedProducts() = flowOf(Either.Right(emptyList<Product>()))
            override fun readNewProducts() = flowOf(Either.Right(emptyList<Product>()))
            override fun getCurrentUserId() = "user-1"
            override fun readProductByIdFlow(id: String): Flow<DomainResult<Product>> =
                flowOf(Either.Right(Product(id = id, title = "P", description = "D", thumbnail = "T", category = "Protein", price = 0.0)))
            override fun readProductsByIdsFlow(ids: List<String>) =
                flowOf(Either.Right(emptyList<Product>()))
            override fun readProductsByCategoryFlow(category: ProductCategory): Flow<DomainResult<List<Product>>> =
                flowOf(Either.Right(emptyList()))
        }
        val observeUseCase = ObserveEnrichedCartUseCase(fakeCustomerRepo, fakeProductRepo, enrichUseCase)
        return CartViewModel(fakeCustomerRepo, observeUseCase)
    }

    @Test
    fun `should call onSuccess when updating cart item quantity`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var successCalled = false

        viewModel.updateCartItemQuantity("cart-1", 5, onSuccess = { successCalled = true }, onError = {})
        advanceUntilIdle()

        assertThat(successCalled).isEqualTo(true)
    }

    @Test
    fun `should call onError when updating cart item quantity fails`() = runTest(testDispatcher) {
        updateQuantityError = "Quantity update failed"
        val viewModel = createViewModel()
        var errorMessage: String? = null

        viewModel.updateCartItemQuantity("cart-1", 5, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()

        assertThat(errorMessage).isEqualTo("Quantity update failed")
    }

    @Test
    fun `should call onSuccess when deleting cart item`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var successCalled = false

        viewModel.deleteCartItem("cart-1", onSuccess = { successCalled = true }, onError = {})
        advanceUntilIdle()

        assertThat(successCalled).isEqualTo(true)
    }

    @Test
    fun `should call onError when deleting cart item fails`() = runTest(testDispatcher) {
        deleteItemError = "Delete failed"
        val viewModel = createViewModel()
        var errorMessage: String? = null

        viewModel.deleteCartItem("cart-1", onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()

        assertThat(errorMessage).isEqualTo("Delete failed")
    }
}
