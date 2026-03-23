package com.nutrisport.analytics.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NutriSportAnalyticsImpl : NutriSportAnalytics {

  private val processors = mutableMapOf<String, AnalyticsProcessor>()

  override fun addProcessor(processor: AnalyticsProcessor) {
    processors[processor.key] = processor
  }

  override suspend fun logEvent(event: AnalyticsEvent) {
    withContext(Dispatchers.Default) {
      processors.values.forEach { it.logEvent(event) }
    }
  }
}
