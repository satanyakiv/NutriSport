package com.nutrisport.data.mapper

import com.nutrisport.data.dto.ProductDto
import com.nutrisport.database.entity.ProductEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProductDtoToEntityMapper {
  fun map(
    dto: ProductDto,
    currentEntity: ProductEntity? = null,
  ): ProductEntity = ProductEntity(
    id = dto.id,
    createdAt = dto.createdAt,
    title = dto.title,
    description = dto.description,
    thumbnail = dto.thumbnail,
    category = dto.category,
    flavors = dto.flavors?.let { Json.encodeToString(it) },
    weight = dto.weight,
    price = dto.price,
    isPopular = dto.isPopular,
    isDiscounted = dto.isDiscounted,
    isNew = dto.isNew,
    previouslyKnownPrice = resolvePreviousPrice(dto.price, currentEntity),
  )

  fun map(dtos: List<ProductDto>): List<ProductEntity> = dtos.map { map(it) }

  private fun resolvePreviousPrice(
    newPrice: Double,
    currentEntity: ProductEntity?,
  ): Double? {
    if (currentEntity == null) return null
    if (currentEntity.price == newPrice) return currentEntity.previouslyKnownPrice
    return currentEntity.price
  }
}
