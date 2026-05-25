package com.nutrisport.shared.test

import com.nutrisport.shared.domain.navigation.NavigationCommand
import com.nutrisport.shared.domain.navigation.Router
import com.nutrisport.shared.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * In-memory [Router] for unit tests. Records every emitted [NavigationCommand]
 * into [recordedCommands] so tests can assert on the navigation contract
 * without spinning up a real `NavController`.
 *
 * Example:
 * ```kotlin
 * val router = FakeRouter()
 * val vm = AuthViewModel(customerRepository = fakeRepo, router = router)
 * vm.goToHome()
 * assertThat(router.recordedCommands).containsExactly(
 *     NavigationCommand.Replace(Screen.HomeGraph),
 * )
 * ```
 */
class FakeRouter : Router {
  private val _commands = MutableSharedFlow<NavigationCommand>(extraBufferCapacity = 16)
  override val commands: SharedFlow<NavigationCommand> = _commands.asSharedFlow()

  /** Always ready: the fake has no real navigation host to wait for. */
  override suspend fun awaitReady() = Unit

  private val _recordedCommands = mutableListOf<NavigationCommand>()
  val recordedCommands: List<NavigationCommand> get() = _recordedCommands.toList()

  override fun navigateTo(destination: Screen) {
    record(NavigationCommand.NavigateTo(destination))
  }

  override fun replaceWith(destination: Screen) {
    record(NavigationCommand.Replace(destination))
  }

  override fun back() {
    record(NavigationCommand.Back)
  }

  override fun popUpTo(destination: Screen, inclusive: Boolean) {
    record(NavigationCommand.PopUpTo(destination, inclusive))
  }

  override fun popToRoot() {
    record(NavigationCommand.PopToRoot)
  }

  /** Drops the recording history. Use between phases of a multi-step test. */
  fun clear() {
    _recordedCommands.clear()
  }

  private fun record(command: NavigationCommand) {
    _recordedCommands += command
    _commands.tryEmit(command)
  }
}
