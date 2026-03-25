package com.nutrisport.details

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.nutrisport.details.model.DetailsScreenState
import com.nutrisport.details.model.ProductUi
import com.nutrisport.shared.domain.ConnectivityStatus
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class DetailsScreenTest {

  private fun testProductUi(
    hasFlavors: Boolean = true,
    flavors: List<String> = listOf("Chocolate", "Vanilla"),
    previousPrice: String? = null,
    isPriceIncrease: Boolean = false,
  ) = ProductUi(
    id = "prod-1",
    title = "WHEY PROTEIN",
    description = "High quality protein",
    thumbnail = "",
    category = "Protein",
    formattedPrice = "$29.99",
    flavors = flavors,
    hasFlavors = hasFlavors,
    formattedWeight = "1000g",
    isPopular = false,
    isDiscounted = false,
    isNew = false,
    previousPrice = previousPrice,
    isPriceIncrease = isPriceIncrease,
  )

  private fun testState(
    product: UiState<ProductUi> = UiState.Content(Either.Right(testProductUi())),
    connectivity: ConnectivityStatus = ConnectivityStatus.Available,
    showReconnectedPrompt: Boolean = false,
  ) = DetailsScreenState(
    product = product,
    connectivity = connectivity,
    showReconnectedPrompt = showReconnectedPrompt,
  )

  @Test
  fun `should display product details`() = runComposeUiTest {
    setContent {
      DetailsScreen(
        goBack = {},
        state = testState(),
        quantity = 1,
        selectedFlavor = "Chocolate",
        onUpdateQuantity = {},
        onUpdateFlavor = {},
        onAddItemToCart = { _, _ -> },
        onRefresh = {},
        onDismissReconnected = {},
        onAcknowledgePriceChange = {},
      )
    }

    onNodeWithText("Add to Cart").assertExists()
  }

  @Test
  fun `should display offline banner when unavailable`() = runComposeUiTest {
    setContent {
      DetailsScreen(
        goBack = {},
        state = testState(connectivity = ConnectivityStatus.Unavailable),
        quantity = 1,
        selectedFlavor = null,
        onUpdateQuantity = {},
        onUpdateFlavor = {},
        onAddItemToCart = { _, _ -> },
        onRefresh = {},
        onDismissReconnected = {},
        onAcknowledgePriceChange = {},
      )
    }

    onNodeWithTag("offline_banner").assertExists()
  }

  @Test
  fun `should display reconnected prompt`() = runComposeUiTest {
    setContent {
      DetailsScreen(
        goBack = {},
        state = testState(showReconnectedPrompt = true),
        quantity = 1,
        selectedFlavor = null,
        onUpdateQuantity = {},
        onUpdateFlavor = {},
        onAddItemToCart = { _, _ -> },
        onRefresh = {},
        onDismissReconnected = {},
        onAcknowledgePriceChange = {},
      )
    }

    onNodeWithTag("reconnected_prompt").assertExists()
    onNodeWithText("Refresh prices").assertExists()
  }

  @Test
  fun `should display price change banner`() = runComposeUiTest {
    val productWithPriceChange = testProductUi(
      previousPrice = "$24.99",
      isPriceIncrease = true,
    )
    setContent {
      DetailsScreen(
        goBack = {},
        state = testState(
          product = UiState.Content(Either.Right(productWithPriceChange)),
        ),
        quantity = 1,
        selectedFlavor = "Chocolate",
        onUpdateQuantity = {},
        onUpdateFlavor = {},
        onAddItemToCart = { _, _ -> },
        onRefresh = {},
        onDismissReconnected = {},
        onAcknowledgePriceChange = {},
      )
    }

    onNodeWithTag("price_change_banner").assertExists()
    onNodeWithText("$24.99").assertExists()
  }

  @Test
  fun `should display error state`() = runComposeUiTest {
    setContent {
      DetailsScreen(
        goBack = {},
        state = testState(
          product = UiState.Content(
            Either.Left(AppError.NotFound("Product not found")),
          ),
        ),
        quantity = 1,
        selectedFlavor = null,
        onUpdateQuantity = {},
        onUpdateFlavor = {},
        onAddItemToCart = { _, _ -> },
        onRefresh = {},
        onDismissReconnected = {},
        onAcknowledgePriceChange = {},
      )
    }

    onNodeWithText("Oops!").assertExists()
    onNodeWithText("Product not found").assertExists()
  }
}
