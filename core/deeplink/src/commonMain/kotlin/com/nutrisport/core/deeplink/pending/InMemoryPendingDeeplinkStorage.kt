package com.nutrisport.core.deeplink.pending

import com.nutrisport.shared.domain.deeplink.PendingDeeplinkStorage
import com.nutrisport.shared.navigation.Screen
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Process-local [PendingDeeplinkStorage]. Backed by a single [AtomicReference]
 * cell so a write from the bridge and a read from the auth flow never race.
 * Lifetime is the launch only — process death drops the value.
 */
@OptIn(ExperimentalAtomicApi::class)
class InMemoryPendingDeeplinkStorage : PendingDeeplinkStorage {
  private val ref = AtomicReference<Screen?>(null)

  override fun peek(): Screen? = ref.load()

  override fun take(): Screen? = ref.exchange(null)

  override fun set(screen: Screen) {
    ref.store(screen)
  }

  override fun clear() {
    ref.store(null)
  }
}
