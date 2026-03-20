package com.nutrisport.checkout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.nutrisport.shared.domain.Country
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class CheckoutScreenTest {

  @Test
  fun `should display checkout form with total amount`() = runComposeUiTest {
    setContent {
      CheckoutScreen(
        totalAmount = 59.98,
        navigateBack = {},
        navigateToPaymentCompleted = { _, _ -> },
        screenState = CheckoutScreenState(
          firstName = "John",
          lastName = "Doe",
          email = "john@example.com",
          country = Country.Serbia,
        ),
        isFormValid = false,
        onCountrySelect = {},
        onFirstNameChange = {},
        onLastNameChange = {},
        onCityChange = {},
        onPostalCodeChange = {},
        onAddressChange = {},
        onPhoneNumberChange = {},
        onPayOnDelivery = { _, _ -> },
      )
    }

    onNodeWithText("Checkout").assertExists()
    onNodeWithText("$59.98", substring = true).assertExists()
    onNodeWithText("Pay on Delivery").assertExists()
  }

  @Test
  fun `should display Pay on Delivery when form is valid`() = runComposeUiTest {
    setContent {
      CheckoutScreen(
        totalAmount = 29.99,
        navigateBack = {},
        navigateToPaymentCompleted = { _, _ -> },
        screenState = CheckoutScreenState(
          firstName = "John",
          lastName = "Doe",
          email = "john@example.com",
          country = Country.Serbia,
        ),
        isFormValid = true,
        onCountrySelect = {},
        onFirstNameChange = {},
        onLastNameChange = {},
        onCityChange = {},
        onPostalCodeChange = {},
        onAddressChange = {},
        onPhoneNumberChange = {},
        onPayOnDelivery = { _, _ -> },
      )
    }

    onNodeWithText("Pay on Delivery").assertExists()
  }
}
