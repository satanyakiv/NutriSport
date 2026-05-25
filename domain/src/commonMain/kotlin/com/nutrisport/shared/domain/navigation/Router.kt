package com.nutrisport.shared.domain.navigation

import com.nutrisport.shared.navigation.Screen
import kotlinx.coroutines.flow.SharedFlow

/**
 * Navigation as a service.
 *
 * ViewModels depend on this contract, never on `NavController`. The implementation
 * lives in `:navigation` and emits [NavigationCommand]s through [commands]. A
 * `LaunchedEffect` inside the navigation host translates each command into the
 * underlying framework call.
 */
interface Router {
  val commands: SharedFlow<NavigationCommand>

  /**
   * Suspends until a collector is attached to [commands] (returns immediately if
   * one already is). Cold-start deeplink delivery awaits this before dispatching a
   * queued link, so the command reaches the live navigation host instead of a
   * bufferless flow that silently drops it.
   */
  suspend fun awaitReady()

  fun navigateTo(destination: Screen)

  /** Navigate to [destination] and clear every prior entry (typical post-Auth transition). */
  fun replaceWith(destination: Screen)

  fun back()

  fun popUpTo(destination: Screen, inclusive: Boolean = false)

  fun popToRoot()
}
