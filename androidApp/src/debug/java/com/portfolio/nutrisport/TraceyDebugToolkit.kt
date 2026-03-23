package com.portfolio.nutrisport

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.himanshoe.tracey.Tracey
import com.himanshoe.tracey.TraceyConfig
import com.himanshoe.tracey.TraceyHost
import com.himanshoe.tracey.navigation.rememberTraceyNavController
import com.himanshoe.tracey.reporter.LogcatReporter
import com.nutrisport.navigation.debug.DebugToolkit

/** Debug instrumentation backed by Tracey session recorder. */
class TraceyDebugToolkit : DebugToolkit {

  override fun initialize() {
    Tracey.install(
      TraceyConfig(
        enabled = true,
        showOverlay = true,
        bufferDurationSeconds = 30,
        maxEvents = 500,
        trackLifecycle = true,
        generateHtmlReport = true,
        reporters = listOf(LogcatReporter(), ClaudeReporter()),
        redactedTags = listOf("credit_card", "password"),
      ),
    )
  }

  @Composable
  override fun rememberNavController(): NavHostController = rememberTraceyNavController()

  @Composable
  override fun WrapRootContent(content: @Composable () -> Unit) {
    TraceyHost { content() }
  }

  override fun log(message: String) {
    Tracey.log(message)
  }
}
