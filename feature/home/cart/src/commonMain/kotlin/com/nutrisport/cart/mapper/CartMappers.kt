package com.nutrisport.cart.mapper

import com.nutrisport.cart.model.CartItemUi
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Product
import kotlin.math.roundToInt

private fun Double.formatPrice(): String {
    val cents = (this * 100).roundToInt()
    val wholePart = cents / 100
    val fracPart = (cents % 100).toString().padStart(2, '0')
    return "$${wholePart}.${fracPart}"
}

fun Pair<CartItem, Product>.toUi(): CartItemUi {
    val (cartItem, product) = this
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
    )
}
