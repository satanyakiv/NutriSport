# Sentry

Cross-platform crash and error reporting via the unified `sentry-kotlin-multiplatform` SDK. Sentry runs **alongside** Firebase Crashlytics and the debug-only Tracey flight recorder (dual observability, see [ADR-0001](adr/0001-observability-sentry-alongside-crashlytics.md)). One `Sentry.init` in `commonMain` serves both Android and iOS.

## Stack

| Tool                                             | Version  | Purpose                                                    |
| ------------------------------------------------ | -------- | ---------------------------------------------------------- |
| `io.sentry.kotlin.multiplatform` (Gradle plugin) | 0.26.0   | Installs the SDK into `commonMain`, links platform SDKs    |
| `io.sentry.android.gradle`                       | 6.8.1    | Uploads the R8 mapping (only; `autoInstallation` disabled) |
| `sentry-cocoa` (SPM)                             | ≥ 8.36.0 | iOS native SDK the KMP plugin links against (pending)      |

Project: org `nutrisport`, project `nutrisport`, region **EU** (`de`).

## How it works

```
Application.onCreate (Android) / AppDelegate (iOS, pending)
        │
        ▼
  startSentry()  ── composeApp/.../observability/SentryInit.kt
        │  Sentry.init { dsn, environment, beforeSend, beforeBreadcrumb }
        ▼
  scrubEvent / scrubBreadcrumb  ── strip PII before any event leaves the device
        ▼
  nutrisport.sentry.io  (EU region)
```

Crashlytics and Tracey are untouched and report independently — no dedup is needed because each ships to its own backend.

## File structure

```
composeApp/src/commonMain/kotlin/com/nutrisport/observability/
  SentryInit.kt                  — startSentry() + PII scrubber (shared, both platforms)
shared/utils/src/{common,android,ios}Main/.../AppConfig.kt
  sentryEnvironment              — "debug" / "production", the single build-config seam
androidApp/build.gradle.kts      — sentry { } block (mapping upload), plugin applied
androidApp/.../NutrisportApplication.kt — startSentry() call in onCreate
```

## Configuration

- **DSN** is hardcoded in `SentryInit.kt`. It is public by design (it only identifies the ingestion endpoint) and safe to commit.
- **Environment** comes from `AppConfig.sentryEnvironment` (`debug` in debug builds, `production` otherwise). No `if (isDebug)` leaks into call sites.
- **`tracesSampleRate = 0.0`** — errors/crashes only for now; raise to sample performance traces.

## PII scrubbing

`beforeSend` and `beforeBreadcrumb` run on every event. Shipping a user's email or token to a third party is a data-processing incident, so scrubbing is mandatory, not optional:

- Regex redaction of emails, `Bearer` tokens, and JWTs in messages and exception values.
- Key-name redaction for structured data (`authorization`, `cookie`, `password`, `secret`, `api_key`, `token`, `credential`, `otp`).
- HTTP breadcrumb URLs have their query string stripped (params carry tokens/ids).
- `event.user` is nulled and `sendDefaultPii = false` (no auto IP/header capture).

## Symbol upload

| Platform             | Mechanism                                                                                        |
| -------------------- | ------------------------------------------------------------------------------------------------ |
| Android (R8 mapping) | Automatic via the Gradle plugin on `assembleRelease`, gated on `SENTRY_AUTH_TOKEN` being present |
| iOS (dSYM)           | `sentry-cli debug-files upload --include-sources build` in CI (pending iOS init)                 |

CI secrets: `SENTRY_AUTH_TOKEN` (secret, never committed), `SENTRY_ORG=nutrisport`, `SENTRY_PROJECT=nutrisport`, `SENTRY_URL=https://de.sentry.io` (the EU region; without it uploads target the US and fail).

## Verification

```bash
./gradlew :composeApp:compileCommonMainKotlinMetadata   # SentryInit compiles against the SDK
./gradlew :androidApp:assembleDebug                      # plugin + sentry{} + init wire up
```

End-to-end ingestion is confirmed once a crash trigger exists (the `nutrisport://crash` smoke test arrives with deep-link routing) or by launching the app and observing a session in `nutrisport.sentry.io`.

## Not Covered (and Why)

- **iOS initialization** — needs the `sentry-cocoa` SPM package linked in the Xcode project before `SentryInitKt.startSentry()` can run in `AppDelegate`. Tracked separately; safer done in Xcode than by editing `.pbxproj`.
- **Scrubber unit test** — `:composeApp` has no host-test source set yet; the scrubber is exercised by the live smoke test until that infra lands.
- **Performance tracing / release health** — `tracesSampleRate` is 0; only errors are sent.
- **Migrating off Crashlytics** — deliberately not done; the dual posture is the decision, see ADR-0001.

## Related

- [ADR-0001 — Sentry alongside Crashlytics](adr/0001-observability-sentry-alongside-crashlytics.md)
- [CRASHLYTICS.md](CRASHLYTICS.md) — the Firebase-native crash channel that runs in parallel
- [TRACEY.md](TRACEY.md) — debug-only flight recorder
- [`.claude/skills/sentry-setup`](../.claude/skills/sentry-setup/SKILL.md) — the reusable recipe this setup follows
