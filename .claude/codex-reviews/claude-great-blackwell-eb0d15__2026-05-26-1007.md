# Codex Review — claude-great-blackwell-eb0d15 — 2026-05-26-1007

Mode: --uncommitted (staged B2: :core:deeplink module + domain interfaces + network gate + DI wiring)
Model: default (ChatGPT plan; gpt-5 blocked via API)
Tier: high (navigation / core / di)
Severity: 3 P1 / 1 P2 / 0 P3 / 0 Q
Raw: raw/claude-great-blackwell-eb0d15\_\_2026-05-26-1007.json

## P1 — triaged (all are "feature not yet wired", i.e. the planned next steps)

- [deferred → B4/B5] DeeplinkBridge entrypoints not called from app. MainActivity does
  not forward intent.data/onNewIntent; iOSApp.swift onOpenURL only handles Google.
  → Resolved by B4 (Android wiring) + B5 (iOS wiring), which follow this commit.
- [deferred → B3] ColdStartDeeplinkDrainEffect defined but not installed, so navReady
  never flips and the queue never drains.
  → Resolved by B3 (install in composeApp AppContent), which follows this commit.
- [deferred → B7-replay] Signed-out deeplink parked but never replayed; auth flow never
  reads PendingDeeplinkStorage.take().
  → Resolved by the AuthViewModel post-login replay step, which follows this commit.

Rationale: this commit is the self-contained, fully unit-tested :core:deeplink foundation
(mirrors the B1 "Add Router navigation abstraction (foundation)" commit that preceded its
own wiring). The three P1s are precisely the wiring steps tracked as the next commits; the
live adb/simctl E2E at the end of the track proves an external URL reaches the Router.

## P2 — fixed in this commit

- DeeplinkRegistry category route accepted only exact ProductCategory.name, so
  `nutrisport://categories/pre-workout` failed. Fixed by normalizing the captured slug the
  same way the domain's `valueOfProductCategory()` does (lowercase + letters-only) before
  matching `ProductCategory.entries`. Added resolver test for the hyphenated slug.

## P3 / Questions

None.
