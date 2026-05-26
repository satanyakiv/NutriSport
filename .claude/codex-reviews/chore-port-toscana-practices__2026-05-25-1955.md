# Codex Review — chore/port-toscana-practices — 2026-05-25-1955

Mode: --uncommitted (B1 Router-refactor)
Model: default (ChatGPT plan; gpt-5 blocked via API)
Tier: high (navigation/ + di/; auth file touched only by a nav method, no credential/token/session logic)
Severity: 0 P1 / 1 P2 / 0 P3 / 0 Q
Raw: raw/chore-port-toscana-practices\_\_2026-05-25-1955.json

## P1 verification (auto)

None reported.

## P2 — grouped by file

### navigation/src/commonMain/kotlin/com/nutrisport/navigation/NavGraph.kt (1)

- 43-60 (confidence 0.88): top-level navigation now depends on the `router.commands`
  collector being active. `DefaultRouter` emits with `tryEmit()` into a
  `MutableSharedFlow(replay = 0)`, so a command fired while the collector is detached
  (cold start, Activity recreation window) is silently dropped — async flows like
  logout / auth completion could leave the user on the wrong screen.

  **Disposition: DEFER (accepted tradeoff, not a B1 regression in practice).**
  - This is the intended port of the established ToscanaDivino Router pattern
    (`replay = 0` + `tryEmit` + host-attached collector), shipped in production there.
  - The cold-start case — the one that genuinely emits before the host attaches — is
    exactly what `Router.awaitReady()` (`subscriptionCount.first { it > 0 }`) exists for;
    it is consumed by the deeplink queue in Track B3 (`ColdStartDeeplinkQueue`).
  - The 9 refactored in-app flows all emit from a visible screen whose `NavHost` is
    composed, so the collector is always live — no drop on the B1 surface.
  - The suggested `replay = 1` fix is rejected: replaying the last command to a
    re-attached collector after Activity recreation would cause a spurious
    re-navigation. `replay = 0` is deliberate.
  - Making `navigateTo`/`back` suspend + `awaitReady()` before every emit would break
    fire-and-forget ViewModel ergonomics and diverge from the source pattern.
  - Durable cross-recreation delivery, if ever needed beyond cold-start deeplinks, is
    tracked with Track B (deeplink queue), not B1.

## P3 — collapsed

P3 nits: 0.

## Questions

None.
