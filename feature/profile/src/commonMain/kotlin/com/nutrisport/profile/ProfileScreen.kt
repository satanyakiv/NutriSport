package com.nutrisport.profile

import ContentWithMessageBar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.nutrisport.shared.component.InfoCard
import com.nutrisport.shared.component.LoadingCard
import com.nutrisport.shared.component.PrimaryButton
import com.nutrisport.shared.component.ProfileForm
import com.nutrisport.shared.domain.Country
import com.nutrisport.shared.util.DisplayResult
import com.nutrisport.shared.util.UiState
import org.jetbrains.compose.resources.painterResource
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  goBack: () -> Unit,
  screenReady: UiState<Unit>,
  screenState: ProfileScreenState,
  isFormValid: Boolean,
  onCountrySelect: (Country) -> Unit,
  onFirstNameChange: (String) -> Unit,
  onLastNameChange: (String) -> Unit,
  onCityChange: (String) -> Unit,
  onPostalCodeChange: (Int?) -> Unit,
  onAddressChange: (String) -> Unit,
  onPhoneNumberChange: (String) -> Unit,
  onUpdateCustomer: (() -> Unit, (String) -> Unit) -> Unit,
) {
  val messageBarState = rememberMessageBarState()

  Scaffold(
    containerColor = Surface,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "My Profile",
            fontFamily = BebasNeueFont(),
            fontSize = FontSize.LARGE,
            color = TextPrimary
          )
        },
        navigationIcon = {
          IconButton(onClick = goBack) {
            Icon(
              painter = painterResource(Resources.Icon.BackArrow),
              contentDescription = "Back Arrow icon",
              tint = IconPrimary
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
          .padding(horizontal = 24.dp)
          .padding(
            top = 12.dp,
            bottom = 24.dp
          )
          .imePadding()
      ) {
        screenReady.DisplayResult(
          onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
          onSuccess = {
            Column(modifier = Modifier.fillMaxSize()) {
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
              Spacer(modifier = Modifier.height(12.dp))
              PrimaryButton(
                text = "Update",
                icon = Resources.Icon.Checkmark,
                enabled = isFormValid,
                onClick = {
                  onUpdateCustomer(
                    { messageBarState.addSuccess("Successfully updated!") },
                    { message -> messageBarState.addError(message) },
                  )
                }
              )
            }
          },
          onError = { message ->
            InfoCard(
              image = Resources.Image.Cat,
              title = "Oops!",
              subtitle = message
            )
          }
        )
      }
    }
  }
}