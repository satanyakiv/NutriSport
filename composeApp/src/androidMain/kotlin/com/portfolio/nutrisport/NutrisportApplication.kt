package com.portfolio.nutrisport

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize
import org.koin.android.ext.koin.androidContext

class NutrisportApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin(
            config = { androidContext(this@NutrisportApplication) }
        )
        Firebase.initialize(this)
    }
}
