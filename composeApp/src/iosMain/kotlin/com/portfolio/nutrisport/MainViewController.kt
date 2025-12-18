package com.portfolio.nutrisport

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() }
) { AppContent() }