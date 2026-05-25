package com.nutrisport.navigation

import com.nutrisport.shared.domain.navigation.NavigationCommand
import com.nutrisport.shared.domain.navigation.Router
import com.nutrisport.shared.navigation.Screen
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

/**
 * Default [Router] implementation. Lives as a Koin `single` and is shared between
 * every ViewModel and the navigation host. The host (`SetupNavGraph`) collects
 * [commands] and translates each into a `NavController` call.
 *
 * `extraBufferCapacity` keeps short bursts of commands (rare, but possible during
 * cold-start deeplink drain) from being dropped.
 */
class DefaultRouter : Router {
  private val _commands = MutableSharedFlow<NavigationCommand>(
    extraBufferCapacity = COMMAND_BUFFER,
    onBufferOverflow = BufferOverflow.SUSPEND,
  )

  override val commands: SharedFlow<NavigationCommand> = _commands.asSharedFlow()

  /**
   * Ready once `SetupNavGraph` attaches its `commands` collector
   * (`subscriptionCount` goes positive). Cold-start deeplink delivery awaits this
   * so a drained command reaches the live NavController instead of a bufferless
   * flow.
   */
  override suspend fun awaitReady() {
    _commands.subscriptionCount.first { it > 0 }
  }

  override fun navigateTo(destination: Screen) {
    _commands.tryEmit(NavigationCommand.NavigateTo(destination))
  }

  override fun replaceWith(destination: Screen) {
    _commands.tryEmit(NavigationCommand.Replace(destination))
  }

  override fun back() {
    _commands.tryEmit(NavigationCommand.Back)
  }

  override fun popUpTo(destination: Screen, inclusive: Boolean) {
    _commands.tryEmit(NavigationCommand.PopUpTo(destination, inclusive))
  }

  override fun popToRoot() {
    _commands.tryEmit(NavigationCommand.PopToRoot)
  }

  private companion object {
    const val COMMAND_BUFFER = 16
  }
}
