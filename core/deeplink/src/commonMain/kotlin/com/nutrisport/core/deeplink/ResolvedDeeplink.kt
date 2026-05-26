package com.nutrisport.core.deeplink

import com.nutrisport.shared.navigation.Screen

/**
 * Output of [com.nutrisport.core.deeplink.resolver.DeeplinkResolver]. [gate] is
 * read by [DeeplinkBridge] to decide which auth signals to consult before
 * navigating to [screen].
 */
data class ResolvedDeeplink(val screen: Screen, val gate: DeeplinkGate)
