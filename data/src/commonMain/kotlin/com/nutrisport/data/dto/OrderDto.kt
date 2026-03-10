package com.nutrisport.data.dto

import com.nutrisport.shared.domain.CartItem
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String,
    val customerId: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val token: String?,
)
