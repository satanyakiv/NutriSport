package com.portfolio.nutrisport.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmarks {

  @get:Rule
  val rule = MacrobenchmarkRule()

  @Test
  fun startupNoCompilation() = startup(CompilationMode.None())

  @Test
  fun startupPartialCompilation() = startup(CompilationMode.Partial())

  private fun startup(compilationMode: CompilationMode) = rule.measureRepeated(
    packageName = "com.portfolio.nutrisport",
    metrics = listOf(StartupTimingMetric()),
    compilationMode = compilationMode,
    iterations = 5,
    startupMode = StartupMode.COLD,
    setupBlock = {
      pressHome()
    },
  ) {
    startActivityAndWait()
    device.waitForIdle()
  }
}
