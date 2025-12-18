package com.portfolio.nutrisport

import androidx.compose.ui.window.ComposeUIViewController
import com.nutrisport.data.AppContent
import com.nutrisport.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
  configure = { initializeKoin() }
) { AppContent() }