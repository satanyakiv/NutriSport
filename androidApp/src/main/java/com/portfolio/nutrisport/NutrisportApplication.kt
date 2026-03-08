package com.portfolio.nutrisport

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.nutrisport.di.initializeKoin
import com.nutrisport.shared.util.AppConfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext

class NutrisportApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (AppConfig.enableLogging) {
            Napier.base(DebugAntilog())
        }
        initializeKoin(
            config = { androidContext(this@NutrisportApplication) }
        )
        Firebase.initialize(this)
    }
}
