# Architecture Rules

## Project Structure

NutriSport is a KMP (Kotlin Multiplatform) project targeting Android + iOS.

### Module Hierarchy

```
androidApp          — Android entry point (Activity, Application)
composeApp          — Shared KMP UI entry (AppContent)
navigation          — NavHost + type-safe routing (SetupNavGraph)
shared              — Domain layer: models, interfaces, use cases, utilities
data                — Data layer: Firebase repos, DTOs, mappers to domain
di                  — Koin DI configuration (sharedModule + platform modules)
analytics           — Abstract analytics tracking (CompositeTracker)
feature/*           — Presentation layer: ViewModels, Screens, UI models
build-logic/        — Convention plugins (library, feature, feature.full)
```

### Dependency Flow (top → bottom, never upward)

```
androidApp / iosApp
  └─ composeApp
       ├─ navigation (depends on all feature modules + shared)
       ├─ di (depends on all feature modules + data + analytics)
       ├─ shared (domain layer — no external deps)
       └─ data (depends on shared only)

feature modules → shared (never depend on each other)
```

## Clean Architecture Layers

### 1. Domain (`:shared`)
Pure business logic. No platform deps, no Firebase, no network.

- **Domain models:** `Product`, `Customer`, `Order`, `CartItem`
- **Repository interfaces:** `ProductRepository`, `CustomerRepository`
- **Use cases:** `CalculateCartTotalUseCase`, `ValidateProfileFormUseCase`, `CreateOrderUseCase`
- **Value objects & enums:** `ProductCategory`, `RequestState<T>`

### 2. Data (`:data`)
External I/O. Firebase, APIs, local storage.

- **DTOs:** suffixed `Dto` — `ProductDto`, `CustomerDto`, `OrderDto`
- **Mappers:** suffixed `Mapper` — `ProductMapper`, `CustomerMapper`
- **Repository implementations:** `ProductRepositoryImpl`, `CustomerRepositoryImpl`

### 3. Presentation (`:feature/*`)
UI + ViewModels. Each feature module = one screen (or a small group).

- **UI models:** suffixed `Ui` — `ProductUi`, `CartItemUi`
- **Mappers:** `toUi()` extension functions inside ViewModel or dedicated mapper
- **ViewModels:** `CartViewModel`, `ProfileViewModel`
- **Screens:** `CartScreen`, `ProfileScreen`

## Model Naming & Mapping

| Layer        | Suffix   | Example          | Location                        |
|-------------|----------|------------------|---------------------------------|
| Domain      | (none)   | `Product`        | `shared/.../model/Product.kt`   |
| Data (DTO)  | `Dto`    | `ProductDto`     | `data/.../dto/ProductDto.kt`    |
| Data Mapper | `Mapper` | `ProductMapper`  | `data/.../mapper/ProductMapper.kt` |
| UI Model    | `Ui`     | `ProductUi`      | `feature/.../model/ProductUi.kt` |
| UI Mapper   | `toUi()` | `Product.toUi()` | `feature/.../mapper/ProductMappers.kt` |

### Mapper Rules

1. **Data mappers** live in `:data` — class with `map()` method, injected into repositories.
2. **UI mappers** live in feature modules — convert domain → `Ui` model.
3. Domain models NEVER know about DTOs or UI models.
4. Mappers are pure (no side effects), testable as standalone classes.
5. **Data mapper pattern:** `{Entity}Mapper` class with `fun map(source): Target`.
6. **UI mapper pattern:** `Product.toUi()` extension functions.

```kotlin
// data/.../ProductMapper.kt
class ProductMapper {
    fun map(document: DocumentSnapshot): Product = Product(
        id = document.id, title = document.get("title"), ...
    )
}

// feature/.../mapper/ProductMappers.kt
fun Product.toUi() = ProductUi(
    id = id, title = title,
    formattedPrice = "$${"%.2f".format(price)}"
)
```

