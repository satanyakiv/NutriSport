Read .claude/rules/architecture.md, .claude/rules/models.md, .claude/rules/error-handling.md, .claude/rules/conventions.md

## Clean Architecture Check

$ARGUMENTS

## Violation Checklist

Scan the specified module/files for these violations:

### Critical (must fix)
- [ ] **DTO leak**: `*Dto` import outside `:network` module
- [ ] **Firebase leak**: `dev.gitlive.firebase.*` import outside `:network`
- [ ] **Domain in UI**: Domain model used directly in `@Composable` parameters
- [ ] **Circular dep**: Feature module depends on another feature module
- [ ] **:domain impurity**: Platform/network/Firebase code in `:domain`

### Warning (should fix)
- [ ] **Missing mapper**: Direct field access across layers instead of mapper
- [ ] **Fat ViewModel**: Business logic > 10 lines (extract UseCase)
- [ ] **God class**: File > 150 lines or function > 20 lines
- [ ] **Wrong module**: Repository impl not in `:network`, ViewModel not in `:feature`
- [ ] **Missing suffix**: Model outside domain without Dto/Ui suffix
- [ ] **ViewModel in Screen**: ViewModel injected inside Screen composable (should be in Route)

## Process

1. **SCAN** — grep for violations using the checklist above. Report with file paths and line numbers.
   **Wait for "go".**

2. **FIX** — apply changes following architecture rules

3. **VERIFY** — run tests, compile check, confirm zero behavior change

## Model Placement Reference

```
domain/.../domain/           → Product, Customer (no suffix)
network/.../dto/             → ProductDto (Dto suffix)
network/.../mapper/          → ProductMapper (toDomain/toDto)
feature/.../model/           → ProductUi (Ui suffix)
feature/.../mapper/          → ProductMappers (toUi)
```

## Mapper Pattern

```kotlin
// data layer: DTO → Domain
fun ProductDto.toDomain() = Product(id = id, title = title, price = price)

// feature layer: Domain → UI
fun Product.toUi() = ProductUi(id = id, title = title, formattedPrice = "$${"%.2f".format(price)}")
```
