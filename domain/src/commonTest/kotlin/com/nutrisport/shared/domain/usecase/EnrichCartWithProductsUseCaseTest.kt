package com.nutrisport.shared.domain.usecase

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.nutrisport.shared.test.fakeCartItem
import com.nutrisport.shared.test.fakeProduct
import kotlin.test.Test

class EnrichCartWithProductsUseCaseTest {

    private val useCase = EnrichCartWithProductsUseCase()

    @Test
    fun `should pair cart items with matching products`() {
        val items = listOf(fakeCartItem(productId = "p1"))
        val products = listOf(fakeProduct(id = "p1", title = "Whey"))

        val result = useCase(items, products)

        assertThat(result).hasSize(1)
        assertThat(result[0].first.productId).isEqualTo("p1")
        assertThat(result[0].second.title).isEqualTo("Whey")
    }

    @Test
    fun `should skip cart items without matching product`() {
        val items = listOf(fakeCartItem(productId = "missing"))
        val products = listOf(fakeProduct(id = "p1"))

        val result = useCase(items, products)

        assertThat(result).isEmpty()
    }

    @Test
    fun `should return empty list for empty cart`() {
        val result = useCase(emptyList(), emptyList())

        assertThat(result).isEmpty()
    }

    @Test
    fun `should match multiple items correctly`() {
        val items = listOf(
            fakeCartItem(id = "c1", productId = "p1"),
            fakeCartItem(id = "c2", productId = "p2"),
            fakeCartItem(id = "c3", productId = "p3"),
        )
        val products = listOf(
            fakeProduct(id = "p1"),
            fakeProduct(id = "p3"),
        )

        val result = useCase(items, products)

        assertThat(result).hasSize(2)
        assertThat(result[0].first.productId).isEqualTo("p1")
        assertThat(result[1].first.productId).isEqualTo("p3")
    }
}
