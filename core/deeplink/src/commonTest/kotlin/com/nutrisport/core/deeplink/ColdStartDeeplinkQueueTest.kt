package com.nutrisport.core.deeplink

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.nutrisport.core.deeplink.pending.InMemoryPendingDeeplinkStorage
import com.nutrisport.core.deeplink.registry.DeeplinkRegistry
import com.nutrisport.core.deeplink.resolver.DefaultDeeplinkResolver
import com.nutrisport.shared.domain.navigation.NavigationCommand
import com.nutrisport.shared.navigation.Screen
import com.nutrisport.shared.test.FakeDeeplinkAuthGate
import com.nutrisport.shared.test.FakeRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ColdStartDeeplinkQueueTest {

  @AfterTest
  fun tearDown() {
    ColdStartDeeplinkQueue.resetForTest()
  }

  @Test
  fun `link enqueued before ready is not dispatched`() = runTest {
    val (bridge, router) = newBridge(scope = this@runTest)
    ColdStartDeeplinkQueue.bridgeProvider = { bridge }

    ColdStartDeeplinkQueue.handle("nutrisport://products/abc123", DeeplinkSource.CustomScheme)
    advanceUntilIdle()

    assertThat(router.recordedCommands).isEmpty()
  }

  @Test
  fun `markReadyAndDrain dispatches queued link`() = runTest {
    val (bridge, router) = newBridge(scope = this@runTest)
    ColdStartDeeplinkQueue.bridgeProvider = { bridge }

    ColdStartDeeplinkQueue.handle("nutrisport://products/abc123", DeeplinkSource.CustomScheme)
    ColdStartDeeplinkQueue.markReadyAndDrain()
    advanceUntilIdle()

    assertThat(router.recordedCommands).isEqualTo(
      listOf(NavigationCommand.Replace(Screen.Details(id = "abc123"))),
    )
  }

  @Test
  fun `warm link dispatches immediately once ready`() = runTest {
    val (bridge, router) = newBridge(scope = this@runTest)
    ColdStartDeeplinkQueue.bridgeProvider = { bridge }

    ColdStartDeeplinkQueue.markReadyAndDrain()
    ColdStartDeeplinkQueue.handle("nutrisport://products/abc123", DeeplinkSource.CustomScheme)
    advanceUntilIdle()

    assertThat(router.recordedCommands).isEqualTo(
      listOf(NavigationCommand.Replace(Screen.Details(id = "abc123"))),
    )
  }

  @Test
  fun `queued push payload drains through handlePush`() = runTest {
    val (bridge, router) = newBridge(scope = this@runTest)
    ColdStartDeeplinkQueue.bridgeProvider = { bridge }

    ColdStartDeeplinkQueue.handlePush(mapOf("deeplink" to "nutrisport://products/xyz"))
    ColdStartDeeplinkQueue.markReadyAndDrain()
    advanceUntilIdle()

    assertThat(router.recordedCommands).isEqualTo(
      listOf(NavigationCommand.Replace(Screen.Details(id = "xyz"))),
    )
  }

  // ─── helpers ────────────────────────────────────────────────────────────

  private fun newBridge(scope: CoroutineScope): Pair<DeeplinkBridge, FakeRouter> {
    val router = FakeRouter()
    val bridge = DeeplinkBridge(
      resolver = DefaultDeeplinkResolver(routes = DeeplinkRegistry.routes),
      pending = InMemoryPendingDeeplinkStorage(),
      router = router,
      authGate = FakeDeeplinkAuthGate(signedIn = true),
      scope = scope,
    )
    return bridge to router
  }
}
