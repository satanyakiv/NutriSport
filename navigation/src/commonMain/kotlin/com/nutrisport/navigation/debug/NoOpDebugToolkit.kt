package com.nutrisport.navigation.debug

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

/** Default implementation with no debug instrumentation. Used in release builds and iOS. */
class NoOpDebugToolkit : DebugToolkit {
  override fun initialize() = Unit

  @Composable
  override fun rememberNavController(): NavHostController =
    rememberNavController()

  @Composable
  override fun WrapRootContent(content: @Composable () -> Unit) = content()

  override fun log(message: String) = Unit
}
