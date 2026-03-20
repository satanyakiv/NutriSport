package com.nutrisport.details

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.nutrisport.details.model.ProductUi
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
  )

  @Test
  fun `should display product details`() = runComposeUiTest {
    setContent {
      DetailsScreen(
        goBack = {},
        product = UiState.Content(Either.Right(testProductUi())),
        quantity = 1,
        selectedFlavor = "Chocolate",
        onUpdateQuantity = {},
        onUpdateFlavor = {},
        onAddItemToCart = { _, _ -> },
      )
    }

    onNodeWithText("Add to Cart").assertExists()
  }

  @Test
  fun `should display Add to Cart when no flavor selected`() = runComposeUiTest {
    setContent {
      DetailsScreen(
        goBack = {},
        product = UiState.Content(Either.Right(testProductUi())),
        quantity = 1,
        selectedFlavor = null,
        onUpdateQuantity = {},
        onUpdateFlavor = {},
        onAddItemToCart = { _, _ -> },
      )
    }

    onNodeWithText("Add to Cart").assertExists()
  }

  @Test
  fun `should display Add to Cart when product has no flavors`() = runComposeUiTest {
    setContent {
      DetailsScreen(
        goBack = {},
        product = UiState.Content(
          Either.Right(testProductUi(hasFlavors = false, flavors = emptyList())),
        ),
        quantity = 1,
        selectedFlavor = null,
        onUpdateQuantity = {},
        onUpdateFlavor = {},
        onAddItemToCart = { _, _ -> },
      )
    }

    onNodeWithText("Add to Cart").assertExists()
  }

  @Test
  fun `should display error state`() = runComposeUiTest {
    setContent {
      DetailsScreen(
        goBack = {},
        product = UiState.Content(
          Either.Left(AppError.NotFound("Product not found")),
        ),
        quantity = 1,
        selectedFlavor = null,
        onUpdateQuantity = {},
        onUpdateFlavor = {},
        onAddItemToCart = { _, _ -> },
      )
    }

    onNodeWithText("Oops!").assertExists()
    onNodeWithText("Product not found").assertExists()
  }
}
