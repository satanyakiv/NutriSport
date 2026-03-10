package com.nutrisport.shared.util

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual object AppConfig {
  actual val isDebug: Boolean = Platform.isDebugBinary
  actual val enableLogging: Boolean = isDebug
}
