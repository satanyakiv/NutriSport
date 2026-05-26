package com.nutrisport.core.deeplink

/**
 * Swift-facing entry point. iOS funnels every deeplink surface through this object
 * so Swift sees a single contract: custom-scheme open URLs arrive via the SwiftUI
 * `.onOpenURL` scene modifier in `iOSApp.swift`; push-notification taps (when
 * wired) arrive via the notification delegate.
 *
 * This is a thin adapter: it converts Swift-shaped inputs (a source string, a
 * loosely-typed user-info dictionary) into the Kotlin domain shapes and delegates
 * to the platform-agnostic [ColdStartDeeplinkQueue], which owns the cold-start
 * buffering + drain and routes through [DeeplinkBridge.dispatch]. Android feeds
 * the same queue from `MainActivity`.
 */
object DeeplinkSwiftBridge {

  fun handleUri(uri: String, source: String) {
    ColdStartDeeplinkQueue.handle(uri, parseSource(source))
  }

  /**
   * APNs / UNNotificationResponse `userInfo` dictionary. Swift sends it as
   * `[AnyHashable: Any]` because user-info dictionaries are loosely typed; we
   * normalize to `Map<String, String>` here so the queue / [DeeplinkBridge] stay
   * platform-agnostic.
   */
  fun handlePush(userInfo: Map<Any?, *>) {
    val normalized = buildMap<String, String> {
      for ((rawKey, rawValue) in userInfo) {
        val key = rawKey as? String ?: continue
        val value = rawValue?.toString() ?: continue
        put(key, value)
      }
    }
    ColdStartDeeplinkQueue.handlePush(normalized)
  }

  private fun parseSource(source: String): DeeplinkSource = when (source) {
    "UniversalLink" -> DeeplinkSource.UniversalLink
    else -> DeeplinkSource.CustomScheme
  }
}
