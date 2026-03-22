package com.nutrisport.analytics.firebase

import com.nutrisport.analytics.core.AnalyticsEvent
import com.nutrisport.analytics.core.AnalyticsEventMapper
import com.nutrisport.analytics.core.AnalyticsProcessor
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import io.github.aakira.napier.Napier

class FirebaseAnalyticsProcessor(
  private val firebaseAnalytics: FirebaseAnalytics,
  private val mappers: List<AnalyticsEventMapper>,
) : AnalyticsProcessor {

  override val key: String = "firebase"

  private var enabled = true
  private var loggingEnabled = false

  override fun logEvent(event: AnalyticsEvent) {
    if (!enabled) return
    val mapped = mappers.firstNotNullOfOrNull { it.map(event) } ?: return
    if (loggingEnabled) {
      Napier.d(tag = "FirebaseAnalytics") {
        "[${mapped.eventName}] ${mapped.params}"
      }
    }
    firebaseAnalytics.logEvent(mapped.eventName, mapped.params)
  }

  override fun setEnabled(enabled: Boolean) = apply {
    this.enabled = enabled
  }

  override fun setLoggingEnabled(enabled: Boolean) = apply {
    this.loggingEnabled = enabled
  }
}
