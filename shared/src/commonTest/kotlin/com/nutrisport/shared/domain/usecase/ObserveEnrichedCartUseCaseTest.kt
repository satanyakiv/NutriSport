package com.nutrisport.shared.domain.usecase

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.FakeCustomerRepository
import com.nutrisport.shared.domain.FakeProductRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.fakeCartItem
import com.nutrisport.shared.domain.fakeCustomer
import com.nutrisport.shared.domain.fakeProduct
import com.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ObserveEnrichedCartUseCaseTest {

    private val customerRepo = FakeCustomerRepository()
    private val productRepo = FakeProductRepository()
    private val enrichUseCase = EnrichCartWithProductsUseCase()
    private val useCase = ObserveEnrichedCartUseCase(customerRepo, productRepo, enrichUseCase)

    private suspend fun app.cash.turbine.ReceiveTurbine<RequestState<List<Pair<CartItem, Product>>>>.awaitSuccess(): List<Pair<CartItem, Product>> {
        var item = awaitItem()
        while (!item.isSuccess()) {
            item = awaitItem()
        }
        return item.getSuccessData()
    }

    @Test
    fun `should emit Loading initially`() = runTest {
        useCase().test {
            assertThat(awaitItem()).isInstanceOf(RequestState.Loading::class)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit enriched cart when customer and products succeed`() = runTest {
        val cart = listOf(fakeCartItem(productId = "p1"), fakeCartItem(id = "c2", productId = "p2"))
        val products = listOf(fakeProduct(id = "p1"), fakeProduct(id = "p2"))
        productRepo.productsByIdsFlow = flowOf(RequestState.Success(products))

        useCase().test {
            customerRepo.customerFlow.value = RequestState.Success(fakeCustomer(cart = cart))
            val enriched = awaitSuccess()

            assertThat(enriched).hasSize(2)
            assertThat(enriched[0].first.productId).isEqualTo("p1")
            assertThat(enriched[1].first.productId).isEqualTo("p2")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit Error when customer fails`() = runTest {
        useCase().test {
            assertThat(awaitItem()).isInstanceOf(RequestState.Loading::class)

            customerRepo.customerFlow.value = RequestState.Error("Network error")
            var item = awaitItem()
            while (!item.isError()) {
                item = awaitItem()
            }

            assertThat(item).isInstanceOf(RequestState.Error::class)
            assertThat(item.getErrorMessage()).isEqualTo("Network error")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit empty list when cart is empty`() = runTest {
        useCase().test {
            customerRepo.customerFlow.value = RequestState.Success(fakeCustomer(cart = emptyList()))
            val enriched = awaitSuccess()

            assertThat(enriched).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should skip cart items without matching product`() = runTest {
        val cart = listOf(fakeCartItem(productId = "p1"), fakeCartItem(id = "c2", productId = "missing"))
        val products = listOf(fakeProduct(id = "p1"))
        productRepo.productsByIdsFlow = flowOf(RequestState.Success(products))

        useCase().test {
            customerRepo.customerFlow.value = RequestState.Success(fakeCustomer(cart = cart))
            val enriched = awaitSuccess()

            assertThat(enriched).hasSize(1)
            assertThat(enriched[0].first.productId).isEqualTo("p1")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
