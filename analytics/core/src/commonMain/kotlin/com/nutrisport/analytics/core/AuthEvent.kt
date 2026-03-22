package com.nutrisport.analytics.core

sealed interface AuthEvent : AnalyticsEvent {

  data class SignIn(val method: String = "google") : AuthEvent {
    override val name: String = "sign_in"
  }

  data object SignOut : AuthEvent {
    override val name: String = "sign_out"
  }
}
