# 06 — Search & Filtering

Status: IDLE
Group: B (sequence: 04→06→05→07)
Depends on: 04-offline-first

## Context

No search exists. With offline-first (04) providing Room as SSOT, we can use Room FTS (Full-Text Search) for fast local search + combined filters (category, price range). Firebase search is limited — FTS on Room is the right approach for a cached dataset.

## Files to Create

- [ ] `database/.../entity/ProductFts.kt` — `@Fts4` virtual table for product search
- [ ] `domain/.../domain/search/SearchQuery.kt` — `data class SearchQuery(text: String, category: ProductCategory?, priceRange: ClosedRange<Double>?)`
- [ ] `domain/.../domain/search/SearchRepository.kt` — interface
- [ ] `network/.../SearchRepositoryImpl.kt` — Room FTS + filter combination
- [ ] `feature/home/.../search/SearchBar.kt` — composable search bar
- [ ] `feature/home/.../search/FilterSheet.kt` — bottom sheet with category/price filters

## Files to Modify

- [ ] `database/.../NutriSportDatabase.kt` — add ProductFts entity
- [ ] `database/.../dao/ProductDao.kt` — add FTS query methods
- [ ] `network/.../ProductRepositoryImpl.kt` — populate FTS table on sync
- [ ] `domain/.../domain/ProductRepository.kt` — add search method (or use SearchRepository)
- [ ] `feature/home/.../HomeViewModel.kt` — search state + debounced query
- [ ] `feature/home/.../HomeScreen.kt` — integrate SearchBar + FilterSheet
- [ ] `di/.../KoinModule.kt` — register SearchRepository if separate

## Dependencies (libs)

None — Room FTS4 is built-in.

## Implementation Steps

1. Create `@Fts4` entity `ProductFts` mirroring searchable Product fields (name, description, category)
2. Add FTS rebuild trigger in `ProductDao` (rebuild on product insert/update)
3. Create `SearchQuery` data class in domain
4. Create `SearchRepository` interface in domain (or extend ProductRepository)
5. Implement `SearchRepositoryImpl`:
   - FTS match query for text search
   - SQL WHERE for category filter
   - SQL WHERE for price range
   - Combine all with AND logic
6. Update `HomeViewModel`:
   - `searchQuery: MutableStateFlow<SearchQuery>`
   - Debounce text input (300ms) with `debounce()` operator
   - `flatMapLatest` to search results
7. Create `SearchBar` composable with text field + clear button
8. Create `FilterSheet` with category chips + price range slider
9. Integrate into `HomeScreen`

## Verification

```bash
./gradlew :database:compileCommonMainKotlinMetadata
./gradlew :network:compileCommonMainKotlinMetadata
./gradlew :feature:home:compileCommonMainKotlinMetadata
./gradlew assembleDebug
```

## Conflict Zones

- `NutriSportDatabase.kt` — also modified by 04, 07
- `ProductDao.kt` — also modified by 05
- `domain/.../ProductRepository.kt` — also modified by 04, 05
- `network/.../ProductRepositoryImpl.kt` — also modified by 04, 05
