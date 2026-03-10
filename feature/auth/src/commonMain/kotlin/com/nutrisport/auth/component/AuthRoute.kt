package com.nutrisport.auth.component

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthRoute(goToHome: () -> Unit) {
  val viewModel = koinViewModel<AuthViewModel>()
  AuthScreen(
    goToHome = goToHome,
    onCreateCustomer = viewModel::createCustomer,
  )
}
