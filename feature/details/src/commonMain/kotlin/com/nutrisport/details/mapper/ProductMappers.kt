package com.nutrisport.details.mapper

import com.nutrisport.details.model.ProductUi
import com.nutrisport.shared.domain.Product
import kotlin.math.roundToInt

private fun Double.formatPrice(): String {
    val cents = (this * 100).roundToInt()
    val wholePart = cents / 100
    val fracPart = (cents % 100).toString().padStart(2, '0')
    return "$${wholePart}.${fracPart}"
}

fun Product.toUi(): ProductUi = ProductUi(
    id = id,
    title = title,
    description = description,
    thumbnail = thumbnail,
    category = category,
    formattedPrice = price.formatPrice(),
    flavors = flavors ?: emptyList(),
    hasFlavors = !flavors.isNullOrEmpty(),
    formattedWeight = weight?.let { "${it}g" },
    isPopular = isPopular,
    isDiscounted = isDiscounted,
    isNew = isNew,
)
