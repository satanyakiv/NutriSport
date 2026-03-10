package com.nutrisport.profile

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoute(goBack: () -> Unit) {
  val viewModel = koinViewModel<ProfileViewModel>()
  ProfileScreen(
    goBack = goBack,
    screenReady = viewModel.screenReady,
    screenState = viewModel.screenState,
    isFormValid = viewModel.isFormValid,
    onCountrySelect = viewModel::updateCountry,
    onFirstNameChange = viewModel::updateFirstName,
    onLastNameChange = viewModel::updateLastName,
    onCityChange = viewModel::updateCity,
    onPostalCodeChange = viewModel::updatePostalCode,
    onAddressChange = viewModel::updateAddress,
    onPhoneNumberChange = viewModel::updatePhoneNumber,
    onUpdateCustomer = viewModel::updateCustomer,
  )
}
