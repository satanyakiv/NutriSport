package com.portfolio.categories_search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nutrisport.shared.domain.ProductCategory
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoriesSearchRoute(
  category: ProductCategory,
) {
  val viewModel = koinViewModel<CategorySearchViewModel>()
  val filteredProducts by viewModel.filteredProducts.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()

  CategorySearchScreen(
    category = category,
    navigateToDetails = viewModel::navigateToDetails,
    navigateBack = viewModel::navigateBack,
    filteredProducts = filteredProducts,
    searchQuery = searchQuery,
    onSearchQueryChange = viewModel::updateSearchQuery,
  )
}
