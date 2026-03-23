package com.portfolio.nutrisport

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.himanshoe.tracey.Tracey
import com.himanshoe.tracey.TraceyConfig
import com.himanshoe.tracey.reporter.LogcatReporter
import com.nutrisport.analytics.core.DebugAnalyticsProcessor
import com.nutrisport.analytics.core.NutriSportAnalytics
import com.nutrisport.analytics.firebase.FirebaseAnalyticsProcessor
import com.nutrisport.data.reporter.ClaudeReporter
import com.nutrisport.di.initializeKoin
import com.nutrisport.shared.Constants
import com.nutrisport.shared.util.AppConfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.java.KoinJavaComponent.getKoin

class NutrisportApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    Constants.initGoogleClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
    if (AppConfig.enableLogging) {
      Napier.base(DebugAntilog())
    }
    initializeKoin(
      useFakeData = BuildConfig.USE_FAKE_DATA,
      config = { androidContext(this@NutrisportApplication) },
    )
    if (!BuildConfig.USE_FAKE_DATA) {
      Firebase.initialize(this)
    }
    initTracey()
    initAnalytics()
  }

  private fun initTracey() {
    if (AppConfig.isDebug) {
      Tracey.install(
        TraceyConfig(
          enabled = true,
          showOverlay = true,
          bufferDurationSeconds = 30,
          maxEvents = 500,
          trackLifecycle = true,
          generateHtmlReport = true,
          reporters = listOf(LogcatReporter(), ClaudeReporter()),
          redactedTags = listOf("credit_card", "password"),
        ),
      )
    }
  }

  private fun initAnalytics() {
    val analytics = getKoin().get<NutriSportAnalytics>()
    analytics.addProcessor(
      DebugAnalyticsProcessor()
        .setEnabled(AppConfig.isDebug)
        .setLoggingEnabled(AppConfig.isDebug),
    )
    if (!BuildConfig.USE_FAKE_DATA) {
      val firebaseProcessor = getKoin().get<FirebaseAnalyticsProcessor>()
      analytics.addProcessor(
        firebaseProcessor
          .setEnabled(!AppConfig.isDebug)
          .setLoggingEnabled(AppConfig.isDebug),
      )
    }
  }
}
