# Sentry Reference — Scrubber, Test, ADR, CI

Full code and templates for [the sentry-setup skill](../SKILL.md). Adapt class names, package, and the exact `sentry-kotlin-multiplatform` API surface (event field accessors shift slightly between SDK versions) to the consuming project.

## Table of contents

- [PII scrubber (SentryInit.kt)](#pii-scrubber)
- [Scrubber unit test](#scrubber-unit-test)
- [ADR template](#adr-template)
- [CI snippets](#ci-snippets)

## PII scrubber

The goal: nothing that can identify a user or grant access ever leaves the device. Three layers — regex over free text, key-name redaction over structured maps, and nulling the user object. The redaction runs recursively because Sentry payloads nest (extras → maps → lists → strings).

```kotlin
package com.nutrisport.observability

import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.protocol.SentryEvent
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb

internal const val SENTRY_DSN = "<your-public-dsn>"

private val EMAIL = Regex("""[A-Za-z0-9.+_\-]+@[A-Za-z0-9_\-]+\.[A-Za-z0-9.\-]+""")
private val BEARER = Regex("""(?i)bearer\s+[A-Za-z0-9._/+\-]+=*""")
private val JWT = Regex("""eyJ[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+""")
private val SENSITIVE_KEY =
    Regex("""(?i)(authorization|cookie|password|passwd|secret|api[_-]?key|token|credential|otp)""")

internal fun redactText(input: String): String =
    input
        .replace(JWT, "[redacted-token]")
        .replace(BEARER, "Bearer [redacted]")
        .replace(EMAIL, "[redacted-email]")

private fun scrubValue(key: String?, value: Any?): Any? = when {
    key != null && SENSITIVE_KEY.containsMatchIn(key) -> "[redacted]"
    value is String -> redactText(value)
    value is Map<*, *> -> value.entries.associate { (k, v) -> k to scrubValue(k?.toString(), v) }
    value is List<*> -> value.map { scrubValue(null, it) }
    else -> value
}

fun scrubEvent(event: SentryEvent): SentryEvent {
    event.user = null                                  // drop user identity entirely
    event.message?.let { it.formatted = it.formatted?.let(::redactText) }
    // Walk extras / contexts / request maps with scrubValue(...) here per SDK API.
    return event
}

fun scrubBreadcrumb(breadcrumb: Breadcrumb): Breadcrumb {
    breadcrumb.message = breadcrumb.message?.let(::redactText)
    // For http breadcrumbs, strip the query string (params carry tokens/ids):
    (breadcrumb.data["url"] as? String)?.let { url ->
        breadcrumb.data["url"] = url.substringBefore("?")
    }
    return breadcrumb
}
```

## Scrubber unit test

The scrubber is pure string logic, so it tests cleanly in `commonTest` with no Sentry runtime. Cover each redaction class plus the "leaves clean text alone" case so a future edit can't silently over-redact.

```kotlin
package com.nutrisport.observability

import kotlin.test.Test
import com.willowtreeapps.assertk.assertThat
import com.willowtreeapps.assertk.assertions.contains
import com.willowtreeapps.assertk.assertions.doesNotContain
import com.willowtreeapps.assertk.assertions.isEqualTo

class SentryScrubberTest {

    @Test
    fun `redacts email addresses`() {
        val out = redactText("contact jane.doe@example.com now")
        assertThat(out).doesNotContain("jane.doe@example.com")
        assertThat(out).contains("[redacted-email]")
    }

    @Test
    fun `redacts bearer tokens`() {
        assertThat(redactText("Authorization: Bearer abc.DEF_123"))
            .doesNotContain("abc.DEF_123")
    }

    @Test
    fun `redacts JWTs`() {
        val jwt = "eyJhbGc.eyJzdWI.SflKxwRJ"
        assertThat(redactText("token=$jwt")).doesNotContain(jwt)
    }

    @Test
    fun `leaves ordinary text untouched`() {
        assertThat(redactText("user tapped checkout")).isEqualTo("user tapped checkout")
    }
}
```

## ADR template

```markdown
# NNNN — Crash and error observability backend

Status: Accepted (YYYY-MM-DD)

## Context

What crash/error visibility the app needs across Android + iOS, and which
backends are on the table (Sentry, Firebase Crashlytics, others).

## Decision

State the posture explicitly: "Sentry replaces Crashlytics" OR "Sentry runs
alongside Crashlytics". For the coexist posture, assign each backend a role so
the duplication is intentional:

- Sentry — unified cross-platform errors + breadcrumbs + release health +
  performance, with PII scrubbed before send.
- Crashlytics — Firebase-native crash channel and Google ecosystem integration.
- (debug-only flight recorder, if any) — local session replay.

## Consequences

- Symbol upload paths (R8 mapping auto, iOS dSYM via sentry-cli).
- SENTRY_AUTH_TOKEN becomes a required CI secret.
- (exclusive only) Crashlytics plugin / init / keep rules removed.
```

## CI snippets

Android release job — pass the env, the Gradle plugin uploads the mapping:

```yaml
- name: Build release
  env:
    SENTRY_AUTH_TOKEN: ${{ secrets.SENTRY_AUTH_TOKEN }}
    SENTRY_ORG: ${{ secrets.SENTRY_ORG }}
    SENTRY_PROJECT: ${{ secrets.SENTRY_PROJECT }}
  run: ./gradlew :androidApp:assembleRelease
```

iOS release job — upload dSYMs after the archive:

```yaml
- name: Upload dSYMs to Sentry
  env:
    SENTRY_AUTH_TOKEN: ${{ secrets.SENTRY_AUTH_TOKEN }}
    SENTRY_ORG: ${{ secrets.SENTRY_ORG }}
    SENTRY_PROJECT: ${{ secrets.SENTRY_PROJECT }}
  run: |
    curl -sL https://sentry.io/get-cli/ | INSTALL_DIR=/usr/local/bin bash
    sentry-cli debug-files upload --include-sources build
```
