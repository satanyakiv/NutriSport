package com.nutrisport.details.mapper

import com.nutrisport.details.model.ProductUi
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.formatPrice

class ProductToUiMapper {
  fun map(product: Product): ProductUi {
    val hasPriceChanged = product.previouslyKnownPrice != null &&
      product.previouslyKnownPrice != product.price

    return ProductUi(
      id = product.id,
      title = product.title,
      description = product.description,
      thumbnail = product.thumbnail,
      category = product.category,
      formattedPrice = product.price.formatPrice(),
      flavors = product.flavors.orEmpty(),
      hasFlavors = !product.flavors.isNullOrEmpty(),
      formattedWeight = product.weight?.let { "${it}g" },
      isPopular = product.isPopular,
      isDiscounted = product.isDiscounted,
      isNew = product.isNew,
      previousPrice = if (hasPriceChanged) {
        product.previouslyKnownPrice!!.formatPrice()
      } else {
        null
      },
      isPriceIncrease = hasPriceChanged &&
        product.price > product.previouslyKnownPrice!!,
    )
  }
}
