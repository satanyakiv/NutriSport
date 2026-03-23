package com.nutrisport.shared.domain.coroutine

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Abstraction for coroutine dispatchers. Inject instead of hardcoding `Dispatchers.*`.
 *
 * Production: [DefaultCoroutineDispatcherProvider] (singleton, delegates to `Dispatchers.*`).
 * Tests: `TestCoroutineDispatcherProvider(testDispatcher)` from `:shared:testing`.
 *
 * Pattern from doterraMobileApp.
 */
interface CoroutineDispatcherProvider {
  fun main(): CoroutineDispatcher
  fun default(): CoroutineDispatcher
  fun io(): CoroutineDispatcher
  fun unconfined(): CoroutineDispatcher
}
