# Models & Use Cases

## Model Naming & Mapping

| Layer       | Suffix         | Example            | Location                               |
|-------------|----------------|--------------------|----------------------------------------|
| Domain      | (none)         | `Product`          | `shared/utils/.../domain/Product.kt`   |
| Data (DTO)  | `Dto`          | `ProductDto`       | `data/.../dto/ProductDto.kt`           |
| Data Mapper | `Mapper`       | `ProductMapper`    | `data/.../mapper/ProductMapper.kt`     |
| UI Model    | `Ui`           | `ProductUi`        | `feature/.../model/ProductUi.kt`       |
| UI Mapper   | `ToUiMapper`   | `ProductToUiMapper` | `feature/.../mapper/ProductToUiMapper.kt` |
| Error       | `AppError`     | `AppError.Network` | `shared/utils/.../util/AppError.kt`    |
| Result      | `DomainResult` | `DomainResult<T>`  | `shared/utils/.../util/AppError.kt`    |
| UI State    | `UiState`      | `UiState<T>`       | `shared/ui/.../util/UiState.kt`        |

### Mapper Rules

1. **Data mappers** live in `:data` — class with `map()` method, injected into repositories.
2. **UI mappers** live in feature modules — class with `map()` method, injected into ViewModels.
3. Domain models NEVER know about DTOs or UI models.
4. Mappers are pure (no side effects), testable as standalone classes.
5. **Data mapper pattern:** `{Source}To{Target}Mapper` class with `fun map(source): Target`.
6. **UI mapper pattern:** `{Domain}ToUiMapper` class with `fun map(domain): {Domain}Ui`. Same pattern as data mappers.
7. **UI-dependent enum extensions** live in `:shared:ui` — `Country.flag`, `ProductCategory.color`.
8. **Domain→UI mapping in ViewModel only.** `mapper.map()` calls happen in ViewModel (inside `.map {}` on flows or in action methods), never in Composable functions. Screens receive `UiState<XxxUi>`, not `UiState<DomainModel>`.
9. **Mapper DI:** All mappers registered as `factory {}` in Koin, injected via constructor.

## Use Cases

Path: `shared/utils/src/commonMain/kotlin/com/nutrisport/shared/domain/usecase/`

### Pattern

```kotlin
// Pure use case (no deps)
class CalculateCartTotalUseCase {
    operator fun invoke(cartItems: List<CartItem>, products: List<Product>): Double {
        return cartItems.sumOf { cartItem ->
            val product = products.find { it.id == cartItem.productId }
            product?.price?.times(cartItem.quantity).orZero()
        }
    }
}

// Use case with repo dependency
class SignOutUseCase(private val customerRepository: CustomerRepository) {
    suspend operator fun invoke(): DomainResult<Unit> {
        return customerRepository.signOut()
    }
}
```

### Rules

1. **One public method** — `operator fun invoke(...)`.
2. **Pure use cases** (no deps) — instantiate directly or via `factory {}` in Koin.
3. **Use cases with repo deps** — inject via constructor, register `factory { UseCase(get()) }`.
4. **Use cases can compose other use cases** — inject nested use cases via constructor.
5. **Tests** — pure unit tests in `shared/utils/src/commonTest/.../usecase/`.
6. **Naming** — `{Action}{Entity}UseCase` (e.g., `CalculateCartTotalUseCase`).
7. **Return DomainResult** for fallible operations, raw value for pure computations.

### Existing Use Cases

| UseCase                         | Dependencies                                   | Returns                         | Purpose                        |
|---------------------------------|------------------------------------------------|---------------------------------|--------------------------------|
| `CalculateCartTotalUseCase`     | none                                           | `Double`                        | Cart total                     |
| `EnrichCartWithProductsUseCase` | none                                           | `List<Pair<CartItem, Product>>` | Pair cart items with products  |
| `ValidateProfileFormUseCase`    | none                                           | `Boolean`                       | Validate profile/checkout form |
| `SignOutUseCase`                | `CustomerRepository`                           | `DomainResult<Unit>`            | Sign out user                  |
| `CreateOrderUseCase`            | `OrderRepository`                              | `DomainResult<Unit>`            | Create order                   |
| `UpdateCustomerUseCase`         | `CustomerRepository`                           | `DomainResult<Unit>`            | Update customer profile        |
| `ObserveEnrichedCartUseCase`    | `CustomerRepo`, `ProductRepo`, `EnrichUseCase` | `Flow<DomainResult<...>>`       | Enriched cart flow             |
