package com.nutrisport.observability

import com.nutrisport.shared.util.AppConfig
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryEvent
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb

// Public DSN — safe to ship in the client; it only identifies where events go.
private const val SENTRY_DSN =
  "https://e08359786c1a2b1977aaf58f663f1041@o4511450793050112.ingest.de.sentry.io/4511451066007632"

/**
 * Initializes Sentry crash + non-fatal reporting. Call as early as possible on each
 * platform, before Koin: Android `Application.onCreate`, iOS `AppDelegate.didFinishLaunching`.
 *
 * Runs alongside Firebase Crashlytics (dual observability). Environment and the SDK's own
 * diagnostic logging are read from [AppConfig], the single build-config seam, so no
 * build-type branching leaks into the call sites.
 */
fun startSentry() {
  Sentry.init { options ->
    options.dsn = SENTRY_DSN
    options.environment = AppConfig.sentryEnvironment
    options.debug = AppConfig.enableLogging
    // Keep PII out of Sentry: no auto IP/headers, plus an explicit scrub of tokens/emails
    // from event messages and breadcrumbs.
    options.sendDefaultPii = false
    options.tracesSampleRate = 0.0
    options.beforeSend = ::scrubEvent
    options.beforeBreadcrumb = ::scrubBreadcrumb
  }
}

// Bearer covers base64 token chars + padding so the whole credential is removed.
private val BEARER = Regex("""(?i)bearer\s+[A-Za-z0-9._/+\-]+=*""")
private val JWT = Regex("""eyJ[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+""")
private val EMAIL = Regex("""[A-Za-z0-9.+_\-]+@[A-Za-z0-9_\-]+\.[A-Za-z0-9.\-]+""")

// Opaque tokens (refresh / reset / FCM) have no recognisable shape, so they are
// caught by key name in structured data rather than by matching their value.
private val SENSITIVE_KEY =
  Regex("""(?i)(authorization|cookie|password|passwd|secret|api[_-]?key|token|credential|otp)""")

internal fun redactText(text: String?): String? =
  text
    ?.replace(BEARER, "Bearer [redacted]")
    ?.replace(JWT, "[redacted-token]")
    ?.replace(EMAIL, "[redacted-email]")

internal fun redactValue(key: String?, value: Any?): Any? = when {
  key != null && SENSITIVE_KEY.containsMatchIn(key) -> "[redacted]"
  value is String -> redactText(value)
  value is Map<*, *> -> value.entries.associate { (k, v) -> k to redactValue(k as? String, v) }
  value is List<*> -> value.map { redactValue(null, it) }
  else -> value
}

internal fun scrubEvent(event: SentryEvent): SentryEvent {
  event.user = null
  event.message?.let { message ->
    message.message = redactText(message.message)
    message.formatted = redactText(message.formatted)
    message.params = message.params?.map { redactText(it) ?: it }
  }
  if (event.exceptions.isNotEmpty()) {
    event.exceptions = event.exceptions
      .map { it.copy(value = redactText(it.value)) }
      .toMutableList()
  }
  return event
}

internal fun scrubBreadcrumb(crumb: Breadcrumb): Breadcrumb {
  crumb.message = redactText(crumb.message)
  val data = crumb.getData() ?: return crumb
  val cleaned = LinkedHashMap<String, Any>(data.size)
  for ((key, value) in data) {
    cleaned[key] = if (key == "url" && value is String) {
      value.substringBefore('?')
    } else {
      redactValue(key, value) ?: value
    }
  }
  crumb.setData(cleaned)
  return crumb
}
