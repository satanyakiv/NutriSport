package com.nutrisport.analytics

import io.github.aakira.napier.Napier

class NapierTracker : AnalyticsTracker {
    override fun trackEvent(event: AnalyticsEvent) {
        Napier.d(tag = "Analytics") {
            "[${event.name}] ${event.params}"
        }
    }
}
