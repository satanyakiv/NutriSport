package com.nutrisport.analytics.core

import com.nutrisport.shared.domain.coroutine.CoroutineDispatcherProvider
import com.nutrisport.shared.domain.coroutine.DefaultCoroutineDispatcherProvider
import kotlinx.coroutines.withContext

class NutriSportAnalyticsImpl(
  private val dispatcherProvider: CoroutineDispatcherProvider = DefaultCoroutineDispatcherProvider,
) : NutriSportAnalytics {

  private val processors = mutableMapOf<String, AnalyticsProcessor>()

  override fun addProcessor(processor: AnalyticsProcessor) {
    processors[processor.key] = processor
  }

  override suspend fun logEvent(event: AnalyticsEvent) {
    withContext(dispatcherProvider.default()) {
      processors.values.forEach { it.logEvent(event) }
    }
  }
}
