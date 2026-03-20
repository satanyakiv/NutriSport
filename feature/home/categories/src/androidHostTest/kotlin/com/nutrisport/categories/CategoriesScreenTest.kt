package com.nutrisport.categories

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class CategoriesScreenTest {

  @Test
  fun `should display all product categories`() = runComposeUiTest {
    setContent {
      CategoriesScreen(goToCategoriesSearch = {})
    }

    onNodeWithText("Protein").assertExists()
    onNodeWithText("Creatine").assertExists()
    onNodeWithText("Pre-Workout").assertExists()
    onNodeWithText("Gainers").assertExists()
    onNodeWithText("Accessories").assertExists()
  }
}
