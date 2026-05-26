package com.nutrisport.shared.test

import com.nutrisport.shared.domain.deeplink.DeeplinkAuthGate

/**
 * In-memory [DeeplinkAuthGate] for tests and fake-data builds. Toggle [signedIn]
 * and [admin] to drive the bridge's gating branches without Firebase.
 */
class FakeDeeplinkAuthGate(
  var signedIn: Boolean = false,
  var admin: Boolean = false,
) : DeeplinkAuthGate {
  override fun isSignedIn(): Boolean = signedIn

  override suspend fun isAdmin(): Boolean = admin
}
