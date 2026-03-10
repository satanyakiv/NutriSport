package com.nutrisport.shared.util

expect object AppConfig {
  val isDebug: Boolean
  val enableLogging: Boolean
}
