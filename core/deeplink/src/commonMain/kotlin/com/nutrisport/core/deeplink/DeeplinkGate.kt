package com.nutrisport.core.deeplink

/**
 * Access tier a resolved deeplink requires.
 *  - [Public] opens unconditionally.
 *  - [SignedIn] requires an authenticated user; otherwise the target is parked
 *    and the user is redirected to auth for post-login replay.
 *  - [Admin] requires admin privileges; otherwise the link is silently dropped
 *    so the admin surface is never revealed to a non-admin.
 */
enum class DeeplinkGate { Public, SignedIn, Admin }
