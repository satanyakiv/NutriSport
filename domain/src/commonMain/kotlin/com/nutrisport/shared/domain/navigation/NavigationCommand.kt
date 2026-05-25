package com.nutrisport.shared.domain.navigation

import com.nutrisport.shared.navigation.Screen

/**
 * Outbound navigation requests emitted by ViewModels through the [Router] and
 * consumed by the navigation host inside the `:navigation` module.
 *
 * Using a sealed type keeps navigation requests type-safe and serialisable.
 */
sealed class NavigationCommand {
  data class NavigateTo(val destination: Screen) : NavigationCommand()
  data class Replace(val destination: Screen) : NavigationCommand()
  data object Back : NavigationCommand()
  data class PopUpTo(val destination: Screen, val inclusive: Boolean = false) : NavigationCommand()
  data object PopToRoot : NavigationCommand()
}
