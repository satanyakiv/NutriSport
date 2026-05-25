# 0006 — Strategy pattern for build-type-specific behavior

Status: Accepted (2026-04-01)

## Context

Some behavior must differ per build type: debug wants the Tracey flight recorder and verbose Firebase config; release wants production Firebase; benchmark wants Firebase skipped entirely (fake data). Scattering `if (BuildConfig.DEBUG)` / `if (USE_FAKE_DATA)` branches through `Application.onCreate()` is brittle and hard to test.

## Decision

Use polymorphism instead of build-flag branching. Define an interface in `androidApp/src/main/` (e.g. `FirebaseConfigurator`) plus a `DebugToolkit` interface in `:navigation`, and provide per-source-set implementations: `DebugFirebaseConfigurator` / `ReleaseFirebaseConfigurator`, `TraceyDebugToolkit` (debug) / `NoOpDebugToolkit`. A `DebugModuleProvider` in each source set (`src/{debug,release,benchmark}/`) registers the right implementation in Koin; `Application.onCreate()` makes a single polymorphic call.

## Consequences

- No build-flag `if`/`else` in `Application.onCreate()`; the variant source set selects behavior at compile time.
- Debug-only libraries (Tracey, LeakCanary) stay `debugImplementation` in `androidApp` and never leak into `commonMain` or release.
- Adding new build-type behavior means adding a Strategy + wiring it in `DebugModuleProvider`, not touching shared code.

## Alternatives considered

- **`if (BuildConfig.DEBUG)` branches.** Simple at first, but multiplies across init code and drags debug-only deps toward `commonMain`. Rejected.
- **Product flavors.** Heavier than the three build types need; the source-set Strategy is enough. Rejected.
