package com.nutrisport.products_overview

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.nutrisport.shared.test.fakeProduct
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class ProductsOverviewScreenTest {

  @Test
  fun `should display products with discounted section`() = runComposeUiTest {
    val products = listOf(
      fakeProduct(id = "1", title = "NEW WHEY", isNew = true),
      fakeProduct(id = "2", title = "DISCOUNT CREATINE", isDiscounted = true),
    )

    setContent {
      ProductsOverviewScreen(
        products = UiState.Content(Either.Right(products)),
        goToDetails = {},
      )
    }

    onNodeWithText("Discounted Products").assertExists()
  }

  @Test
  fun `should display empty state when product list is empty`() = runComposeUiTest {
    setContent {
      ProductsOverviewScreen(
        products = UiState.Content(Either.Right(emptyList())),
        goToDetails = {},
      )
    }

    onNodeWithText("Nothing here").assertExists()
    onNodeWithText("Empty product list.").assertExists()
  }

  @Test
  fun `should display loading indicator`() = runComposeUiTest {
    setContent {
      ProductsOverviewScreen(
        products = UiState.Loading,
        goToDetails = {},
      )
    }

    onNodeWithTag("loading_indicator").assertExists()
  }

  @Test
  fun `should display error state`() = runComposeUiTest {
    setContent {
      ProductsOverviewScreen(
        products = UiState.Content(
          Either.Left(AppError.Network("Server error")),
        ),
        goToDetails = {},
      )
    }

    onNodeWithText("Oops!").assertExists()
    onNodeWithText("Server error").assertExists()
  }
}
