# 0007 — Deep-link architecture + Router readiness latch

Status: Accepted (2026-05-26)

## Context

The app needs cross-platform deep links (Android + iOS, from `commonMain`) that route to typed `Screen` destinations and respect auth. Two hard problems: (1) a launch link can arrive before the navigation graph attaches its `Router.commands` collector — and on iOS before Koin is initialized — so a naive emit into a `replay = 0` flow is silently dropped; (2) NutriSport has no `AccountStatus` model, only a signed-in check and a Firestore admin flag, so gating must be expressed in those terms. Builds on [ADR-0004](0004-compose-navigation-library.md) (Compose Navigation + `Router`).

## Decision

A dedicated `:core:deeplink` module owns the subsystem:

- **Resolver + registry.** `DefaultDeeplinkResolver` matches path templates first-match-wins against a single `DeeplinkRegistry`. For the `nutrisport://` custom scheme the host segment is folded into the matchable path (`nutrisport://products/123` → `/products/123`); for `http(s)` the host is the domain and is ignored.
- **Three-tier gate.** `DeeplinkGate { Public, SignedIn, Admin }` replaces the source pattern's boolean. `DeeplinkAuthGate` (in `:domain`, impl in `:network`) exposes `isSignedIn()` + `isAdmin()`. SignedIn-without-session parks the target in `PendingDeeplinkStorage` and redirects to auth; Admin-without-rights drops silently.
- **Cold-start readiness latch.** `ColdStartDeeplinkQueue` is a process-global `object` (not a Koin `single`) so the iOS Swift bridge reaches it before Koin init; it resolves `DeeplinkBridge` lazily. A `navReady` `AtomicReference` latch buffers events until `ColdStartDeeplinkDrainEffect` (in `AppContent`) sees `Router.awaitReady()` resolve — implemented as `commands.subscriptionCount.first { it > 0 }`, a real signal rather than a timer — then drains.
- **Post-login replay.** `AuthViewModel.goToHome()` resumes `PendingDeeplinkStorage.take()` after sign-in, else navigates Home.

## Consequences

- A launch deep link is never dropped, on either platform, without platform-specific "nav ready" plumbing — the shared Compose effect drives it.
- Gating is centralized and testable; `:core:deeplink` stays free of Firebase (gate via a `:domain` interface).
- `PendingDeeplinkStorage` is process-lifetime by design: a parked target clears on process death and never auto-replays days later. The minor edge — abandoning the auth flow then signing in later still replays the parked target in the same process — is accepted as the simpler, source-consistent contract.
- New linkable screens are one registry row; transactional/admin-write/inner-tab screens are intentionally excluded (see [docs/DEEPLINKS.md](../DEEPLINKS.md)).

## Alternatives considered

- **Timer-based "nav ready" delay.** Fragile and racy; `subscriptionCount` is the precise signal. Rejected.
- **Koin `single` queue.** Cannot be reached during the iOS pre-Koin cold-start window. Rejected in favor of a process-global `object`.
- **Boolean `authGated`.** Cannot express the signed-in vs admin distinction NutriSport needs. Rejected for the three-tier enum.
- **App Links / Universal Links now.** Deferred — no production domain; the resolver already handles `http(s)`, so it is configuration-only later.
