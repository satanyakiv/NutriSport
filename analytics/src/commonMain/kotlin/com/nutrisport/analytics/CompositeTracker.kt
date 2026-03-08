package com.nutrisport.analytics

class CompositeTracker(
    private val trackers: List<AnalyticsTracker>,
) : AnalyticsTracker {
    override fun trackEvent(event: AnalyticsEvent) {
        trackers.forEach { it.trackEvent(event) }
    }
}
