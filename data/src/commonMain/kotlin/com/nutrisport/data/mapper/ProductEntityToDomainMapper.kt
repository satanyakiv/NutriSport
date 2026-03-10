package com.nutrisport.data.mapper

import com.nutrisport.database.entity.ProductEntity
import com.nutrisport.shared.domain.Product
import kotlinx.serialization.json.Json

class ProductEntityToDomainMapper {
  fun map(entity: ProductEntity): Product = Product(
    id = entity.id,
    createdAt = entity.createdAt,
    title = entity.title,
    description = entity.description,
    thumbnail = entity.thumbnail,
    category = entity.category,
    flavors = entity.flavors?.let { Json.decodeFromString(it) },
    weight = entity.weight,
    price = entity.price,
    isPopular = entity.isPopular,
    isDiscounted = entity.isDiscounted,
    isNew = entity.isNew,
  )

  fun map(entities: List<ProductEntity>): List<Product> = entities.map { map(it) }
}
