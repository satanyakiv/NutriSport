package com.nutrisport.shared.test

import com.nutrisport.shared.domain.deeplink.PendingDeeplinkStorage
import com.nutrisport.shared.navigation.Screen

/**
 * In-memory [PendingDeeplinkStorage] for tests. Not thread-safe — single-threaded
 * test use only. Seed with [stored] or via [set].
 */
class FakePendingDeeplinkStorage(private var stored: Screen? = null) : PendingDeeplinkStorage {
  override fun peek(): Screen? = stored

  override fun take(): Screen? = stored.also { stored = null }

  override fun set(screen: Screen) {
    stored = screen
  }

  override fun clear() {
    stored = null
  }
}
