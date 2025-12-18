package com.nutrisport.auth.component

import ContentWithMessageBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.nutrisport.shared.Alpha
import com.nutrisport.shared.BebasNeuFont
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextSecondary
import com.nutrisport.shared.TextWhite
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@Composable
fun AuthScreen() {
  val messageBarState = rememberMessageBarState()
  val viewModel = koinViewModel<AuthViewModel>()
  var loadingState by remember { mutableStateOf(false) }

  Scaffold { padding ->
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
      successContentColor = TextPrimary,
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        Column(
          modifier = Modifier.weight(2f),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            modifier = Modifier.fillMaxWidth(),
            text = "NUTRISPORT",
            textAlign = TextAlign.Center,
            fontFamily = BebasNeuFont(),
            fontSize = FontSize.EXTRA_LARGE,
            color = TextSecondary,
          )
          Text(
            modifier = Modifier
              .fillMaxWidth()
              .alpha(Alpha.HALF),
            text = "Sign in to continue",
            textAlign = TextAlign.Center,
            fontSize = FontSize.EXTRA_REGULAR,
            color = TextSecondary,
          )
        }
        GoogleButtonUiContainerFirebase(
          linkAccount = false,
          onResult = { result ->
            result.onSuccess { user ->
              viewModel.createCustomer(
                user = user,
                onSuccess = { messageBarState.addSuccess("Auth Successful") },
                onError = { e -> messageBarState.addError(e) }
              )
              loadingState = false

            }.onFailure { error ->
              if (error.message?.contains("A network error") == true) {
                messageBarState.addError("Internet connection unavailable")
              } else if (error.message?.contains("Idtoken is null") == true) {
                messageBarState.addError("Sign in cancelled")
              } else {
                messageBarState.addError(error.message ?: "Something went wrong")
              }
              loadingState = false
            }
          }
        ) {
          GoogleButton(
            modifier = Modifier.padding(horizontal = 16.dp),
            loading = loadingState,
            onClicked = {
              loadingState = true
              this@GoogleButtonUiContainerFirebase.onClick()
            }
          )
        }
      }
    }
  }
}
