# Deep Links

Cross-platform deep linking for Android and iOS via the shared `:core:deeplink` module. A single resolver + registry maps an incoming URL to a typed `Screen`, an auth gate decides whether to open it, and the `Router` performs the navigation. A cold-start readiness latch buffers links that arrive before the navigation graph is live, so a launch deep link is never dropped.

## Components

| Piece                          | Module           | Role                                                           |
| ------------------------------ | ---------------- | -------------------------------------------------------------- |
| `DefaultDeeplinkResolver`      | `:core:deeplink` | URL → `Screen` via path templates (first-match-wins)           |
| `DeeplinkRegistry`             | `:core:deeplink` | Single source of truth: path → `Screen` + `DeeplinkGate`       |
| `ColdStartDeeplinkQueue`       | `:core:deeplink` | Process-global buffer + `navReady` latch (race-safe)           |
| `DeeplinkBridge`               | `:core:deeplink` | Resolves, applies the gate, dispatches through `Router`        |
| `DeeplinkSwiftBridge`          | `:core:deeplink` | iOS Swift-facing `object`, reachable before Koin init          |
| `DeeplinkAuthGate`             | `:domain`        | `isSignedIn()` + `isAdmin()` — impl `FirebaseDeeplinkAuthGate` |
| `PendingDeeplinkStorage`       | `:domain`        | Parks a signed-out target for post-login replay                |
| `ColdStartDeeplinkDrainEffect` | `:core:deeplink` | Composable that drains the queue once `Router.awaitReady()`    |

## How it works

```
Android: VIEW intent ─▶ MainActivity
                         ├─ onCreate (cold, savedInstanceState==null) ─▶ ColdStartDeeplinkQueue.handle
                         └─ onNewIntent (warm, singleTop)             ─▶ DeeplinkBridge.handle
iOS:     onOpenURL ─────▶ DeeplinkSwiftBridge.handleUri ──────────────▶ ColdStartDeeplinkQueue.handle
                                                                            │
        DefaultDeeplinkResolver ◀── DeeplinkBridge.handle ◀────────────────┘
                │  (path template match → Screen + DeeplinkGate)
                ▼
        DeeplinkBridge.dispatch
                │  Public → open · SignedIn → open or park+Auth · Admin → open or drop
                ▼
        Router.replaceWith(screen) ─▶ SetupNavGraph collector ─▶ NavController
```

### Cold-start readiness latch

A launch link can arrive before `SetupNavGraph` attaches its `Router.commands` collector (and, on iOS, before Koin is initialized). Emitting straight through the `replay = 0` `MutableSharedFlow` would drop it. Instead:

- `ColdStartDeeplinkQueue` is an `object` (not a Koin `single`) so the iOS Swift adapter reaches it pre-Koin; it resolves `DeeplinkBridge` lazily.
- While `navReady` is `false`, every event is buffered (`AtomicReference` queue, race-safe enqueue-then-recheck).
- `ColdStartDeeplinkDrainEffect` in `composeApp` `AppContent` awaits `Router.awaitReady()` (which resolves the instant the collector subscribes — `subscriptionCount.first { it > 0 }`) and calls `markReadyAndDrain()`. The same effect serves both platforms.
- Once the latch is up, later (warm) links dispatch immediately.

## File structure

```
core/deeplink/src/commonMain/.../core/deeplink/
  DeeplinkSource.kt              — UniversalLink | CustomScheme | Push
  DeeplinkGate.kt                — Public | SignedIn | Admin
  ResolvedDeeplink.kt            — (Screen, DeeplinkGate)
  ColdStartDeeplinkQueue.kt      — buffer + navReady latch + drain
  DeeplinkBridge.kt              — resolve → gate → dispatch
  ColdStartDeeplinkDrainEffect.kt — @Composable drain trigger
  resolver/DefaultDeeplinkResolver.kt — path-template match + host folding
  resolver/UrlParts.kt           — minimal multiplatform URL parser
  registry/DeeplinkRegistry.kt   — path → Screen + gate table
  pending/InMemoryPendingDeeplinkStorage.kt
  di/DeeplinkModule.kt           — Koin singletons
core/deeplink/src/iosMain/.../DeeplinkSwiftBridge.kt — Swift entry object
domain/.../shared/domain/deeplink/  — DeeplinkAuthGate, PendingDeeplinkStorage (interfaces)
network/.../FirebaseDeeplinkAuthGate.kt — gate impl (signed-in + Firestore admin read)
```

