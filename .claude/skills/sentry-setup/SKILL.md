---
name: sentry-setup
description: Set up Sentry crash and error reporting in a Kotlin Multiplatform + Compose Multiplatform app (Android + iOS) using the unified sentry-kotlin-multiplatform SDK. Use this whenever the user wants to add Sentry, wire crash / error / observability reporting into a KMP project, configure dSYM or ProGuard/R8 mapping upload, initialize SentrySDK, scrub PII via beforeSend, add a crash smoke test, or decide between Sentry and Firebase Crashlytics (run both or migrate). Triggers on: Sentry, crash reporting, error monitoring, observability, sentry-kotlin-multiplatform, SentrySDK, dSYM, sentry-cli, beforeSend, DSN, SENTRY_AUTH_TOKEN.
---

# Sentry Setup for KMP (Android + iOS)

A field-tested recipe for wiring Sentry into a Kotlin Multiplatform + Compose Multiplatform project with one shared init path, PII scrubbing, symbol upload, and a crash smoke test. The unified `sentry-kotlin-multiplatform` SDK means you write `Sentry.init { }` once in `commonMain` and both platforms use it.

## Step 0 — Decide the crash-reporting posture

Sentry can either **replace** Firebase Crashlytics or **run alongside** it. Pick deliberately and record it in an ADR, because the choice changes what you remove.

- **Exclusive (migrate off Crashlytics).** Sentry becomes the only crash/error backend. Remove the Crashlytics Gradle plugin, its init/Strategy, and any `ComponentRegistrar` keep rule. One backend, one dashboard, simplest mental model.
- **Coexist (dual).** Keep Crashlytics and add Sentry. Justify _why_ in the ADR so it reads as intentional, not indecisive — e.g. Sentry for unified cross-platform errors + release health + PII-scrubbed events + performance, Crashlytics for the Firebase-native crash channel and Google ecosystem integration. Both initialize independently; no dedup is required because they ship to different backends.

Everything below is identical for both postures except the "remove Crashlytics" step (exclusive only).

## Step 1 — Add the SDK and Gradle plugins

Two Gradle plugins do different jobs. The KMP plugin installs the SDK into `commonMain`; the Android plugin only uploads the R8 mapping.

In `gradle/libs.versions.toml`:

```toml
[versions]
sentryKmp = "0.26.0"            # check latest before pinning
sentryAndroidGradle = "6.8.1"   # check latest before pinning

[plugins]
sentry-kmp = { id = "io.sentry.kotlin.multiplatform.gradle", version.ref = "sentryKmp" }
sentry-android = { id = "io.sentry.android.gradle", version.ref = "sentryAndroidGradle" }
```

Apply `sentry-kmp` in `:composeApp` (the module that owns the shared `Sentry.init`), and `sentry-android` in `:androidApp`. For iOS, add the `sentry-cocoa` SPM package (≥ 8.36.0) to the Xcode project — the KMP plugin links against it.

```kotlin
// :androidApp/build.gradle.kts
sentry {
    org.set(System.getenv("SENTRY_ORG") ?: "<org-slug>")
    projectName.set(System.getenv("SENTRY_PROJECT") ?: "<project-slug>")
    authToken.set(System.getenv("SENTRY_AUTH_TOKEN"))
    // Only upload when the token is present (local debug builds skip it):
    autoUploadProguardMapping.set(System.getenv("SENTRY_AUTH_TOKEN") != null)
    autoInstallation { enabled.set(false) }   // SDK comes from the KMP plugin, not here
    tracingInstrumentation { enabled.set(false) }
    uploadNativeSymbols.set(false)
    telemetry.set(false)
}
```

`autoInstallation.enabled = false` is the load-bearing line: without it the Android plugin tries to add its own SDK and collides with the KMP plugin's.

## Step 2 — Environment via expect/actual

Sentry's `environment` and `debug` flag should come from a platform-resolved config, not a hardcoded literal, so debug noise stays out of the production project.

```kotlin
// :shared:utils commonMain — extend the existing AppConfig
expect object AppConfig {
    val sentryEnvironment: String
    val enableLogging: Boolean
}
// androidMain: read a BuildConfig string ("debug" / "release"), default "production"
// iosMain: when { isDebug -> "debug"; bundleId endsWith ".sandbox" -> "sandbox"; else -> "production" }
```

## Step 3 — Shared init with PII scrubbing

