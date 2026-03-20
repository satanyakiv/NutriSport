package com.portfolio.nutrisport.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

  @get:Rule
  val rule = BaselineProfileRule()

  @Test
  fun generate() = rule.collect(
    packageName = "com.portfolio.nutrisport",
  ) {
    // Cold start
    pressHome()
    startActivityAndWait()

    // Wait for content to load
    device.waitForIdle()

    // Scroll products list
    val scrollable = device.findObject(By.scrollable(true))
    scrollable?.let {
      it.scroll(Direction.DOWN, 2f)
      device.waitForIdle()
      it.scroll(Direction.UP, 2f)
      device.waitForIdle()
    }

    // Tap first clickable product card
    val productCard = device.findObject(By.clickable(true).depth(4))
    productCard?.let {
      it.click()
      device.wait(Until.hasObject(By.scrollable(true)), 3000)
      device.waitForIdle()
      device.pressBack()
      device.waitForIdle()
    }

    // Navigate bottom tabs
    val bottomNav = device.findObject(By.res("bottom_navigation"))
    if (bottomNav != null) {
      val tabs = bottomNav.children
      tabs?.forEachIndexed { index, tab ->
        if (index > 0) {
          tab.click()
          device.waitForIdle()
        }
      }
    }
  }
}
