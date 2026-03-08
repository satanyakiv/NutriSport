Read .claude/rules/architecture.md, .claude/rules/models.md, .claude/rules/error-handling.md, .claude/rules/conventions.md

## Clean Architecture Check

Analyze the specified code for Clean Architecture compliance.

$ARGUMENTS

## Layer Boundaries

### Domain (`:shared`) — inner circle
- Pure Kotlin. No Android, no Firebase, no Ktor imports.
- Models: `Product`, `Customer`, `Order` — NO suffix.
- Interfaces: `ProductRepository`, `CustomerRepository`.
- UseCases: `{Verb}{Entity}UseCase` with `suspend operator fun invoke()`.
- NEVER depends on data or feature modules.

### Data (`:data`) — outer circle
- DTOs: `{Entity}Dto` — match external API/Firebase schema.
- Mappers: `{Entity}Dto.toDomain(): {Entity}` — extension functions.х
- Repository impls: `{Entity}RepositoryImpl` — implements domain interface.
- All Firebase/Ktor/external calls live HERE.
- DTOs NEVER leave this module. Always map to domain before returning.

### Presentation (`:feature/*`) — outer circle
- UI models: `{Entity}Ui` — tailored for Compose rendering.
- Mappers: `{Entity}.toUi(): {Entity}Ui` — extension functions.
- ViewModels: receive domain models from repos, map to Ui in StateFlow.
- Screens: receive `{Entity}Ui` via state, NEVER domain models directly.

## Process

1. **SCAN** the specified files/module for violations:
   - [ ] Domain model used directly in `@Composable` function
   - [ ] DTO leaking outside `:data` module
   - [ ] Firebase/Ktor import in feature module
   - [ ] Missing mapper between layers
   - [ ] Business logic in ViewModel (extract UseCase if >10 lines)
   - [ ] Repository impl in wrong module
   - [ ] Model without proper suffix (Dto/Ui) outside domain

2. **REPORT** violations with file paths and line numbers.
   Suggest fixes with correct layer placement.
   **Wait for "go".**

3. **FIX** — apply changes following architecture rules.

4. **VERIFY** — run tests, confirm zero behavior change.

## Mapper Conventions

```kotlin
// ✅ data/.../mapper/ProductMapper.kt
fun ProductDto.toDomain() = Product(
    id = id,
    title = title,
    price = price,
    category = ProductCategory.valueOf(category)
)

fun Product.toDto() = ProductDto(
    id = id,
    title = title,
    price = price,
    category = category.name
)

// ✅ feature/.../mapper/ProductMappers.kt
fun Product.toUi() = ProductUi(
    id = id,
    title = title,
    formattedPrice = "$${"%.2f".format(price)}"
)

// ✅ ViewModel usage
class ProductsViewModel(private val repo: ProductRepository) : ViewModel() {
    val state = repo.getProducts()
        .map { products -> products.map { it.toUi() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
```

## Model Placement

```
shared/src/commonMain/kotlin/com/nutrisport/shared/
  model/Product.kt              ← domain (no suffix)
  model/Customer.kt
  repository/ProductRepository.kt  ← interface only

data/src/commonMain/kotlin/com/nutrisport/data/
  dto/ProductDto.kt             ← DTO (Dto suffix)
  mapper/ProductMapper.kt       ← toDomain() / toDto()
  repository/ProductRepositoryImpl.kt

feature/details/src/commonMain/kotlin/com/nutrisport/feature/details/
  model/ProductUi.kt            ← UI model (Ui suffix)
  mapper/ProductMappers.kt      ← toUi()
  DetailsViewModel.kt
  DetailsScreen.kt
```
