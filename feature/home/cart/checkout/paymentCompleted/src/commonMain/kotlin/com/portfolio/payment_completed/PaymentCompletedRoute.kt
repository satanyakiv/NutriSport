package com.portfolio.payment_completed

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaymentCompletedRoute() {
  val viewModel = koinViewModel<PaymentViewModel>()
  PaymentCompletedScreen(
    screenState = viewModel.screenState,
    navigateBack = viewModel::navigateBack,
  )
}
