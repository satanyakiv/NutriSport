package com.nutrisport.checkout

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.test.fakeCartItem
import com.nutrisport.shared.test.fakeProduct
import com.nutrisport.shared.util.orZero
import kotlin.test.Test

class CalculateTotalPriceTest {

    @Test
    fun `should calculate total for single item`() {
        val items = listOf(fakeCartItem(id = "c-p1", productId = "p1", quantity = 2))
        val products = listOf(fakeProduct(id = "p1", price = 10.0))

        val total = calculateTotal(items, products)

        assertThat(total).isCloseTo(20.0, 0.001)
    }

    @Test
    fun `should calculate total for multiple items`() {
        val items = listOf(
            fakeCartItem(id = "c-p1", productId = "p1", quantity = 1),
            fakeCartItem(id = "c-p2", productId = "p2", quantity = 3),
        )
        val products = listOf(
            fakeProduct(id = "p1", price = 10.0),
            fakeProduct(id = "p2", price = 5.0),
        )

        val total = calculateTotal(items, products)

        assertThat(total).isCloseTo(25.0, 0.001)
    }

    @Test
    fun `should return zero for empty cart`() {
        val total = calculateTotal(emptyList(), emptyList())

        assertThat(total).isEqualTo(0.0)
    }

    @Test
    fun `should skip cart items without matching product`() {
        val items = listOf(fakeCartItem(id = "c-missing", productId = "missing", quantity = 5))
        val products = emptyList<Product>()

        val total = calculateTotal(items, products)

        assertThat(total).isEqualTo(0.0)
    }

    private fun calculateTotal(cartItems: List<com.nutrisport.shared.domain.CartItem>, products: List<Product>): Double {
        return cartItems.sumOf { cartItem ->
            val product = products.find { it.id == cartItem.productId }
            product?.price?.times(cartItem.quantity).orZero()
        }
    }
}
