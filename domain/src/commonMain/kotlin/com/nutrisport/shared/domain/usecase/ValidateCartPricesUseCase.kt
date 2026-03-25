package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Product

data class PriceMismatch(
  val productId: String,
  val productTitle: String,
  val previousPrice: Double,
  val currentPrice: Double,
  val quantity: Int,
)

class ValidateCartPricesUseCase {
  operator fun invoke(
    cartItems: List<CartItem>,
    products: List<Product>,
  ): List<PriceMismatch> {
    return cartItems.mapNotNull { cartItem ->
      val product = products.find { it.id == cartItem.productId }
      val previousPrice = product?.previouslyKnownPrice
      if (product != null && previousPrice != null && previousPrice != product.price) {
        PriceMismatch(
          productId = product.id,
          productTitle = product.title,
          previousPrice = previousPrice,
          currentPrice = product.price,
          quantity = cartItem.quantity,
        )
      } else {
        null
      }
    }
  }
}
