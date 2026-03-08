package com.nutrisport.shared.domain.usecase

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.FakeCustomerRepository
import com.nutrisport.shared.domain.FakeProductRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.fakeCartItem
import com.nutrisport.shared.domain.fakeCustomer
import com.nutrisport.shared.domain.fakeProduct
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ObserveEnrichedCartUseCaseTest {

    private val customerRepo = FakeCustomerRepository()
    private val productRepo = FakeProductRepository()
    private val enrichUseCase = EnrichCartWithProductsUseCase()
    private val useCase = ObserveEnrichedCartUseCase(customerRepo, productRepo, enrichUseCase)

    private suspend fun app.cash.turbine.ReceiveTurbine<DomainResult<List<Pair<CartItem, Product>>>>.awaitRight(): List<Pair<CartItem, Product>> {
        var item = awaitItem()
        while (!item.isRight) {
            item = awaitItem()
        }
        return item.getOrNull()!!
    }

    @Test
    fun `should emit enriched cart when customer and products succeed`() = runTest {
        val cart = listOf(fakeCartItem(productId = "p1"), fakeCartItem(id = "c2", productId = "p2"))
        val products = listOf(fakeProduct(id = "p1"), fakeProduct(id = "p2"))
        productRepo.productsByIdsFlow = flowOf(Either.Right(products))
        customerRepo.customerFlow.value = Either.Right(fakeCustomer(cart = cart))

        useCase().test {
            val enriched = awaitRight()

            assertThat(enriched).hasSize(2)
            assertThat(enriched[0].first.productId).isEqualTo("p1")
            assertThat(enriched[1].first.productId).isEqualTo("p2")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit Error when customer fails`() = runTest {
        useCase().test {
            customerRepo.customerFlow.value = Either.Left(AppError.Network("Network error"))
            var item = awaitItem()
            while (!item.isLeft) {
                item = awaitItem()
            }

            assertThat(item.isLeft).isTrue()
            assertThat(item.leftOrNull()?.message).isEqualTo("Network error")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit empty list when cart is empty`() = runTest {
        useCase().test {
            customerRepo.customerFlow.value = Either.Right(fakeCustomer(cart = emptyList()))
            val enriched = awaitRight()

            assertThat(enriched).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should skip cart items without matching product`() = runTest {
        val cart = listOf(fakeCartItem(productId = "p1"), fakeCartItem(id = "c2", productId = "missing"))
        val products = listOf(fakeProduct(id = "p1"))
        productRepo.productsByIdsFlow = flowOf(Either.Right(products))
        customerRepo.customerFlow.value = Either.Right(fakeCustomer(cart = cart))

        useCase().test {
            val enriched = awaitRight()

            assertThat(enriched).hasSize(1)
            assertThat(enriched[0].first.productId).isEqualTo("p1")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
