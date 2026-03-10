package com.nutrisport.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nutrisport.shared.util.UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeGraphRoute(
  navigateToAuth: () -> Unit,
  navigateToProfile: () -> Unit,
  navigateToAdminPanel: () -> Unit,
  navigateToDetails: (String) -> Unit,
  navigateToCategorySearch: (String) -> Unit,
  navigateToCheckout: (Double) -> Unit,
) {
  val viewModel = koinViewModel<HomeGraphViewModel>()
  val customer by viewModel.customer.collectAsState()
  val totalAmount by viewModel.totalAmountFlow.collectAsState(UiState.Loading)

  HomeGraphScreen(
    navigateToAuth = navigateToAuth,
    navigateToProfile = navigateToProfile,
    navigateToAdminPanel = navigateToAdminPanel,
    navigateToDetails = navigateToDetails,
    navigateToCategorySearch = navigateToCategorySearch,
    navigateToCheckout = navigateToCheckout,
    customer = customer,
    totalAmount = totalAmount,
    onSignOut = viewModel::signOut,
  )
}
