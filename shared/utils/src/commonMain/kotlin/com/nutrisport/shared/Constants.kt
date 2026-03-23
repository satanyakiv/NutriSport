package com.nutrisport.shared

object Constants {
  /**
   * Injected at build time via BuildConfig / local.properties.
   * Call [initGoogleClientId] from Application.onCreate().
   */
  var GOOGLE_WEB_CLIENT_ID: String = ""
    private set

  fun initGoogleClientId(clientId: String) {
    GOOGLE_WEB_CLIENT_ID = clientId
  }

  const val MAX_QUANTITY = 10
  const val MIN_QUANTITY = 1
}
