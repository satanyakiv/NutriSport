package com.nutrisport.analytics.core

sealed interface NavigationEvent : AnalyticsEvent {

  data class ScreenView(val screenName: String) : NavigationEvent {
    override val name: String = "screen_view"
  }
}
