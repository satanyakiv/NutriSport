package com.portfolio.nutrisport

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.nutrisport.di.initializeKoin
import com.nutrisport.shared.Constants
import com.nutrisport.shared.util.AppConfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext

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
  }
}
