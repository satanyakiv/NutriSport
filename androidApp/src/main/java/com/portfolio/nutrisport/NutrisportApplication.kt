package com.portfolio.nutrisport

import android.app.Application
import com.nutrisport.analytics.core.DebugAnalyticsProcessor
import com.nutrisport.analytics.core.NutriSportAnalytics
import com.nutrisport.analytics.firebase.FirebaseAnalyticsProcessor
import com.nutrisport.di.initializeKoin
import com.nutrisport.navigation.debug.DebugToolkit
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
      additionalModules = DebugModuleProvider.modules,
      config = { androidContext(this@NutrisportApplication) },
    )
    getKoin().get<FirebaseConfigurator>().initialize(this)
    getKoin().get<DebugToolkit>().initialize()
    initAnalytics()
  }

  private fun initAnalytics() {
    val analytics = getKoin().get<NutriSportAnalytics>()
    analytics.addProcessor(
      DebugAnalyticsProcessor()
        .setEnabled(AppConfig.isDebug)
        .setLoggingEnabled(AppConfig.isDebug),
    )
    getKoin().getOrNull<FirebaseAnalyticsProcessor>()?.let { firebaseProcessor ->
      analytics.addProcessor(
        firebaseProcessor
          .setEnabled(!AppConfig.isDebug)
          .setLoggingEnabled(AppConfig.isDebug),
      )
    }
  }
}
