package com.nutrisport.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.categories.component.CategoryCard
import com.nutrisport.shared.domain.ProductCategory

@Composable
fun CategoriesScreen(
  modifier: Modifier = Modifier,
  goToCategoriesSearch: (String) -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    ProductCategory.entries.forEach { category ->
      CategoryCard(
        category = category,
        onClick = { goToCategoriesSearch(category.name) }
      )
    }
  }
}