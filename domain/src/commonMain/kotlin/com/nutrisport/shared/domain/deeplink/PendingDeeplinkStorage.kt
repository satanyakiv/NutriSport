package com.nutrisport.shared.domain.deeplink

import com.nutrisport.shared.navigation.Screen

/**
 * Holds a single deeplink target that arrived while the user was signed out.
 * [take] returns the stored value and clears the cell atomically; [peek] never
 * clears. Lifetime is the current app launch only — process death drops the
 * value, so a stale link never auto-replays across cold starts.
 */
interface PendingDeeplinkStorage {
  fun peek(): Screen?
  fun take(): Screen?
  fun set(screen: Screen)
  fun clear()
}
