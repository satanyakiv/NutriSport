package com.nutrisport.shared.util

import android.os.Build

actual object AppConfig {
  actual val isDebug: Boolean = Build.TYPE == "userdebug" ||
    try {
      val clazz = Class.forName("com.portfolio.nutrisport.BuildConfig")
      clazz.getField("DEBUG").getBoolean(null)
    } catch (_: Exception) {
      false
    }
  actual val enableLogging: Boolean = isDebug
}
