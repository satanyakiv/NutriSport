package com.nutrisport.analytics.core

interface NutriSportAnalytics {
  suspend fun logEvent(event: AnalyticsEvent)
  fun addProcessor(processor: AnalyticsProcessor)
}