Write `startSentry()` once in `:composeApp/commonMain`. The `beforeSend` / `beforeBreadcrumb` callbacks are not optional — events and breadcrumbs routinely carry emails, tokens, and request bodies, and shipping those to a third party is a privacy incident. See [references/sentry-reference.md](references/sentry-reference.md) for the complete scrubber + its unit test.

```kotlin
// :composeApp/.../observability/SentryInit.kt
fun startSentry() {
    Sentry.init { options ->
        options.dsn = SENTRY_DSN                       // public, safe to commit
        options.environment = AppConfig.sentryEnvironment
        options.debug = AppConfig.enableLogging
        options.sendDefaultPii = false                 // do not auto-capture IP / headers
        options.tracesSampleRate = 0.0                 // errors only; raise for performance
        options.beforeSend = ::scrubEvent
        options.beforeBreadcrumb = ::scrubBreadcrumb
    }
}
```

## Step 4 — Initialize on both platforms

Call `startSentry()` as early as possible so crashes during startup are captured.

- **Android:** first line of `Application.onCreate()` (before Koin / Firebase). If the project uses a build-type Strategy pattern (e.g. a `FirebaseConfigurator`), you may add a parallel `SentryConfigurator` for consistency, or just call `startSentry()` unconditionally — environment gating already happens inside.
- **iOS:** in `AppDelegate.application(_:didFinishLaunchingWithOptions:)`, call `SentryInitKt.startSentry()` (the generated Kotlin entry point) before Firebase init.

## Step 5 — Secrets

- **DSN** is public by design (it only identifies the project for ingestion). Hardcode it in `SentryInit.kt`. Do not treat it as a secret.
- **`SENTRY_AUTH_TOKEN`** uploads symbols and creates releases — it IS a secret. Keep it only in CI secrets and your local shell env. Never commit it; never put it in `gradle.properties` that is tracked.

## Step 6 — Symbol upload (readable stack traces)

Minified/native frames are useless without symbols.

- **Android (ProGuard/R8 mapping):** automatic. The `sentry-android` Gradle plugin uploads the mapping during `assembleRelease` when `SENTRY_AUTH_TOKEN` is present.
- **iOS (dSYM):** manual, in CI after the build:

```bash
curl -sL https://sentry.io/get-cli/ | INSTALL_DIR=/usr/local/bin bash
SENTRY_ORG=<org> SENTRY_PROJECT=<project> SENTRY_AUTH_TOKEN=$SENTRY_AUTH_TOKEN \
  sentry-cli debug-files upload --include-sources build
```

Note: iOS **Debug** builds produce no app `.dSYM`; for local debug symbolication you must run `dsymutil` first. Release/archive builds emit dSYMs normally.

## Step 7 — CI wiring

Add repository secrets `SENTRY_AUTH_TOKEN`, `SENTRY_ORG`, `SENTRY_PROJECT`. Pass them as env to the Android release job (the Gradle plugin picks them up) and add the `sentry-cli debug-files upload` step to the iOS release job. Per CI-budget discipline, run the upload step locally once against a non-shipping build before relying on it in CI.

## Step 8 — Smoke test

Verify ingestion end to end with a controlled crash behind a custom URL scheme, so you never ship a crash trigger but can fire one on demand.

```swift
// iOS iOSApp.swift onOpenURL
if url.scheme == "<scheme>" && url.host == "crash" {
    fatalError("Sentry smoke test: \(url)")
}
```

Trigger it on a booted simulator and confirm the event lands:

```bash
xcrun simctl openurl booted "<scheme>://crash"
```

If the project has deep-link routing, register `<scheme>://crash` through the same path. On Android, a debug-only menu action or `adb` broadcast works equally well.

## Step 9 — ADR

Record the Step 0 decision as an ADR (`docs/adr/NNNN-*.md`). The template is in [references/sentry-reference.md](references/sentry-reference.md). For the coexist posture, the ADR is what makes "two crash backends" read as a deliberate observability layering.

## Verification checklist

- `./gradlew :composeApp:compileCommonMainKotlinMetadata` — init compiles.
- `./gradlew :composeApp:allTests --tests "*Scrubber*"` — PII scrubber green.
- Fire the smoke crash → event appears in the Sentry project, and the payload contains no raw email / token (scrubber working).
- Exclusive posture only: confirm Crashlytics is fully removed (no plugin, no init, no keep rule) and the app still builds.
- Coexist posture only: confirm Crashlytics still reports too (both backends receive the smoke event or a second test crash).

## References

- [references/sentry-reference.md](references/sentry-reference.md) — full PII scrubber + unit test + ADR template + CI YAML snippets.
