package com.nutrisport.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nutrisport.shared.util.UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeGraphRoute() {
  val viewModel = koinViewModel<HomeGraphViewModel>()
  val customer by viewModel.customer.collectAsState()
  val totalAmount by viewModel.totalAmountFlow.collectAsState(UiState.Loading)

  HomeGraphScreen(
    navigateToAuth = viewModel::navigateToAuth,
    navigateToProfile = viewModel::navigateToProfile,
    navigateToAdminPanel = viewModel::navigateToAdminPanel,
    navigateToDetails = viewModel::navigateToDetails,
    navigateToCategorySearch = viewModel::navigateToCategorySearch,
    navigateToCheckout = viewModel::navigateToCheckout,
    customer = customer,
    totalAmount = totalAmount,
    onSignOut = viewModel::signOut,
  )
}
