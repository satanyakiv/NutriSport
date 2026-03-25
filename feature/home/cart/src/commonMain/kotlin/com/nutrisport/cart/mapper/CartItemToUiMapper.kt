package com.nutrisport.cart.mapper

import com.nutrisport.cart.model.CartItemUi
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.formatPrice

class CartItemToUiMapper {
  fun map(cartItem: CartItem, product: Product): CartItemUi {
    val hasPriceChanged = product.previouslyKnownPrice != null &&
      product.previouslyKnownPrice != product.price

    return CartItemUi(
      cartItemId = cartItem.id,
      productId = product.id,
      title = product.title,
      thumbnail = product.thumbnail,
      flavor = cartItem.flavor,
      quantity = cartItem.quantity,
      unitPrice = product.price,
      formattedUnitPrice = product.price.formatPrice(),
      formattedTotalPrice = (product.price * cartItem.quantity).formatPrice(),
      formattedPreviousUnitPrice = if (hasPriceChanged) {
        product.previouslyKnownPrice!!.formatPrice()
      } else {
        null
      },
    )
  }
}
