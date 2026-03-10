package com.nutrisport.checkout


import ContentWithMessageBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextWhite
import com.nutrisport.shared.component.PrimaryButton
import com.nutrisport.shared.component.ProfileForm
import com.nutrisport.shared.domain.Country
import org.jetbrains.compose.resources.painterResource
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
  totalAmount: Double,
  navigateBack: () -> Unit,
  navigateToPaymentCompleted: (Boolean?, String?) -> Unit,
  screenState: CheckoutScreenState,
  isFormValid: Boolean,
  onCountrySelect: (Country) -> Unit,
  onFirstNameChange: (String) -> Unit,
  onLastNameChange: (String) -> Unit,
  onCityChange: (String) -> Unit,
  onPostalCodeChange: (Int?) -> Unit,
  onAddressChange: (String) -> Unit,
  onPhoneNumberChange: (String) -> Unit,
  onPayOnDelivery: (() -> Unit, (String) -> Unit) -> Unit,
) {
  val messageBarState = rememberMessageBarState()

  Scaffold(
    containerColor = Surface,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Checkout",
            fontFamily = BebasNeueFont(),
            fontSize = FontSize.LARGE,
            color = TextPrimary
          )
        },
        actions = {
          Text(
            text = "$${totalAmount}",
            fontSize = FontSize.EXTRA_MEDIUM,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.width(16.dp))
        },
        navigationIcon = {
          IconButton(onClick = navigateBack) {
            Icon(
              painter = painterResource(Resources.Icon.BackArrow),
              contentDescription = "Back arrow icon",
              tint = IconPrimary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Surface,
          scrolledContainerColor = Surface,
          navigationIconContentColor = IconPrimary,
          titleContentColor = TextPrimary,
          actionIconContentColor = IconPrimary
        )
      )
    }
  ) { padding ->
    ContentWithMessageBar(
      contentBackgroundColor = Surface,
      modifier = Modifier
        .padding(
          top = padding.calculateTopPadding(),
          bottom = padding.calculateBottomPadding()
        ),
      messageBarState = messageBarState,
      errorMaxLines = 2,
      errorContainerColor = SurfaceError,
      errorContentColor = TextWhite,
      successContainerColor = SurfaceBrand,
      successContentColor = TextPrimary
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(
            top = 12.dp,
            bottom = 24.dp
          )
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        ProfileForm(
          modifier = Modifier.weight(1f),
          country = screenState.country,
          onCountrySelect = onCountrySelect,
          firstName = screenState.firstName,
          onFirstNameChange = onFirstNameChange,
          lastName = screenState.lastName,
          onLastNameChange = onLastNameChange,
          email = screenState.email,
          city = screenState.city,
          onCityChange = onCityChange,
          postalCode = screenState.postalCode,
          postalCodeChange = onPostalCodeChange,
          address = screenState.address,
          onAddressChange = onAddressChange,
          phoneNumber = screenState.phoneNumber?.number,
          onPhoneNumberChange = onPhoneNumberChange,
        )
        Column {
          PrimaryButton(
            text = "Pay on Delivery",
            icon = Resources.Icon.ShoppingCart,
            secondary = true,
            enabled = isFormValid,
            onClick = {
              onPayOnDelivery(
                { navigateToPaymentCompleted(true, null) },
                { message -> navigateToPaymentCompleted(null, message) },
              )
            }
          )
        }
      }
    }
  }
}