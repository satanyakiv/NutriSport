package com.nutrisport.core.deeplink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.nutrisport.shared.domain.navigation.Router

/**
 * Shared cold-start drain effect for both platforms. Awaits [Router.awaitReady]
 * (which resolves the moment `SetupNavGraph` attaches its `Router.commands`
 * collector) and then flips the [ColdStartDeeplinkQueue] latch and replays any
 * deeplink buffered during the cold-start gap. Place it once near the navigation
 * root (see `composeApp` `AppContent`).
 */
@Composable
fun ColdStartDeeplinkDrainEffect(router: Router) {
  LaunchedEffect(router) {
    router.awaitReady()
    ColdStartDeeplinkQueue.markReadyAndDrain()
  }
}
