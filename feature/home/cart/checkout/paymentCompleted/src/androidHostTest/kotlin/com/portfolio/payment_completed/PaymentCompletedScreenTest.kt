package com.portfolio.payment_completed

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class PaymentCompletedScreenTest {

  @Test
  fun `should display success message when payment succeeds`() = runComposeUiTest {
    setContent {
      PaymentCompletedScreen(
        screenState = UiState.Content(Either.Right(Unit)),
        navigateBack = {},
      )
    }

    onNodeWithText("Success!").assertExists()
    onNodeWithText("Your purchase is on the way.").assertExists()
    onNodeWithText("Go back").assertExists()
  }

  @Test
  fun `should display error message when payment fails`() = runComposeUiTest {
    setContent {
      PaymentCompletedScreen(
        screenState = UiState.Content(
          Either.Left(AppError.Network("Connection failed")),
        ),
        navigateBack = {},
      )
    }

    onNodeWithText("Oops!").assertExists()
    onNodeWithText("Connection failed").assertExists()
    onNodeWithText("Go back").assertExists()
  }
}
