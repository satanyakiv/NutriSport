package com.nutrisport.cart.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.nutrisport.shared.test.fakeCartItem
import com.nutrisport.shared.test.fakeProduct
import kotlin.test.Test

class CartMappersTest {

    private val mapper = CartItemToUiMapper()

    @Test
    fun `should map all fields correctly`() {
        // Arrange
        val cartItem = fakeCartItem(quantity = 2)
        val product = fakeProduct()

        // Act
        val ui = mapper.map(cartItem, product)

        // Assert
        assertThat(ui.cartItemId).isEqualTo("cart-1")
        assertThat(ui.productId).isEqualTo("prod-1")
        assertThat(ui.title).isEqualTo("WHEY PROTEIN")
        assertThat(ui.thumbnail).isEqualTo("https://example.com/img.jpg")
        assertThat(ui.flavor).isEqualTo("Chocolate")
        assertThat(ui.quantity).isEqualTo(2)
        assertThat(ui.unitPrice).isEqualTo(29.99)
    }

    @Test
    fun `should format unit price correctly`() {
        // Arrange & Act
        val ui = mapper.map(fakeCartItem(quantity = 2), fakeProduct(price = 9.90))

        // Assert
        assertThat(ui.formattedUnitPrice).isEqualTo("$9.90")
    }

    @Test
    fun `should calculate and format total price`() {
        // Arrange & Act
        val ui = mapper.map(fakeCartItem(quantity = 3), fakeProduct(price = 10.00))

        // Assert
        assertThat(ui.formattedTotalPrice).isEqualTo("$30.00")
    }

    @Test
    fun `should handle null flavor`() {
        // Arrange & Act
        val ui = mapper.map(fakeCartItem(flavor = null, quantity = 2), fakeProduct())

        // Assert
        assertThat(ui.flavor).isNull()
    }

    @Test
    fun `should handle zero price`() {
        // Arrange & Act
        val ui = mapper.map(fakeCartItem(quantity = 1), fakeProduct(price = 0.0))

        // Assert
        assertThat(ui.formattedUnitPrice).isEqualTo("$0.00")
        assertThat(ui.formattedTotalPrice).isEqualTo("$0.00")
    }
}
