package com.nutrisport.data.reporter

import com.himanshoe.tracey.Tracey
import com.himanshoe.tracey.model.ReplayPayload
import com.himanshoe.tracey.reporter.TraceyReporter

/**
 * Custom Tracey reporter optimized for Claude Code debug sessions.
 *
 * On crash or manual capture:
 * 1. Persists full replay payload as JSON to device storage
 * 2. Generates a runnable Kotlin UI test from the session
 *
 * Pull dumps: `adb pull /data/data/com.portfolio.nutrisport.debug/files/tracey/ ./tracey-dumps/`
 * Then analyze: `/debug-crash tracey-dumps/replay_xxx.json`
 */
class ClaudeReporter : TraceyReporter {

  override suspend fun onReplayReady(payload: ReplayPayload) {
    val jsonPath = Tracey.captureAndExportFile()
    val testCode = Tracey.captureAndExportTest()

    if (jsonPath != null) {
      println("[$TAG] JSON dump: $jsonPath")
      println("[$TAG] Pull: adb pull $jsonPath ./tracey-dumps/")
    }
    if (testCode.isNotBlank()) {
      println("[$TAG] Generated regression test available")
    }
  }

  companion object {
    private const val TAG = "Tracey-Claude"
  }
}
