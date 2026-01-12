package com.nutrisport.shared.domain

import androidx.compose.ui.graphics.Color
import com.nutrisport.shared.CategoryBlue
import com.nutrisport.shared.CategoryGreen
import com.nutrisport.shared.CategoryPurple
import com.nutrisport.shared.CategoryRed
import com.nutrisport.shared.CategoryYellow
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
)

enum class ProductCategory(
  val title: String,
  val color: Color
) {
  Protein(
    title = "Protein",
    color = CategoryYellow
  ),
  Creatine(
    title = "Creatine",
    color = CategoryBlue,
  ),
  PreWorkout(
    title = "Pre-Workout",
    color = CategoryGreen,
  ),
  Gainers(
    title = "Gainers",
    color = CategoryPurple,
  ),
  Accessories(
    title = "Accessories",
    color = CategoryRed,
  );
}

fun String.valueOfProductCategory(): ProductCategory {
  return when(lowercase().filter { it.isLetter() }) {
    "protein" -> Protein
    "creatine" -> Creatine
    "preworkout" -> PreWorkout
    "gainers" -> Gainers
    "accessories" -> Accessories
    else -> throw IllegalArgumentException("Invalid product category: $this")
  }
}