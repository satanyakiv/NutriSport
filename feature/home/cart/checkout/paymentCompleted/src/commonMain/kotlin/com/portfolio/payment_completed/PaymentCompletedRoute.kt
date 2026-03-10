package com.portfolio.payment_completed

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaymentCompletedRoute(navigateBack: () -> Unit) {
  val viewModel = koinViewModel<PaymentViewModel>()
  PaymentCompletedScreen(
    screenState = viewModel.screenState,
    navigateBack = navigateBack,
  )
}
