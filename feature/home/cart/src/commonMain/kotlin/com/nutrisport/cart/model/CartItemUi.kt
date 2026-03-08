package com.nutrisport.cart.model

data class CartItemUi(
    val cartItemId: String,
    val productId: String,
    val title: String,
    val thumbnail: String,
    val flavor: String?,
    val quantity: Int,
    val unitPrice: Double,
    val formattedUnitPrice: String,
    val formattedTotalPrice: String,
)
