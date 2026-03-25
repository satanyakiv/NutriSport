# Offline-First Architecture

NutriSport uses an offline-first pattern where the local Room database is the **single source of truth** (SSOT). The UI always reads from Room via reactive Flows, while background sync keeps the cache up-to-date with Firebase.

## Stack

| Component                | Module      | Purpose                                               |
| ------------------------ | ----------- | ----------------------------------------------------- |
| Room KMP 2.8.4           | `:database` | Local cache (entities, DAOs, migrations)              |
| Firebase Firestore       | `:network`  | Remote data source (snapshot listeners)               |
| `ConnectivityObserver`   | `:domain`   | Interface for network state monitoring                |
| `AndroidConnectivity...` | `:network`  | Android impl (`ConnectivityManager` + `callbackFlow`) |
| `IosConnectivityObs...`  | `:network`  | iOS impl (`NWPathMonitor` + `callbackFlow`)           |

## How It Works

```
┌──────────────┐     Flow<Product>     ┌──────────────┐
│    UI Layer   │◄─────────────────────│  Room (SSOT) │
│ (Composables) │                      │  ProductDao  │
└──────┬───────┘                      └──────▲───────┘
       │                                      │ upsert
       │ ConnectivityStatus                   │
       │                              ┌──────┴───────┐
┌──────▼───────┐                      │   Repository  │
│  OfflineBanner│                      │  (sync scope) │
│  Reconnected  │                      └──────▲───────┘
│    Prompt     │                             │ snapshots
└──────────────┘                      ┌──────┴───────┐
                                      │   Firebase    │
                                      │   Firestore   │
                                      └──────────────┘
```

**Data flow:**

1. Repository starts background sync via `SupervisorJob + Dispatchers.IO`
2. Firebase snapshot listener emits document changes
3. DTOs are mapped to entities and upserted into Room
4. Room Flow emits updated entities to UI automatically
5. If Firebase is unreachable, UI shows last cached data

## Sync vs Refresh

| Pattern            | Trigger     | Implementation                              | Returns         |
| ------------------ | ----------- | ------------------------------------------- | --------------- |
| **Passive Sync**   | Automatic   | `syncScope.launch { snapshots.collect {} }` | Fire-and-forget |
| **Active Refresh** | User action | `suspend fun refreshProductById(id)`        | `DomainResult`  |

Passive sync runs in a `CoroutineScope(SupervisorJob() + Dispatchers.IO)` — errors are logged but never crash the UI. Active refresh is a one-shot suspend function that the ViewModel awaits.

## ConnectivityObserver

**Interface** lives in `:domain` (dependency inversion, same pattern as `CoroutineDispatcherProvider`):

```kotlin
enum class ConnectivityStatus { Available, Unavailable, Losing }

interface ConnectivityObserver {
  val status: Flow<ConnectivityStatus>
}
```

**Platform implementations** live in `:network` source sets:

- **Android:** `ConnectivityManager.registerDefaultNetworkCallback()` wrapped in `callbackFlow`
- **iOS:** `NWPathMonitor` with `nw_path_status_satisfied` check

**Key principle:** ConnectivityObserver is for **UX only** (showing banners), not for data flow decisions. Repositories always attempt sync and handle errors gracefully.

## Price Tracking

Database-level tracking via `previouslyKnownPrice` field on `ProductEntity`:

```
┌───────────────────────────────────────────────┐
│ Sync arrives with new price                   │
│                                               │
│   currentEntity.price → previouslyKnownPrice  │
│   newDto.price        → price                 │
│                                               │
│ If prices are equal → previouslyKnownPrice    │
│   stays unchanged                             │
└───────────────────────────────────────────────┘
```

- `ProductDtoToEntityMapper.resolvePreviousPrice()` handles the logic
- UI compares `price` vs `previouslyKnownPrice` — if different, shows `PriceChangeBanner`
- User acknowledges → `ProductDao.clearPreviousPrice(id)` sets field to `NULL`

**Room Migration v1 → v2** adds the `previouslyKnownPrice` column:

```sql
ALTER TABLE products ADD COLUMN previouslyKnownPrice REAL DEFAULT NULL
```

## Cache Management

- **Sign out:** deletes all `CustomerEntity` + cascades to `CartItemEntity`
- **Product cache:** persists indefinitely, updated by sync
- **No TTL:** Firestore snapshot listeners keep cache fresh while app is active
- **Schema versioning:** migrations in `database/migration/Migrations.kt`

## Checkout Price Validation

At checkout entry, `ValidateCartPricesUseCase` compares cart item prices with current `Product.previouslyKnownPrice`. If mismatches exist, a dialog lists changed prices and asks the user to confirm or cancel.

## Not Covered (and Why)

- **2-step checkout with server-side reservation** — requires Cloud Functions or a backend API for booking products with TTL, rollback on timeout, and a separate `pre-orders` Firestore collection. Out of scope for a mobile portfolio project. Production approach: create pre-order → reserve for N minutes → show final price → confirm or cancel → rollback if timeout.

- **Optimistic writes (offline queue)** — writing changes locally with sync-on-reconnect. Not implemented because cart operations write directly to Firestore and require server confirmation. Production: `SyncStatus` enum in entity (SYNCED, PENDING, CONFLICTED), WorkManager for retry, conflict resolution strategy.

- **Delta sync** — current sync replaces the entire cache (Server Wins). Inefficient for large catalogs. Production: `updatedAt` timestamp + incremental fetch of only changed documents.

- **Conflict resolution UI** — multi-device scenarios where the cart is modified from two devices simultaneously. Currently Server Wins automatically. Production: merge strategy with a user-facing dialog.

- **Background periodic sync (WorkManager / BGTaskScheduler)** — data is only refreshed while the app is active (Firestore snapshot listeners). Production: periodic background fetch via KMP WorkManager wrapper.

## Related

- [Testing Guide](TESTING.md) — test stack, FakeConnectivityObserver, Robolectric UI tests
- `.claude/rules/architecture.md` — module dependency flow
- `.claude/rules/error-handling.md` — Either, AppError, DomainResult patterns
