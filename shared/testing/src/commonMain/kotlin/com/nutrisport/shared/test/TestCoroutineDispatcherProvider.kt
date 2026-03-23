package com.nutrisport.shared.test

import com.nutrisport.shared.domain.coroutine.CoroutineDispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher

/** All dispatchers route to [testDispatcher] for deterministic test execution. */
class TestCoroutineDispatcherProvider(
  private val testDispatcher: TestDispatcher,
) : CoroutineDispatcherProvider {
  override fun main(): CoroutineDispatcher = testDispatcher
  override fun default(): CoroutineDispatcher = testDispatcher
  override fun io(): CoroutineDispatcher = testDispatcher
  override fun unconfined(): CoroutineDispatcher = testDispatcher
}
