package com.portfolio.nutrisport

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.initialize

class ReleaseFirebaseConfigurator : FirebaseConfigurator {
  override fun initialize(application: Application) {
    Firebase.initialize(application)
    FirebaseCrashlytics.getInstance()
      .setCrashlyticsCollectionEnabled(true)
  }
}
