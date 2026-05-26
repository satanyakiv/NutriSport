package com.nutrisport.core.deeplink.resolver

import com.nutrisport.core.deeplink.DeeplinkSource
import com.nutrisport.core.deeplink.ResolvedDeeplink

/**
 * Translates platform-side input (a URI string or a push `data` payload) into a
 * typed [com.nutrisport.shared.navigation.Screen]. Returns null for unknown or
 * malformed input; callers ignore null and log nothing user-visible.
 */
interface DeeplinkResolver {
  fun resolveUri(uri: String, source: DeeplinkSource): ResolvedDeeplink?
  fun resolvePush(data: Map<String, String>): ResolvedDeeplink?
}
