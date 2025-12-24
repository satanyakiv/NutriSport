package com.nutrisport.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nutrisport.shared.Surface
import com.nutrisport.shared.composent.ProfileForm

@Composable
fun ProfileScreen(
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .background(Surface)
      .systemBarsPadding()
  ) {
    ProfileForm(
      modifier = modifier,
      firstName = "Miranda Jennings",
      firstNameChange = {},
      lastName = "",
      lastNameChange = {},
      email = "marva.house@example.com",
      city = "Greystone",
      onCityChange = {},
      postalCode = 6615,
      postalCodeChange = {},
      address = "volutpat",
      onAddressChange = {},
      phoneNumber = "(682) 257-4641",
      onPhoneNumberChange = {},

      )
  }
}