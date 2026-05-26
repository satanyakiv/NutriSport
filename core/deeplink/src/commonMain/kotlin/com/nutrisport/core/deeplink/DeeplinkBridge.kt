package com.nutrisport.core.deeplink

import com.nutrisport.core.deeplink.resolver.DeeplinkResolver
import com.nutrisport.shared.domain.deeplink.DeeplinkAuthGate
import com.nutrisport.shared.domain.deeplink.PendingDeeplinkStorage
import com.nutrisport.shared.domain.navigation.Router
import com.nutrisport.shared.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Single entry point platform code calls when a deeplink arrives (Android intent
 * data / iOS [DeeplinkSwiftBridge] / push tap). Resolves the URI to a [Screen],
 * applies the [DeeplinkGate], and either dispatches the navigation immediately or
 * parks it in [PendingDeeplinkStorage] for post-login replay.
 *
 * Lives on an application-scoped [CoroutineScope] so the suspend admin check
 * survives Activity recreation on Android and SwiftUI scene reattachment on iOS.
 */
class DeeplinkBridge(
  private val resolver: DeeplinkResolver,
  private val pending: PendingDeeplinkStorage,
  private val router: Router,
  private val authGate: DeeplinkAuthGate,
  private val scope: CoroutineScope,
) {

  /**
   * The single dispatch entry. Resolves [uri] and routes it through [dispatch].
   * Call sites: warm taps (`MainActivity.onNewIntent`, iOS scene re-activation)
   * AND the cold-start drain — once [ColdStartDeeplinkQueue.markReadyAndDrain]
   * sees the NavGraph collector attach it replays queued cold links through here.
   */
  fun handle(uri: String, source: DeeplinkSource) {
    val resolved = resolver.resolveUri(uri, source) ?: return
    dispatch(resolved)
  }

  fun handlePush(data: Map<String, String>) {
    val resolved = resolver.resolvePush(data) ?: return
    dispatch(resolved)
  }

  private fun dispatch(resolved: ResolvedDeeplink) {
    when (resolved.gate) {
      DeeplinkGate.Public -> router.replaceWith(resolved.screen)
      DeeplinkGate.SignedIn ->
        if (authGate.isSignedIn()) {
          router.replaceWith(resolved.screen)
        } else {
          // Park for post-login replay, send to the auth screen.
          pending.set(resolved.screen)
          router.replaceWith(Screen.Auth)
        }
      DeeplinkGate.Admin -> scope.launch {
        // Drop silently for non-admins — never reveal the admin surface.
        if (authGate.isAdmin()) router.replaceWith(resolved.screen)
      }
    }
  }
}
