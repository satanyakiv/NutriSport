package com.nutrisport.checkout

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckoutRoute(
  totalAmount: Double,
  navigateBack: () -> Unit,
  navigateToPaymentCompleted: (Boolean?, String?) -> Unit,
) {
  val viewModel = koinViewModel<CheckoutViewModel>()
  CheckoutScreen(
    totalAmount = totalAmount,
    navigateBack = navigateBack,
    navigateToPaymentCompleted = navigateToPaymentCompleted,
    screenState = viewModel.screenState,
    isFormValid = viewModel.isFormValid,
    onCountrySelect = viewModel::updateCountry,
    onFirstNameChange = viewModel::updateFirstName,
    onLastNameChange = viewModel::updateLastName,
    onCityChange = viewModel::updateCity,
    onPostalCodeChange = viewModel::updatePostalCode,
    onAddressChange = viewModel::updateAddress,
    onPhoneNumberChange = viewModel::updatePhoneNumber,
    onPayOnDelivery = viewModel::payOnDelivery,
  )
}
