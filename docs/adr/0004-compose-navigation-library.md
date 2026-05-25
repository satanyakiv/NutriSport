# 0004 — Compose Navigation as the navigation library

Status: Accepted (2026-04-01)

## Context

A KMP + Compose Multiplatform app needs type-safe navigation that works on Android and iOS from `commonMain`. Options range from the official Compose Navigation (JetBrains KMP fork) to third-party frameworks (Decompose, Voyager, Circuit) and the newer Navigation 3.

## Decision

Use the official Compose Navigation (JetBrains KMP fork): `NavHost` + typed `composable<Screen.X>` destinations driven by a `@Serializable sealed class Screen` in `:shared:utils`. Destinations are wired in `:navigation` (`NavGraph.kt`); feature Screens stay stateless and receive navigation intent via callbacks.

## Consequences

- Type-safe routes via the `Screen` sealed class; args travel as serializable route fields.
- First-party library — tracks Compose/CMP releases, no third-party framework lock-in.
- Navigation logic concentrated in `:navigation`; features do not depend on each other.
- A `Router` abstraction (decoupling ViewModels from the NavController, and enabling deep-link dispatch) is a planned evolution on top of this library, not a replacement for it.

## Alternatives considered

- **Decompose / Voyager / Circuit.** Powerful, but add a framework dependency and a learning surface; the official library covers the app's needs. Rejected for footprint.
- **Navigation 3.** Promising but not yet stable for CMP + iOS multi-back-stack at decision time. Deferred until the KMP fork stabilizes.
