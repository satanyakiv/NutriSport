package com.nutrisport.shared.domain

import com.nutrisport.shared.domain.ProductCategory.Accessories
import com.nutrisport.shared.domain.ProductCategory.Creatine
import com.nutrisport.shared.domain.ProductCategory.Gainers
import com.nutrisport.shared.domain.ProductCategory.PreWorkout
import com.nutrisport.shared.domain.ProductCategory.Protein
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class Product(
  val id: String,
  val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
  val title: String,
  val description: String,
  val thumbnail: String,
  val category: String,
  val flavors: List<String>? = null,
  val weight: Int? = null,
  val price: Double,
  val isPopular: Boolean = false,
  val isDiscounted: Boolean = false,
  val isNew: Boolean = false,
  val previouslyKnownPrice: Double? = null,
)

enum class ProductCategory(val title: String) {
  Protein(title = "Protein"),
  Creatine(title = "Creatine"),
  PreWorkout(title = "Pre-Workout"),
  Gainers(title = "Gainers"),
  Accessories(title = "Accessories"),
}

fun String.valueOfProductCategory(): ProductCategory {
  return when (lowercase().filter { it.isLetter() }) {
    "protein" -> Protein
    "creatine" -> Creatine
    "preworkout" -> PreWorkout
    "gainers" -> Gainers
    "accessories" -> Accessories
    else -> throw IllegalArgumentException("Invalid product category: $this")
  }
}
