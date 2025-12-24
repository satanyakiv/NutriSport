package com.nutrisport.shared.composent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.composent.dialog.CountryPickerDialog
import com.nutrisport.shared.domain.Country

@Composable
fun ProfileForm(
  modifier: Modifier = Modifier,
  firstName: String,
  firstNameChange: (String) -> Unit,
  lastName: String,
  lastNameChange: (String) -> Unit,
  email: String,
  city: String,
  onCityChange: (String) -> Unit,
  postalCode: Int?,
  postalCodeChange: (Int?) -> Unit,
  address: String,
  onAddressChange: (String) -> Unit,
  phoneNumber: String,
  onPhoneNumberChange: (String) -> Unit,
) {
  CountryPickerDialog(
    country = Country.Serbia,
    onDismiss = {},
    onConfirmClick = {}
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp, vertical = 12.dp)
      .imePadding(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    CustomTextField(
      value = firstName,
      onValueChange = firstNameChange,
      placeholder = "First name",
      error = firstName.length !in 3..50
    )
    CustomTextField(
      value = lastName,
      onValueChange = lastNameChange,
      placeholder = "Last name",
      error = lastName.length !in 3..50
    )
    CustomTextField(
      value = email,
      onValueChange = {},
      enabled = false,
      placeholder = "Email",
    )
    CustomTextField(
      value = city,
      onValueChange = onCityChange,
      placeholder = "City",
      error = city.length !in 3..50
    )
    CustomTextField(
      value = postalCode?.toString() ?: "",
      onValueChange = { postalCodeChange(it.toIntOrNull()) },
      placeholder = "Postal code",
      error = postalCode.toString().length !in 3..8,
    )
    CustomTextField(
      value = address,
      onValueChange = onAddressChange,
      placeholder = "Address",
      error = address.length !in 3..50
    )
    CustomTextField(
      value = phoneNumber,
      onValueChange = onPhoneNumberChange,
      placeholder = "Phone number",
      error = phoneNumber.length !in 5..15
    )
  }
}