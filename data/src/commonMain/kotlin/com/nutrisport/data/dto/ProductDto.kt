package com.nutrisport.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val createdAt: Long,
    val title: String,
    val description: String,
    val thumbnail: String,
    val category: String,
    val flavors: List<String>?,
    val weight: Int?,
    val price: Double,
    val isPopular: Boolean,
    val isDiscounted: Boolean,
    val isNew: Boolean,
)
