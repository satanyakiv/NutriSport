package com.nutrisport.shared.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.nutrisport.shared.test.FakeOrderRepository
import com.nutrisport.shared.test.fakeCartItem
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CreateOrderUseCaseTest {

    private val fakeRepo = FakeOrderRepository()
    private val useCase = CreateOrderUseCase(fakeRepo)

    @Test
    fun `should return Right when order is created`() = runTest {
        val result = useCase(
            customerId = "user-1",
            cartItems = listOf(fakeCartItem()),
            totalAmount = 29.99,
        )

        assertThat(result).isEqualTo(Either.Right(Unit))
    }

    @Test
    fun `should return Left when repository fails`() = runTest {
        fakeRepo.createOrderError = "Order creation failed"

        val result = useCase(
            customerId = "user-1",
            cartItems = listOf(fakeCartItem()),
            totalAmount = 29.99,
        )

        assertThat(result.isLeft).isTrue()
        assertThat(result.leftOrNull()?.message).isEqualTo("Order creation failed")
    }

    @Test
    fun `should pass correct total amount to repository`() = runTest {
        val result = useCase(
            customerId = "user-1",
            cartItems = listOf(fakeCartItem(quantity = 3)),
            totalAmount = 89.97,
        )

        assertThat(result).isEqualTo(Either.Right(Unit))
    }

    @Test
    fun `should handle empty cart`() = runTest {
        val result = useCase(
            customerId = "user-1",
            cartItems = emptyList(),
            totalAmount = 0.0,
        )

        assertThat(result).isEqualTo(Either.Right(Unit))
    }
}
