# 0001 — Sentry alongside Crashlytics (dual observability)

Status: Accepted (2026-05-25)

## Context

The app already reports crashes through Firebase Crashlytics (release) and captures debug sessions with the Tracey flight recorder. Crashlytics is Android-strong and tied to the Firebase ecosystem, but it does not give a single cross-platform view of _non-fatal_ errors, breadcrumbs, or release health across Android and iOS, and its iOS story is weaker. We want one place to triage errors from both platforms with PII controlled before send.

## Decision

Add Sentry via the unified `sentry-kotlin-multiplatform` SDK and run it **alongside** Crashlytics rather than replacing it. Each backend owns a clear role, so the duplication is deliberate:

- **Sentry** — unified cross-platform errors, breadcrumbs, release health, and (later) performance, with PII scrubbed before any event leaves the device.
- **Crashlytics** — the Firebase-native crash channel and Google ecosystem integration; unchanged.
- **Tracey** — debug-only local session replay; unchanged.

No deduplication is required because each backend ingests independently. A single `Sentry.init` in `commonMain` serves both platforms.

## Consequences

- One shared `startSentry()` with a mandatory PII scrubber (`beforeSend` / `beforeBreadcrumb`).
- `SENTRY_AUTH_TOKEN` becomes a required CI secret for symbol upload; `SENTRY_URL` must point at the EU region (`https://de.sentry.io`).
- Two crash backends to keep in mind when triaging; the role split above is the guard against that becoming confusing.
- iOS reporting is pending `sentry-cocoa` SPM linkage in the Xcode project.

## Alternatives considered

- **Migrate off Crashlytics entirely.** Cleaner single dashboard, but discards working Firebase-native crash reporting and the existing setup for no concrete gain at this stage. Rejected in favor of additive adoption.
