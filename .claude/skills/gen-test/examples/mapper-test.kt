// Example: Pure mapper test — no mocks, no coroutines
package com.nutrisport.feature.details.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ProductMapperTest {

    @Test
    fun `should map all fields from domain to UI`() {
        // Arrange
        val product = fakeProduct(
            id = "prod-1",
            title = "WHEY PROTEIN",
            price = 29.99,
        )

        // Act
        val ui = product.toUi()

        // Assert
        assertThat(ui.id).isEqualTo("prod-1")
        assertThat(ui.title).isEqualTo("WHEY PROTEIN")
        assertThat(ui.formattedPrice).isEqualTo("$29.99")
    }

    @Test
    fun `should handle zero price`() {
        // Arrange
        val product = fakeProduct(price = 0.0)

        // Act
        val ui = product.toUi()

        // Assert
        assertThat(ui.formattedPrice).isEqualTo("$0.00")
    }

    @Test
    fun `should handle empty description`() {
        // Arrange
        val product = fakeProduct(description = "")

        // Act
        val ui = product.toUi()

        // Assert
        assertThat(ui.description).isEqualTo("")
    }
}

// Fake data factory — reusable across tests in this module
private fun fakeProduct(
    id: String = "prod-1",
    title: String = "Test Product",
    description: String = "Test description",
    price: Double = 9.99,
) = Product(
    id = id,
    title = title,
    description = description,
    thumbnail = "https://example.com/img.png",
    category = "Protein",
    price = price,
)
