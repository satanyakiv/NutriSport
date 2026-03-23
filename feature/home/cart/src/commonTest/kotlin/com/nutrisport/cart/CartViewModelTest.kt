package com.nutrisport.cart

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.nutrisport.analytics.core.NutriSportAnalyticsImpl
import com.nutrisport.cart.analytics.CartAnalyticsInteractor
import com.nutrisport.cart.mapper.CartItemToUiMapper
import com.nutrisport.shared.domain.usecase.EnrichCartWithProductsUseCase
import com.nutrisport.shared.domain.usecase.ObserveEnrichedCartUseCase
import com.nutrisport.shared.test.FakeCustomerRepository
import com.nutrisport.shared.test.FakeProductRepository
import com.nutrisport.shared.test.TestCoroutineDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private val fakeCustomerRepo = FakeCustomerRepository()
    private val fakeAnalytics = NutriSportAnalyticsImpl(TestCoroutineDispatcherProvider(testDispatcher))

    private fun createViewModel(): CartViewModel {
        val enrichUseCase = EnrichCartWithProductsUseCase()
        val fakeProductRepo = FakeProductRepository()
        val observeUseCase = ObserveEnrichedCartUseCase(fakeCustomerRepo, fakeProductRepo, enrichUseCase)
        return CartViewModel(
            fakeCustomerRepo,
            observeUseCase,
            CartItemToUiMapper(),
            CartAnalyticsInteractor(fakeAnalytics),
        )
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
        fakeCustomerRepo.updateCartQuantityError = "Quantity update failed"
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

        viewModel.deleteCartItem("cart-1", "Whey Protein", onSuccess = { successCalled = true }, onError = {})
        advanceUntilIdle()

        assertThat(successCalled).isEqualTo(true)
    }

    @Test
    fun `should call onError when deleting cart item fails`() = runTest(testDispatcher) {
        fakeCustomerRepo.deleteCartItemError = "Delete failed"
        val viewModel = createViewModel()
        var errorMessage: String? = null

        viewModel.deleteCartItem("cart-1", "Whey Protein", onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()

        assertThat(errorMessage).isEqualTo("Delete failed")
    }
}
