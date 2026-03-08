package com.portfolio.nutrisport

import androidx.compose.ui.window.ComposeUIViewController
import com.nutrisport.data.AppContent
import com.nutrisport.di.initializeKoin
import com.nutrisport.shared.util.AppConfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun MainViewController() = ComposeUIViewController(
  configure = {
    if (AppConfig.enableLogging) {
      Napier.base(DebugAntilog())
    }
    initializeKoin()
  }
) { AppContent() }