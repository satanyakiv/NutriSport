package com.nutrisport.details.model

data class ProductUi(
    val id: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val category: String,
    val formattedPrice: String,
    val flavors: List<String>,
    val hasFlavors: Boolean,
    val formattedWeight: String?,
    val isPopular: Boolean,
    val isDiscounted: Boolean,
    val isNew: Boolean,
)
