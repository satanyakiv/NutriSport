# 05 — Cursor-Based Pagination

Status: IDLE
Group: B (sequence: 04→06→05→07)
Depends on: 04-offline-first, 06-search-filtering

## Context

Currently all products load at once from Firebase. With offline-first in place (04) and search (06), pagination ensures scalability. Uses cursor-based pagination with Firestore `startAfter()` and Room `LIMIT/OFFSET`.

## Files to Create

- [ ] `shared/utils/.../domain/pagination/PaginatedResult.kt` — `data class PaginatedResult<T>(items: List<T>, hasMore: Boolean, cursor: String?)`
- [ ] `shared/utils/.../domain/pagination/PagingConfig.kt` — `data class PagingConfig(pageSize: Int = 20)`

## Files to Modify

- [ ] `shared/utils/.../domain/ProductRepository.kt` — add `getProductsPaginated(cursor, config): DomainResult<PaginatedResult<Product>>`
- [ ] `data/.../ProductRepositoryImpl.kt` — implement paginated query with Firestore `startAfter()` + Room cache
- [ ] `database/.../dao/ProductDao.kt` — add `getProductsPaged(limit: Int, offset: Int): Flow<List<ProductEntity>>`
- [ ] `feature/home/.../HomeViewModel.kt` — load-more logic with cursor tracking
- [ ] `feature/home/.../HomeScreen.kt` — lazy column with load-more trigger

## Dependencies (libs)

None — uses Firestore built-in cursor pagination + Room LIMIT/OFFSET.

## Implementation Steps

1. Create `PaginatedResult<T>` and `PagingConfig` in domain
2. Add paginated query to `ProductRepository` interface
3. Add `getProductsPaged()` to `ProductDao`
4. Implement in `ProductRepositoryImpl`:
   - First page: Room cache + background Firestore fetch
   - Next pages: Firestore `startAfter(lastDocument)` → save to Room → return
   - Track cursor (last document snapshot or ID)
5. Update `HomeViewModel`:
   - `MutableStateFlow<List<Product>>` with append logic
   - `loadMore()` function triggered by scroll threshold
   - `isLoadingMore` state for footer indicator
6. Update `HomeScreen`:
   - `LazyColumn` / `LazyVerticalGrid` with `LaunchedEffect` at threshold
   - Loading footer composable

## Verification

```bash
./gradlew :shared:utils:compileCommonMainKotlinMetadata
./gradlew :data:compileCommonMainKotlinMetadata
./gradlew :feature:home:compileCommonMainKotlinMetadata
./gradlew assembleDebug
```

## Conflict Zones

- `ProductRepository.kt` — also modified by 04, 06
- `ProductRepositoryImpl.kt` — also modified by 04, 06
- `ProductDao.kt` — also modified by 06