## Registry

URLs use the `nutrisport://` custom scheme. For a custom scheme the host segment IS the first route segment, so the resolver folds it into the matchable path: `nutrisport://products/123` → `/products/123`. For `http(s)` Universal Links the host is the domain and is ignored — only the path drives resolution.

| Path                     | Screen                 | Gate     | Notes                                      |
| ------------------------ | ---------------------- | -------- | ------------------------------------------ |
| `/products/{id}`         | `Details(id)`          | Public   | `id` must be non-blank                     |
| `/categories/{category}` | `CategorySearch(name)` | Public   | Normalized like `valueOfProductCategory()` |
| `/profile`               | `Profile`              | SignedIn | Parked + redirected to Auth if signed out  |
| `/admin`                 | `AdminPanel`           | Admin    | Dropped for non-admins                     |

**Gate behavior:** Public opens unconditionally. SignedIn opens for an authenticated user, otherwise parks the target in `PendingDeeplinkStorage` and redirects to `Screen.Auth`; `AuthViewModel.goToHome()` replays the parked target after sign-in. Admin opens only for admins, otherwise the link is dropped silently (the admin surface is never revealed). `PendingDeeplinkStorage` is process-lifetime only — a parked target clears on process death and never auto-replays across cold starts.

**Intentionally excluded:** `Checkout` (needs a cart-derived `totalAmount` a link cannot supply); `Cart` / `ProductsOverview` / `Categories` (inner `HomeNavHost` tabs, not top-level `Router` destinations); `PaymentCompleted` / `ManageProduct` (server-issued or admin-write state).

## Testing

```bash
# Unit tests (resolver / registry / queue / bridge / pending)
./gradlew :core:deeplink:testAndroidHostTest --tests "*Deeplink*"

# Android E2E on a connected emulator (debug pkg = com.portfolio.nutrisport.debug)
adb shell am force-stop com.portfolio.nutrisport.debug                       # cold start
adb shell am start -W -a android.intent.action.VIEW -d "nutrisport://products/123"
adb shell am start -a android.intent.action.VIEW -d "nutrisport://profile"   # warm (singleTop → onNewIntent)

# iOS E2E on a booted simulator
xcrun simctl openurl booted "nutrisport://categories/protein"
```

Verify both paths: cold start (app not running → latch buffers → drain after ready) and warm start (app foregrounded → `onNewIntent` → direct dispatch).

## Not Covered (and why)

- **App Links / Universal Links** — no production domain yet; the `https` `autoVerify` intent-filter, `.well-known/assetlinks.json`, and the iOS Associated Domains capability are deliberately omitted. The resolver already handles `http(s)` (host = domain, ignored), so adding them later is configuration-only.
- **Push-tap deep links** — `DeeplinkBridge.handlePush` / `DeeplinkResolver.resolvePush` and the queue path exist and are unit-tested, but the platform notification-tap wiring is deferred until the KMPNotifier tap-payload contract is settled.
- **Tab deep links** (e.g. open the Cart tab) — would require tab switching in the `Router` contract and changes to the inner `HomeNavHost`; out of scope.

## Related

- [`.claude/rules/navigation.md`](../.claude/rules/navigation.md) — Router contract the bridge dispatches through
- [`docs/adr/0007-deeplink-architecture.md`](adr/0007-deeplink-architecture.md) — design rationale
- [`docs/adr/0004-compose-navigation-library.md`](adr/0004-compose-navigation-library.md) — the navigation library underneath
- [`.claude/skills/claude-in-mobile/SKILL.md`](../.claude/skills/claude-in-mobile/SKILL.md) — emulator/simulator deep-link launch mechanics
