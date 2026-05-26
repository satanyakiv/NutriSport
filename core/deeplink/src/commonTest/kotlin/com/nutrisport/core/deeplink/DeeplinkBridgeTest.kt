package com.nutrisport.core.deeplink

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.nutrisport.core.deeplink.pending.InMemoryPendingDeeplinkStorage
import com.nutrisport.core.deeplink.registry.DeeplinkRegistry
import com.nutrisport.core.deeplink.resolver.DefaultDeeplinkResolver
import com.nutrisport.shared.domain.navigation.NavigationCommand
import com.nutrisport.shared.navigation.Screen
import com.nutrisport.shared.test.FakeDeeplinkAuthGate
import com.nutrisport.shared.test.FakeRouter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeeplinkBridgeTest {

  @Test
  fun `public link dispatches replace immediately`() = runTest {
    val h = newBridge(scope = this@runTest)

    h.bridge.handle("nutrisport://products/abc123", DeeplinkSource.CustomScheme)
    advanceUntilIdle()

    assertThat(h.router.recordedCommands).isEqualTo(
      listOf(NavigationCommand.Replace(Screen.Details(id = "abc123"))),
    )
    assertThat(h.pending.peek()).isNull()
  }

  @Test
  fun `signed-in gated link with a session dispatches replace`() = runTest {
    val h = newBridge(scope = this@runTest, signedIn = true)

    h.bridge.handle("nutrisport://profile", DeeplinkSource.CustomScheme)
    advanceUntilIdle()

    assertThat(h.router.recordedCommands).isEqualTo(
      listOf(NavigationCommand.Replace(Screen.Profile)),
    )
    assertThat(h.pending.peek()).isNull()
  }

  @Test
  fun `signed-in gated link with no session parks pending and replaces with Auth`() = runTest {
    val h = newBridge(scope = this@runTest, signedIn = false)

    h.bridge.handle("nutrisport://profile", DeeplinkSource.CustomScheme)
    advanceUntilIdle()

    assertThat(h.router.recordedCommands).isEqualTo(
      listOf(NavigationCommand.Replace(Screen.Auth)),
    )
    assertThat(h.pending.peek()).isEqualTo(Screen.Profile)
  }

  @Test
  fun `admin gated link with admin dispatches replace`() = runTest {
    val h = newBridge(scope = this@runTest, signedIn = true, admin = true)

    h.bridge.handle("nutrisport://admin", DeeplinkSource.CustomScheme)
    advanceUntilIdle()

    assertThat(h.router.recordedCommands).isEqualTo(
      listOf(NavigationCommand.Replace(Screen.AdminPanel)),
    )
  }

  @Test
  fun `admin gated link without admin is dropped`() = runTest {
    val h = newBridge(scope = this@runTest, signedIn = true, admin = false)

    h.bridge.handle("nutrisport://admin", DeeplinkSource.CustomScheme)
    advanceUntilIdle()

    assertThat(h.router.recordedCommands).isEmpty()
    assertThat(h.pending.peek()).isNull()
  }

  @Test
  fun `push payload routes through the same code path`() = runTest {
    val h = newBridge(scope = this@runTest)

    h.bridge.handlePush(mapOf("deeplink" to "nutrisport://products/xyz"))
    advanceUntilIdle()

    assertThat(h.router.recordedCommands).isEqualTo(
      listOf(NavigationCommand.Replace(Screen.Details(id = "xyz"))),
    )
  }

  @Test
  fun `unknown uri is silently dropped`() = runTest {
    val h = newBridge(scope = this@runTest)

    h.bridge.handle("nutrisport://garbage/path", DeeplinkSource.CustomScheme)
    advanceUntilIdle()

    assertThat(h.router.recordedCommands).isEmpty()
    assertThat(h.pending.peek()).isNull()
  }

  // ─── helpers ────────────────────────────────────────────────────────────

  private data class Harness(
    val bridge: DeeplinkBridge,
    val router: FakeRouter,
    val pending: InMemoryPendingDeeplinkStorage,
  )

  private fun newBridge(
    scope: CoroutineScope,
    signedIn: Boolean = false,
    admin: Boolean = false,
  ): Harness {
    val pending = InMemoryPendingDeeplinkStorage()
    val router = FakeRouter()
    val bridge = DeeplinkBridge(
      resolver = DefaultDeeplinkResolver(routes = DeeplinkRegistry.routes),
      pending = pending,
      router = router,
      authGate = FakeDeeplinkAuthGate(signedIn = signedIn, admin = admin),
      scope = scope,
    )
    return Harness(bridge = bridge, router = router, pending = pending)
  }
}
