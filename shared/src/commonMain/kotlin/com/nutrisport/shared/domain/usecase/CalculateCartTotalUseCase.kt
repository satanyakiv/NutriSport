package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Product

class CalculateCartTotalUseCase {
    operator fun invoke(cartItems: List<CartItem>, products: List<Product>): Double {
        return cartItems.sumOf { cartItem ->
            val product = products.find { it.id == cartItem.productId }
            product?.price?.times(cartItem.quantity) ?: 0.0
        }
    }
}