## Convention Plugins

| Plugin                        | Applies                                            |
|-------------------------------|----------------------------------------------------|
| `nutrisport.kmp.library`      | KMP + Compose + Mokkery + Kover (iOS, jvmToolchain 21) |
| `nutrisport.kmp.feature`      | library + Koin Compose + messagebar                |
| `nutrisport.kmp.feature.full` | feature + Coil + compose-navigation + Ktor clients |

## Rules

1. **No circular deps.** Feature modules NEVER depend on each other.
2. **shared is pure domain.** No platform code, no Firebase, no network.
3. **data owns all external I/O.** Firebase, APIs, file storage — only here.
4. **di module** is the only place that wires repositories and ViewModels.
5. **navigation module** is the only module that knows about all features.
6. **ViewModels** live in feature modules. One ViewModel per screen (max).
7. **Max 150 lines/file, max 20 lines/function.** No god classes.
8. **RequestState<T>** is the standard async wrapper. Use it consistently.
9. **Screen sealed class** (in shared) defines all navigation destinations.
10. **Convention plugins** — always use them. Never duplicate plugin config.
11. **DTOs never leak** outside `:data`. Map to domain before returning.
12. **Domain models never leak** into Composables. Map to UI models.

## Use Cases

Use cases live in `shared/src/commonMain/kotlin/com/nutrisport/shared/domain/usecase/`.

### Pattern
```kotlin
class CalculateCartTotalUseCase {
    operator fun invoke(cartItems: List<CartItem>, products: List<Product>): Double {
        return cartItems.sumOf { cartItem ->
            val product = products.find { it.id == cartItem.productId }
            product?.price?.times(cartItem.quantity) ?: 0.0
        }
    }
}
```

### Rules
1. **One public method** — `operator fun invoke(...)`.
2. **Pure use cases** (no deps) — instantiate directly or via `factory {}` in Koin.
3. **Use cases with repo deps** — inject via constructor, register `factory { UseCase(get()) }`.
4. **Use cases can compose other use cases** — inject nested use cases via constructor.
5. **Tests** — pure unit tests in `shared/src/commonTest/.../usecase/`.
6. **Naming** — `{Action}{Entity}UseCase` (e.g., `CalculateCartTotalUseCase`).

### Existing Use Cases
| UseCase | Dependencies | Purpose |
|---------|-------------|---------|
| `CalculateCartTotalUseCase` | none | Cart total from items + products |
| `EnrichCartWithProductsUseCase` | none | Pair cart items with products |
| `ValidateProfileFormUseCase` | none | Validate profile/checkout form |
| `SignOutUseCase` | `CustomerRepository` | Sign out user |
| `CreateOrderUseCase` | `OrderRepository` | Create order |
| `UpdateCustomerUseCase` | `CustomerRepository` | Update customer profile |
| `ObserveEnrichedCartUseCase` | `CustomerRepository`, `ProductRepository`, `EnrichCartWithProductsUseCase` | Reactive flow: customer → products → enriched cart |

## Naming

- Modules: lowercase, Gradle path nesting (`:feature:home:cart`)
- ViewModels: `{Feature}ViewModel`
- Repositories: `{Entity}Repository` (interface) + `{Entity}RepositoryImpl`
- Screens: `{Feature}Screen`
- DTOs: `{Entity}Dto`
- UI models: `{Entity}Ui`
- Mappers: `{Entity}Mapper` or `.toDomain()` / `.toUi()` extensions

## Adding a New Feature

1. Create module under `feature/` with appropriate convention plugin
2. Add `@Serializable` route to `Screen` sealed class in `shared`
3. Create UI models in `feature/.../model/` if needed
4. Create ViewModel with mappers in `feature/.../`
5. Add composable destination in `navigation/NavGraph.kt`
6. Register ViewModel in `di/KoinModule.kt`
7. Add module to `settings.gradle.kts`
