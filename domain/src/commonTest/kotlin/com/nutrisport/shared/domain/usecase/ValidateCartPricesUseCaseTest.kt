package com.nutrisport.shared.domain.usecase

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.nutrisport.shared.test.fakeCartItem
import com.nutrisport.shared.test.fakeProduct
import kotlin.test.Test

class ValidateCartPricesUseCaseTest {

  private val useCase = ValidateCartPricesUseCase()

  @Test
  fun `should return empty when no price changes`() {
    val cartItems = listOf(fakeCartItem(productId = "prod-1"))
    val products = listOf(fakeProduct(id = "prod-1", price = 29.99))

    val result = useCase(cartItems, products)

    assertThat(result).isEmpty()
  }

  @Test
  fun `should detect price increase`() {
    val cartItems = listOf(fakeCartItem(productId = "prod-1", quantity = 2))
    val products = listOf(
      fakeProduct(
        id = "prod-1",
        price = 34.99,
        previouslyKnownPrice = 29.99,
      ),
    )

    val result = useCase(cartItems, products)

    assertThat(result).hasSize(1)
    assertThat(result[0].previousPrice).isEqualTo(29.99)
    assertThat(result[0].currentPrice).isEqualTo(34.99)
    assertThat(result[0].quantity).isEqualTo(2)
  }

  @Test
  fun `should detect price decrease`() {
    val cartItems = listOf(fakeCartItem(productId = "prod-1"))
    val products = listOf(
      fakeProduct(
        id = "prod-1",
        price = 19.99,
        previouslyKnownPrice = 29.99,
      ),
    )

    val result = useCase(cartItems, products)

    assertThat(result).hasSize(1)
    assertThat(result[0].currentPrice).isEqualTo(19.99)
  }

  @Test
  fun `should ignore products without previous price`() {
    val cartItems = listOf(fakeCartItem(productId = "prod-1"))
    val products = listOf(
      fakeProduct(id = "prod-1", price = 34.99, previouslyKnownPrice = null),
    )

    val result = useCase(cartItems, products)

    assertThat(result).isEmpty()
  }

  @Test
  fun `should ignore when previous price equals current`() {
    val cartItems = listOf(fakeCartItem(productId = "prod-1"))
    val products = listOf(
      fakeProduct(
        id = "prod-1",
        price = 29.99,
        previouslyKnownPrice = 29.99,
      ),
    )

    val result = useCase(cartItems, products)

    assertThat(result).isEmpty()
  }
}
