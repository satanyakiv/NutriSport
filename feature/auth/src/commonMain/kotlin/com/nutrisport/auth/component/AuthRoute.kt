package com.nutrisport.auth.component

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthRoute() {
  val viewModel = koinViewModel<AuthViewModel>()
  AuthScreen(
    goToHome = viewModel::goToHome,
    onCreateCustomer = viewModel::createCustomer,
  )
}
