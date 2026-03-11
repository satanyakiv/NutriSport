package com.nutrisport.shared.domain.usecase

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import com.nutrisport.shared.test.fakeCartItem
import com.nutrisport.shared.test.fakeProduct
import kotlin.test.Test

class CalculateCartTotalUseCaseTest {

    private val useCase = CalculateCartTotalUseCase()

    @Test
    fun `should calculate total for single item`() {
        val items = listOf(fakeCartItem(productId = "p1", quantity = 2))
        val products = listOf(fakeProduct(id = "p1", price = 10.0))

        val total = useCase(items, products)

        assertThat(total).isCloseTo(20.0, 0.001)
    }

    @Test
    fun `should calculate total for multiple items`() {
        val items = listOf(
            fakeCartItem(id = "c1", productId = "p1", quantity = 1),
            fakeCartItem(id = "c2", productId = "p2", quantity = 3),
        )
        val products = listOf(
            fakeProduct(id = "p1", price = 10.0),
            fakeProduct(id = "p2", price = 5.0),
        )

        val total = useCase(items, products)

        assertThat(total).isCloseTo(25.0, 0.001)
    }

    @Test
    fun `should return zero for empty cart`() {
        val total = useCase(emptyList(), emptyList())

        assertThat(total).isEqualTo(0.0)
    }

    @Test
    fun `should skip cart items without matching product`() {
        val items = listOf(fakeCartItem(productId = "missing", quantity = 5))

        val total = useCase(items, emptyList())

        assertThat(total).isEqualTo(0.0)
    }
}
