package com.nutrisport.analytics.core

class FakeAnalyticsProcessor(
    override val key: String = "fake",
) : AnalyticsProcessor {

    val events = mutableListOf<AnalyticsEvent>()

    override fun logEvent(event: AnalyticsEvent) {
        events.add(event)
    }

    override fun setEnabled(enabled: Boolean) = this
    override fun setLoggingEnabled(enabled: Boolean) = this
}
