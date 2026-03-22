package com.nutrisport.analytics.core

import io.github.aakira.napier.Napier

class DebugAnalyticsProcessor : AnalyticsProcessor {

  override val key: String = "debug"

  private var enabled = true
  private var loggingEnabled = true

  override fun logEvent(event: AnalyticsEvent) {
    if (!enabled || !loggingEnabled) return
    Napier.d(tag = "Analytics") { "[${event.name}]" }
  }

  override fun setEnabled(enabled: Boolean) = apply {
    this.enabled = enabled
  }

  override fun setLoggingEnabled(enabled: Boolean) = apply {
    this.loggingEnabled = enabled
  }
}
