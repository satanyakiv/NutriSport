package com.nutrisport.checkout

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.orZero
import kotlin.test.Test

class CalculateTotalPriceTest {

    private fun product(id: String, price: Double) = Product(
        id = id, title = "P", description = "D",
        thumbnail = "T", category = "Protein", price = price,
    )

    private fun cartItem(productId: String, quantity: Int) = CartItem(
        id = "c-$productId", productId = productId,
        flavor = null, quantity = quantity,
    )

    @Test
    fun `should calculate total for single item`() {
        val items = listOf(cartItem("p1", 2))
        val products = listOf(product("p1", 10.0))

        val total = calculateTotal(items, products)

        assertThat(total).isCloseTo(20.0, 0.001)
    }

    @Test
    fun `should calculate total for multiple items`() {
        val items = listOf(
            cartItem("p1", 1),
            cartItem("p2", 3),
        )
        val products = listOf(
            product("p1", 10.0),
            product("p2", 5.0),
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
        val items = listOf(cartItem("missing", 5))
        val products = emptyList<Product>()

        val total = calculateTotal(items, products)

        assertThat(total).isEqualTo(0.0)
    }

    private fun calculateTotal(cartItems: List<CartItem>, products: List<Product>): Double {
        return cartItems.sumOf { cartItem ->
            val product = products.find { it.id == cartItem.productId }
            product?.price?.times(cartItem.quantity).orZero()
        }
    }
}
