package com.nutrisport.cart.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Product
import kotlin.test.Test

class CartMappersTest {

    private fun fakeProduct(
        id: String = "prod-1",
        title: String = "Whey Protein",
        thumbnail: String = "https://example.com/img.png",
        price: Double = 29.99,
    ) = Product(
        id = id,
        title = title,
        description = "desc",
        thumbnail = thumbnail,
        category = "Protein",
        price = price,
    )

    private fun fakeCartItem(
        id: String = "cart-1",
        productId: String = "prod-1",
        flavor: String? = "Chocolate",
        quantity: Int = 2,
    ) = CartItem(
        id = id,
        productId = productId,
        flavor = flavor,
        quantity = quantity,
    )

    @Test
    fun `should map all fields correctly`() {
        // Arrange
        val pair = Pair(fakeCartItem(), fakeProduct())

        // Act
        val ui = pair.toUi()

        // Assert
        assertThat(ui.cartItemId).isEqualTo("cart-1")
        assertThat(ui.productId).isEqualTo("prod-1")
        assertThat(ui.title).isEqualTo("Whey Protein")
        assertThat(ui.thumbnail).isEqualTo("https://example.com/img.png")
        assertThat(ui.flavor).isEqualTo("Chocolate")
        assertThat(ui.quantity).isEqualTo(2)
        assertThat(ui.unitPrice).isEqualTo(29.99)
    }

    @Test
    fun `should format unit price correctly`() {
        // Arrange
        val pair = Pair(fakeCartItem(), fakeProduct(price = 9.90))

        // Act
        val ui = pair.toUi()

        // Assert
        assertThat(ui.formattedUnitPrice).isEqualTo("$9.90")
    }

    @Test
    fun `should calculate and format total price`() {
        // Arrange
        val pair = Pair(fakeCartItem(quantity = 3), fakeProduct(price = 10.00))

        // Act
        val ui = pair.toUi()

        // Assert
        assertThat(ui.formattedTotalPrice).isEqualTo("$30.00")
    }

    @Test
    fun `should handle null flavor`() {
        // Arrange
        val pair = Pair(fakeCartItem(flavor = null), fakeProduct())

        // Act
        val ui = pair.toUi()

        // Assert
        assertThat(ui.flavor).isNull()
    }

    @Test
    fun `should handle zero price`() {
        // Arrange
        val pair = Pair(fakeCartItem(quantity = 1), fakeProduct(price = 0.0))

        // Act
        val ui = pair.toUi()

        // Assert
        assertThat(ui.formattedUnitPrice).isEqualTo("$0.00")
        assertThat(ui.formattedTotalPrice).isEqualTo("$0.00")
    }
}
