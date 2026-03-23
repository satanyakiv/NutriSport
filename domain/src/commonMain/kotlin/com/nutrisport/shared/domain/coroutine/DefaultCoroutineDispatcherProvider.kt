package com.nutrisport.shared.domain.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

object DefaultCoroutineDispatcherProvider : CoroutineDispatcherProvider {
  override fun main() = Dispatchers.Main
  override fun default() = Dispatchers.Default
  override fun io() = Dispatchers.IO
  override fun unconfined() = Dispatchers.Unconfined
}
