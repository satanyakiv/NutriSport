package com.nutrisport.data.mapper

import com.nutrisport.data.dto.ProductDto
import com.nutrisport.shared.domain.Product

class ProductToDtoMapper {
    fun map(product: Product): ProductDto = ProductDto(
        id = product.id,
        createdAt = product.createdAt,
        title = product.title,
        description = product.description,
        thumbnail = product.thumbnail,
        category = product.category,
        flavors = product.flavors,
        weight = product.weight,
        price = product.price,
        isPopular = product.isPopular,
        isDiscounted = product.isDiscounted,
        isNew = product.isNew,
    )
}
