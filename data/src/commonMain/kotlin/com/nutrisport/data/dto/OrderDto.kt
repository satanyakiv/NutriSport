package com.nutrisport.data.dto

import com.nutrisport.shared.domain.CartItem

data class OrderDto(
    val id: String,
    val customerId: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val token: String?,
)
