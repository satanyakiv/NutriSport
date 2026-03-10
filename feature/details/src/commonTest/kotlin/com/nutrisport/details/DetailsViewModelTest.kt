package com.nutrisport.details

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import androidx.lifecycle.SavedStateHandle
import com.nutrisport.details.mapper.ProductToUiMapper
import com.nutrisport.details.model.ProductUi
import com.nutrisport.shared.test.FakeCustomerRepository
import com.nutrisport.shared.test.FakeProductRepository
import com.nutrisport.shared.test.fakeProduct
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private val fakeCustomerRepo = FakeCustomerRepository()
    private val fakeProductRepo = FakeProductRepository().apply {
        productByIdFlows = mutableMapOf(
            "prod-1" to flowOf(Either.Right(fakeProduct())),
        )
    }

    private fun createViewModel(productId: String = "prod-1"): DetailsViewModel {
        val savedState = SavedStateHandle(mapOf("id" to productId))
        return DetailsViewModel(fakeProductRepo, fakeCustomerRepo, ProductToUiMapper(), savedState)
    }

    @Test
    fun `should load product by id from saved state`() = runTest(testDispatcher) {
        val viewModel = createViewModel("prod-1")

        viewModel.product.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val result = awaitItem()
            assertThat(result).isInstanceOf<UiState.Content<ProductUi>>()
            val data = (result as UiState.Content).result.getOrNull()
            assertThat(data).isNotNull()
            assertThat(data!!.title).isEqualTo("WHEY PROTEIN")
            assertThat(data.formattedPrice).isEqualTo("$29.99")
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
        fakeCustomerRepo.addToCartError = "Cart is full"
        val viewModel = createViewModel()
        var errorMessage: String? = null

        viewModel.addItemToCart(onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()

        assertThat(errorMessage).isEqualTo("Cart is full")
    }
}
