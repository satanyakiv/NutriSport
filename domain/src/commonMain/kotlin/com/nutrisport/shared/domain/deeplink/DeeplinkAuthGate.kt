package com.nutrisport.shared.domain.deeplink

/**
 * Auth signals the deeplink layer consults before opening a gated target.
 * Implemented in :network against Firebase (signed-in = a current user exists;
 * admin = the Firestore role flag). Declared here in :domain so :core:deeplink
 * stays free of platform/Firebase dependencies.
 */
interface DeeplinkAuthGate {
  fun isSignedIn(): Boolean
  suspend fun isAdmin(): Boolean
}
