# 07 — Favorites Feature Module

Status: IDLE
Group: B (sequence: 04→06→05→07)
Depends on: 04-offline-first

## Context

No favorites/wishlist feature exists. This is a full-cycle feature module demonstrating the complete architecture: domain model → database entity → repository → use case → ViewModel → Screen → navigation → DI.

## Files to Create

### Domain (shared:utils)
- [ ] `shared/utils/.../domain/Favorite.kt` — `data class Favorite(val productId: String, val addedAt: Long)`
- [ ] `shared/utils/.../domain/FavoriteRepository.kt` — interface
- [ ] `shared/utils/.../domain/usecase/ToggleFavoriteUseCase.kt`
- [ ] `shared/utils/.../domain/usecase/ObserveFavoritesUseCase.kt`

### Database
- [ ] `database/.../entity/FavoriteEntity.kt` — Room entity
- [ ] `database/.../dao/FavoriteDao.kt` — CRUD + observe

### Data
- [ ] `data/.../FavoriteRepositoryImpl.kt` — Room + Firebase sync
- [ ] `data/.../dto/FavoriteDto.kt` — Firestore DTO
- [ ] `data/.../mapper/FavoriteMapper.kt` — Dto↔Domain↔Entity mappers

### Feature Module
- [ ] `feature/favorites/build.gradle.kts` — `nutrisport.kmp.feature` plugin
- [ ] `feature/favorites/.../FavoritesViewModel.kt`
- [ ] `feature/favorites/.../FavoritesScreen.kt`
- [ ] `feature/favorites/.../model/FavoriteUi.kt`
- [ ] `feature/favorites/.../mapper/FavoriteUiMapper.kt`

## Files to Modify

- [ ] `database/.../NutriSportDatabase.kt` — add FavoriteEntity + FavoriteDao
- [ ] `navigation/.../Screen.kt` — add `Screen.Favorites` route
- [ ] `navigation/.../SetupNavGraph.kt` — add favorites destination
- [ ] `di/.../KoinModule.kt` — register FavoriteRepository, UseCases, ViewModel
- [ ] `settings.gradle.kts` — include `:feature:favorites`
- [ ] `composeApp/build.gradle.kts` — add dependency on `:feature:favorites`
- [ ] `navigation/build.gradle.kts` — add dependency on `:feature:favorites`

## Dependencies (libs)

None — uses existing stack.

## Implementation Steps

1. Create domain models and interfaces in shared:utils
2. Create `FavoriteEntity` + `FavoriteDao` in database module
3. Update `NutriSportDatabase` with new entity/DAO
4. Create `FavoriteDto` + `FavoriteMapper` in data module
5. Implement `FavoriteRepositoryImpl` with Room-first + Firebase sync
6. Create use cases: `ToggleFavoriteUseCase`, `ObserveFavoritesUseCase`
7. Create feature module with convention plugin
8. Add to `settings.gradle.kts`
9. Create `FavoriteUi` model + mapper
10. Create `FavoritesViewModel` with state management
11. Create `FavoritesScreen` with favorites list
12. Add `Screen.Favorites` route + navigation destination
13. Register everything in Koin
14. Add favorite toggle button to product detail screen

## Verification

```bash
./gradlew :feature:favorites:compileCommonMainKotlinMetadata
./gradlew :database:compileCommonMainKotlinMetadata
./gradlew :data:compileCommonMainKotlinMetadata
./gradlew assembleDebug

# Verify module is included
./gradlew projects | grep favorites
```

## Conflict Zones

- `NutriSportDatabase.kt` — also modified by 04, 06
- `KoinModule.kt` — also modified by 04
- `Screen.kt` / `SetupNavGraph.kt` — unique to this feature
- `settings.gradle.kts` — low conflict risk (append only)
