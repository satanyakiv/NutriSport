package com.nutrisport.data.mapper

import com.nutrisport.data.dto.ProductDto
import com.nutrisport.shared.domain.Product

class ProductDtoToDomainMapper {
    fun map(dto: ProductDto): Product = Product(
        id = dto.id,
        createdAt = dto.createdAt,
        title = dto.title,
        description = dto.description,
        thumbnail = dto.thumbnail,
        category = dto.category,
        flavors = dto.flavors,
        weight = dto.weight,
        price = dto.price,
        isPopular = dto.isPopular,
        isDiscounted = dto.isDiscounted,
        isNew = dto.isNew,
    )

    fun map(dtos: List<ProductDto>): List<Product> = dtos.map { map(it) }
}
