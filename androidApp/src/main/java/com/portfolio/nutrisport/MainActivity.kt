package com.portfolio.nutrisport

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import com.mmk.kmpnotifier.permission.permissionUtil
import com.nutrisport.app.AppContent

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(
        Color.TRANSPARENT, Color.TRANSPARENT
      ),
      navigationBarStyle = SystemBarStyle.light(
        Color.TRANSPARENT, Color.TRANSPARENT
      )
    )
    installSplashScreen()
    super.onCreate(savedInstanceState)
    if (!BuildConfig.ENABLE_LOGGING) {
      window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }
    initNotifier()
    requestNotificationPermission()

    setContent {
      AppContent()
    }
  }

  private fun requestNotificationPermission() {
    val permissionUtil by permissionUtil()
    permissionUtil.askNotificationPermission()
  }

  private fun initNotifier() {
    NotifierManager.initialize(
      configuration = NotificationPlatformConfiguration.Android(
        notificationIconResId = R.drawable.ic_launcher_foreground,
        showPushNotification = true,
      )
    )
  }
}
