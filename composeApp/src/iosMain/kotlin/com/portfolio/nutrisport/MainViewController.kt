package com.portfolio.nutrisport

import androidx.compose.ui.window.ComposeUIViewController
import com.nutrisport.analytics.core.DebugAnalyticsProcessor
import com.nutrisport.analytics.core.NutriSportAnalytics
import com.nutrisport.analytics.firebase.FirebaseAnalyticsProcessor
import com.nutrisport.data.AppContent
import com.nutrisport.di.initializeKoin
import com.nutrisport.shared.util.AppConfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.mp.KoinPlatformTools

@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController(
  configure = {
    if (AppConfig.enableLogging) {
      Napier.base(DebugAntilog())
    }
    initializeKoin()
    initAnalytics()
  },
) { AppContent() }

private fun initAnalytics() {
  val koin = KoinPlatformTools.defaultContext().get()
  val analytics = koin.get<NutriSportAnalytics>()
  analytics.addProcessor(
    DebugAnalyticsProcessor()
      .setEnabled(AppConfig.isDebug)
      .setLoggingEnabled(AppConfig.isDebug),
  )
  val firebaseProcessor = koin.get<FirebaseAnalyticsProcessor>()
  analytics.addProcessor(
    firebaseProcessor
      .setEnabled(!AppConfig.isDebug)
      .setLoggingEnabled(AppConfig.isDebug),
  )
}
