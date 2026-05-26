package com.nutrisport.data

import com.nutrisport.shared.domain.deeplink.DeeplinkAuthGate

/**
 * Firebase-backed [DeeplinkAuthGate]. Signed-in is the presence of a current
 * Firebase user; admin reuses the Firestore role read shared with `withAdminAuth`.
 */
class FirebaseDeeplinkAuthGate : DeeplinkAuthGate {
  override fun isSignedIn(): Boolean = currentUserId() != null

  override suspend fun isAdmin(): Boolean = isCurrentUserAdmin()
}
