# 0003 — Offline-first with Room as the single source of truth

Status: Accepted (2026-04-01)

## Context

A mobile shopping app must stay usable on a flaky connection. If the UI reads directly from Firestore, every screen depends on live network and flickers between loading and content as snapshots arrive. We want the UI to render instantly from local data and treat the network as a background sync.

## Decision

The local Room (KMP) database is the single source of truth. The UI layer only ever reads `Flow<T>` from Room DAOs; it never reads Firestore directly. A background sync writes Firestore snapshot data into Room, and Room's reactive queries push updates to the UI. A `ConnectivityObserver` (`:domain` interface, Android `ConnectivityManager` + iOS `NWPathMonitor` impls in `:network`) drives sync vs cached-read decisions.

## Consequences

- UI is decoupled from network latency and renders from cache immediately.
- Room is the contract the UI depends on; `:network` maps Firestore DTOs into Room/domain models, which never leak outward.
- Room DAOs/entities are excluded from Kover (need instrumented tests), so the data layer carries 0% unit coverage by design.

## Alternatives considered

- **Read Firestore directly with snapshot listeners.** Simpler, but couples UI to network state and offers no offline story. Rejected.
- **In-memory cache only.** Loses data across process death; no durable offline. Rejected.

## References

[`docs/OFFLINE_FIRST.md`](../OFFLINE_FIRST.md)
