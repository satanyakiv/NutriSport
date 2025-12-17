package com.portfolio.nutrisport

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize

class NutrisportApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
    }
}
