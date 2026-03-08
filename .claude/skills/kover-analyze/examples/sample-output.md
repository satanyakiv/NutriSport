# Sample `/kover-analyze` Output

## A) Summary

| Category   | Classes | Current | Target | Status |
|------------|---------|---------|--------|--------|
| ViewModel  | 5       | 62%     | 80%    | BELOW  |
| UseCase    | 7       | 88%     | 90%    | BELOW  |
| Repository | 4       | 71%     | 70%    | OK     |
| Mapper     | 3       | 95%     | 90%    | OK     |
| Utils      | 2       | 85%     | 90%    | BELOW  |

## B) Priority Targets (top 10)

| # | Class | Category | Current | Target | Gap | Action |
|---|-------|----------|---------|--------|-----|--------|
| 1 | `ProductRepositoryImpl` | Repository | 45% | 70% | -25% | `/gen-test ProductRepositoryImpl` |
| 2 | `CartViewModel` | ViewModel | 52% | 80% | -28% | `/gen-test CartViewModel` |
| 3 | `ProfileViewModel` | ViewModel | 60% | 80% | -20% | `/gen-test ProfileViewModel` |
| 4 | `ProductMapper` | Mapper | 72% | 90% | -18% | `/gen-test ProductMapper` |
| 5 | `ObserveEnrichedCartUseCase` | UseCase | 75% | 90% | -15% | `/gen-test ObserveEnrichedCartUseCase` |
| 6 | `DetailsViewModel` | ViewModel | 68% | 80% | -12% | `/gen-test DetailsViewModel` |
| 7 | `OrderRepositoryImpl` | Repository | 60% | 70% | -10% | `/gen-test OrderRepositoryImpl` |
| 8 | `Either` | Utils | 82% | 90% | -8% | `/gen-test Either` |
| 9 | `ValidateProfileFormUseCase` | UseCase | 85% | 90% | -5% | `/gen-test ValidateProfileFormUseCase` |
| 10 | `HomeViewModel` | ViewModel | 76% | 80% | -4% | `/gen-test HomeViewModel` |

## C) New excludes (not in current `build.gradle.kts`)

**Generated Resources (126 classes):**
- `nutrisport.shared.ui.generated.resources.*`
- `nutrisport.composeapp.generated.resources.*`

**Analytics:**
- `com.nutrisport.analytics.*`

**UI Models:**
- `com.nutrisport.feature.*.model.*Ui`

**DTOs:**
- `com.nutrisport.data.dto.*`

## D) Ready `excludes` block

```kotlin
excludes {
    classes(
        // Screens & Compose generated
        "*Screen*", "*Preview*", "*ComposableSingletons*",
        // Database
        "com.nutrisport.database.dao.*", "com.nutrisport.database.entity.*",
        "com.nutrisport.database.NutriSportDatabase*", "com.nutrisport.database.converter.*",
        // DI & Navigation
        "com.nutrisport.di.*", "com.nutrisport.navigation.*",
        // Android entry points
        "com.portfolio.nutrisport.MainActivity*", "com.portfolio.nutrisport.NutrisportApplication*",
        // Shared UI resources & components
        "com.nutrisport.shared.Resources*", "com.nutrisport.shared.component.*",
        "com.nutrisport.shared.Alpha*", "com.nutrisport.shared.Colors*",
        "com.nutrisport.shared.Fonts*", "com.nutrisport.shared.Constants*",
        "*BuildConfig*",
        // NEW: Generated resources
        "nutrisport.shared.ui.generated.resources.*",
        "nutrisport.composeapp.generated.resources.*",
        // NEW: Analytics
        "com.nutrisport.analytics.*",
        // NEW: UI models (feature layer)
        "com.nutrisport.feature.*.model.*Ui",
        // NEW: DTOs
        "com.nutrisport.data.dto.*",
    )
}
```

## Suggested action

> `ProductRepositoryImpl` має найбільший gap (-25%). Рекомендую:
> `/gen-test ProductRepositoryImpl`
