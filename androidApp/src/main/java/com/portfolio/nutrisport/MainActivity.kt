package com.portfolio.nutrisport

import android.content.Intent
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
import com.nutrisport.core.deeplink.ColdStartDeeplinkQueue
import com.nutrisport.core.deeplink.DeeplinkBridge
import com.nutrisport.core.deeplink.DeeplinkSource
import org.koin.android.ext.android.get

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

    // Cold start: buffer the launch deeplink until the NavGraph collector attaches.
    if (savedInstanceState == null) {
      intent.data?.toString()?.let {
        ColdStartDeeplinkQueue.handle(it, DeeplinkSource.CustomScheme)
      }
    }

    setContent {
      AppContent()
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    // Warm start: NavGraph is alive, dispatch straight through the bridge.
    intent.data?.toString()?.let {
      get<DeeplinkBridge>().handle(it, DeeplinkSource.CustomScheme)
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
