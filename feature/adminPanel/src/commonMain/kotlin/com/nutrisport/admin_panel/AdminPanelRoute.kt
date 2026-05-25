package com.nutrisport.admin_panel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AdminPanelRoute() {
  val viewModel = koinViewModel<AdminPanelViewModel>()
  val products by viewModel.filteredProducts.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()

  AdminPanelScreen(
    goBack = viewModel::goBack,
    goToManageProduct = viewModel::goToManageProduct,
    products = products,
    searchQuery = searchQuery,
    onSearchQueryChange = viewModel::updateSearchQuery,
  )
}
