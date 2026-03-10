package com.nutrisport.analytics

interface AnalyticsTracker {
  fun trackEvent(event: AnalyticsEvent)
}
