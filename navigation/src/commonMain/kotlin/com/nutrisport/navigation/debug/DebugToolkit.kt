package com.nutrisport.navigation.debug

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/**
 * Abstraction for debug-only instrumentation (session recording, layout inspection, etc.).
 *
 * Implementations are provided via Koin DI per build type:
 * - debug: [TraceyDebugToolkit] in `androidApp/src/debug/`
 * - release: [NoOpDebugToolkit] (default)
 *
 * To add a new debug tool, create a new implementation or use [CompositeDebugToolkit]
 * pattern — only `androidApp/src/debug/` changes, zero modifications in common code.
 */
interface DebugToolkit {
  /** One-time SDK initialization. Called from Application.onCreate(). */
  fun initialize()

  /** Create NavController with optional debug enhancements (e.g. screen tracking). */
  @Composable
  fun rememberNavController(): NavHostController

  /** Wrap root composable with debug overlay (e.g. gesture recording layer). */
  @Composable
  fun WrapRootContent(content: @Composable () -> Unit)

  /** Log a debug breadcrumb. Feature modules call this without knowing the debug backend. */
  fun log(message: String)
}
