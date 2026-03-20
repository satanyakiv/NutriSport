package com.nutrisport.cart

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.nutrisport.cart.model.CartItemUi
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class CartScreenTest {

  @Test
  fun `should display empty cart info when cart is empty`() = runComposeUiTest {
    setContent {
      CartScreen(
        cartItems = UiState.Content(Either.Right(emptyList())),
        onUpdateQuantity = { _, _, _, _ -> },
        onDeleteItem = { _, _, _ -> },
      )
    }

    onNodeWithText("Empty Cart").assertExists()
    onNodeWithText("Check some of our products.").assertExists()
  }

  @Test
  fun `should display cart items when cart has products`() = runComposeUiTest {
    val items = listOf(
      CartItemUi(
        cartItemId = "cart-1",
        productId = "prod-1",
        title = "WHEY PROTEIN",
        thumbnail = "",
        flavor = "Chocolate",
        quantity = 2,
        unitPrice = 29.99,
        formattedUnitPrice = "$29.99",
        formattedTotalPrice = "$59.98",
      ),
    )

    setContent {
      CartScreen(
        cartItems = UiState.Content(Either.Right(items)),
        onUpdateQuantity = { _, _, _, _ -> },
        onDeleteItem = { _, _, _ -> },
      )
    }

    onNodeWithText("WHEY PROTEIN").assertExists()
  }

  @Test
  fun `should display loading indicator`() = runComposeUiTest {
    setContent {
      CartScreen(
        cartItems = UiState.Loading,
        onUpdateQuantity = { _, _, _, _ -> },
        onDeleteItem = { _, _, _ -> },
      )
    }

    onNodeWithTag("loading_indicator").assertExists()
  }
}
