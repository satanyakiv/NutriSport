# 04 — Offline-First Architecture

Status: IDLE
Group: B (first in sequence: 04→06→05→07)
Depends on: none

## Context

Currently the app reads directly from Firebase (Firestore) with no offline strategy. Products, cart, and customer data should be available offline via Room cache with background sync.

### doTerra Mobile Android Analysis

**Current doTerra implementation:**
- **RefreshPolicy enum**: `Now` (must fetch), `TryNow` (try, fallback cache), `None` (cache only)
- **WorkManager**: `FetchPromotionsWorker`, `FetchLrpWorker` for background sync
- **Room Flow as SSOT**: all reads through Room, remote only for sync
- **OkHttp cache** (10MB) for HTTP-level caching
- **Version-based invalidation**: catalog checks cache version
- **Conflict resolution**: Last-Write-Wins via `@Insert(onConflict = REPLACE)`
- **No ConnectivityManager**: graceful degradation through Either error handling

**Pros:**
- Simplicity: RefreshPolicy gives flexibility without complex state machine
- Stability: Room Flow guarantees UI always has data
- Background sync: WorkManager survives process death

**Cons:**
- No connectivity monitoring — user doesn't know if data is fresh
- LWW can overwrite local changes (in doTerra not critical — read-heavy)
- Version-based invalidation tied to app version, not data freshness

### Alternative A — Google Official (Recommended for NutriSport)

- `WorkManager` (Android) + coroutine-based timer (iOS)
- `SyncMetadataEntity` in Room for timestamp tracking per entity type
- `RefreshPolicy` enum in domain layer (pure Kotlin, no platform deps)
- **Deps**: `androidx.work:work-runtime-ktx` (Android only, expect/actual)

**Pros:** Production-proven, survives process death on Android
**Cons:** Android-only WorkManager, need expect/actual for iOS

### Alternative B — KMP Library

- `dev.tmapps:konnection` — KMP connectivity monitoring
- `com.russhwolf:multiplatform-settings` — sync metadata (simpler than Room table)
- Coroutine-based periodic sync (no WorkManager — cross-platform)
- **Deps**: both on kmp-awesome

**Pros:** True KMP, simpler setup
**Cons:** No process death survival, konnection less battle-tested

### Chosen Approach: Hybrid (A + parts of B)

- `RefreshPolicy` enum in domain (from doTerra pattern)
- Room as SSOT with `SyncMetadata` entity (timestamp-based, not version-based)
- `konnection` for connectivity monitoring (KMP, on kmp-awesome)
- Coroutine-based sync for both platforms (simpler than WorkManager for a portfolio project)
- Last-Write-Wins (acceptable — NutriSport is read-heavy like doTerra)

## Files to Create

- [ ] `shared/utils/.../domain/sync/RefreshPolicy.kt` — `Now`, `TryNow`, `CacheOnly` enum
- [ ] `shared/utils/.../domain/sync/SyncStatus.kt` — `Synced`, `Stale`, `Offline` sealed class
- [ ] `shared/utils/.../domain/sync/ConnectivityObserver.kt` — interface
- [ ] `database/.../entity/SyncMetadataEntity.kt` — entityType, lastSyncTimestamp, etag
- [ ] `database/.../dao/SyncMetadataDao.kt` — CRUD for sync metadata
- [ ] `data/.../sync/ConnectivityObserverImpl.kt` — konnection-based implementation
- [ ] `data/.../sync/SyncOrchestrator.kt` — coordinates refresh for all entity types

## Files to Modify

- [ ] `database/.../NutriSportDatabase.kt` — add SyncMetadataEntity + SyncMetadataDao
- [ ] `data/.../ProductRepositoryImpl.kt` — Room-first reads, Firebase sync
- [ ] `data/.../CustomerRepositoryImpl.kt` — Room-first reads, Firebase sync
- [ ] `shared/utils/.../domain/ProductRepository.kt` — add sync-aware methods
- [ ] `di/.../KoinModule.kt` — register SyncOrchestrator, ConnectivityObserver
- [ ] `gradle/libs.versions.toml` — add konnection dependency

## Dependencies (libs)

- `dev.tmapps:konnection:1.4.1` — KMP connectivity monitoring (kmp-awesome)

## Implementation Steps

1. Add `konnection` to version catalog
2. Create `RefreshPolicy` enum in domain layer (pure Kotlin)
3. Create `SyncStatus` sealed class in domain
4. Create `ConnectivityObserver` interface in domain
5. Add `SyncMetadataEntity` + `SyncMetadataDao` to database module
6. Update `NutriSportDatabase` with new entity and DAO
7. Implement `ConnectivityObserverImpl` using konnection in data module
8. Create `SyncOrchestrator` that:
   - Checks connectivity status
   - Compares timestamps in SyncMetadata
   - Decides whether to fetch from Firebase based on RefreshPolicy
   - Updates Room + SyncMetadata on successful sync
9. Refactor `ProductRepositoryImpl`:
   - `getProducts()` → reads from Room (Flow), triggers background sync
   - `syncProducts()` → fetches from Firebase, updates Room
10. Register new components in Koin

## Verification

```bash
# Compile check
./gradlew :shared:utils:compileCommonMainKotlinMetadata
./gradlew :database:compileCommonMainKotlinMetadata
./gradlew :data:compileCommonMainKotlinMetadata

# Full build
./gradlew assembleDebug
```

## Conflict Zones

- `NutriSportDatabase.kt` — also modified by 06, 07
- `ProductRepository.kt` — also modified by 05, 06
- `ProductRepositoryImpl.kt` — also modified by 05, 06
- `KoinModule.kt` — also modified by 07
- `libs.versions.toml` — also modified by 05, 10, 11
