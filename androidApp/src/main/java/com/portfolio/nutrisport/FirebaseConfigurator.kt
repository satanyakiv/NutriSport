package com.portfolio.nutrisport

import android.app.Application

interface FirebaseConfigurator {
  fun initialize(application: Application)
}

class NoOpFirebaseConfigurator : FirebaseConfigurator {
  override fun initialize(application: Application) = Unit
}
