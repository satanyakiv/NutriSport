package com.nutrisport.shared.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.component.dialog.CountryPickerDialog
import com.nutrisport.shared.domain.Country

@Composable
fun ProfileForm(
  modifier: Modifier = Modifier,
  country: Country,
  onCountrySelect: (Country) -> Unit,
  firstName: String,
  firstNameChange: (String) -> Unit,
  lastName: String,
  lastNameChange: (String) -> Unit,
  email: String,
  city: String?,
  onCityChange: (String) -> Unit,
  postalCode: Int?,
  postalCodeChange: (Int?) -> Unit,
  address: String?,
  onAddressChange: (String) -> Unit,
  phoneNumber: String?,
  onPhoneNumberChange: (String) -> Unit,
) {
  var showCountryDialog by remember { mutableStateOf(false) }

  AnimatedVisibility(
    visible = showCountryDialog
  ) {
    CountryPickerDialog(
      country = country,
      onDismiss = { showCountryDialog = false },
      onConfirmClick = { selectedCountry ->
        showCountryDialog = false
        onCountrySelect(selectedCountry)
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
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
      value = city.orEmpty(),
      onValueChange = onCityChange,
      placeholder = "City",
      error = city?.length !in 3..50
    )
    CustomTextField(
      value = postalCode?.toString() ?: "",
      onValueChange = { postalCodeChange(it.toIntOrNull()) },
      placeholder = "Postal code",
      error = postalCode.toString().length !in 3..8,
    )
    CustomTextField(
      value = address.orEmpty(),
      onValueChange = onAddressChange,
      placeholder = "Address",
      error = address?.length !in 3..50
    )
    Row(
      verticalAlignment = CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      AlertTextField(
        text = "+${country.dialCode}",
        icon = country.flag,
        onClick = {
          showCountryDialog = true
        },
      )
      Spacer(modifier = Modifier.width(12.dp))
      CustomTextField(
        value = phoneNumber.orEmpty(),
        onValueChange = onPhoneNumberChange,
        placeholder = "Phone number",
        error = phoneNumber?.length !in 5..15
      )
    }
  }
}