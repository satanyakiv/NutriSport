package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Product

class EnrichCartWithProductsUseCase {
    operator fun invoke(
        cartItems: List<CartItem>,
        products: List<Product>,
    ): List<Pair<CartItem, Product>> {
        return cartItems.mapNotNull { cartItem ->
            val product = products.find { it.id == cartItem.productId }
            product?.let { cartItem to it }
        }
    }
}
