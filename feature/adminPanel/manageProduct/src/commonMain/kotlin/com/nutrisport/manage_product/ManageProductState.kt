package com.nutrisport.manage_product

import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class ManageProductState(
  val id: String = Uuid.random().toHexString(),
  val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
  val title: String = "",
  val description: String = "",
  val thumbnail: String = "",
  val category: ProductCategory = ProductCategory.Protein,
  val flavors: String? = null,
  val weight: Int? = null,
  val price: Double = 0.0,
  val isNew: Boolean = false,
  val isPopular: Boolean = false,
  val isDiscounted: Boolean = false,
) {
  fun toProduct(): Product = Product(
    id = id,
    createdAt = createdAt,
    title = title,
    description = description,
    thumbnail = thumbnail,
    category = category.title,
    flavors = flavors?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() },
    weight = weight,
    price = price,
    isNew = isNew,
    isPopular = isPopular,
    isDiscounted = isDiscounted,
  )
}
