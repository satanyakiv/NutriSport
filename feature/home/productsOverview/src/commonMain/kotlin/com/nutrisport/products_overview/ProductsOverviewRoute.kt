package com.nutrisport.products_overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProductsOverviewRoute(goToDetails: (String) -> Unit) {
  val viewModel = koinViewModel<ProductsOverviewViewModel>()
  val products by viewModel.products.collectAsState()

  ProductsOverviewScreen(
    products = products,
    goToDetails = goToDetails,
  )
}
