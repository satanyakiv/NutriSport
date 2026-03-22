package com.nutrisport.analytics.core

interface AnalyticsProcessor {
  val key: String
  fun logEvent(event: AnalyticsEvent)
  fun setEnabled(enabled: Boolean): AnalyticsProcessor
  fun setLoggingEnabled(enabled: Boolean): AnalyticsProcessor
}
