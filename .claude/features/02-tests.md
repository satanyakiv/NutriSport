# 02 — Comprehensive Tests

Status: IDLE
Group: E (runs LAST — covers all new code)
Depends on: 04, 05, 06, 07, 08, 09, 10, 11, 12

## Context

Current test coverage is minimal. Need mapper tests, ViewModel tests, use case tests following AAA/Turbine/Mokkery conventions. Runs last to cover all features.

## Files to Create

### Mapper Tests
- [ ] `data/src/commonTest/.../mapper/ProductMapperTest.kt`
- [ ] `data/src/commonTest/.../mapper/CustomerMapperTest.kt`
- [ ] `data/src/commonTest/.../mapper/CartItemMapperTest.kt`
- [ ] `data/src/commonTest/.../mapper/OrderMapperTest.kt`
- [ ] `feature/home/src/commonTest/.../mapper/ProductUiMapperTest.kt`
- [ ] `feature/cart/src/commonTest/.../mapper/CartItemUiMapperTest.kt`

### ViewModel Tests
- [ ] `feature/home/src/commonTest/.../HomeViewModelTest.kt`
- [ ] `feature/cart/src/commonTest/.../CartViewModelTest.kt`
- [ ] `feature/details/src/commonTest/.../DetailsViewModelTest.kt`
- [ ] `feature/checkout/src/commonTest/.../CheckoutViewModelTest.kt`
- [ ] `feature/profile/src/commonTest/.../ProfileViewModelTest.kt`

### UseCase Tests
- [ ] `shared/utils/src/commonTest/.../usecase/CalculateCartTotalUseCaseTest.kt`
- [ ] `shared/utils/src/commonTest/.../usecase/EnrichCartWithProductsUseCaseTest.kt`
- [ ] `shared/utils/src/commonTest/.../usecase/ValidateProfileFormUseCaseTest.kt`
- [ ] `shared/utils/src/commonTest/.../usecase/SignOutUseCaseTest.kt`
- [ ] `shared/utils/src/commonTest/.../usecase/CreateOrderUseCaseTest.kt`
- [ ] `shared/utils/src/commonTest/.../usecase/ObserveEnrichedCartUseCaseTest.kt`

### Fake Data Factories
- [ ] `shared/utils/src/commonTest/.../FakeData.kt` — fakeProduct(), fakeCustomer(), etc.

## Files to Modify

- [ ] Module `build.gradle.kts` files — add test deps if missing

## Dependencies (libs)

All already in convention plugins:
- `kotlin.test`
- `turbine`
- `mokkery`
- `assertk`
- `kotlinx-coroutines-test`

## Implementation Steps

1. Create `FakeData.kt` with factory functions for all domain models
2. Write mapper tests (pure, no mocks needed)
3. Write use case tests (pure for stateless, Mokkery for repo-dependent)
4. Write ViewModel tests with Turbine + Mokkery
5. Run all tests and verify coverage

## Verification

```bash
# Run all tests
./gradlew allTests

# Check coverage
./gradlew koverHtmlReport
./gradlew koverVerify

# Specific test classes
./gradlew :shared:utils:allTests --tests "*CalculateCartTotalUseCaseTest"
./gradlew :feature:cart:allTests --tests "*CartViewModelTest"
./gradlew :data:allTests --tests "*ProductMapperTest"
```

## Conflict Zones

Test files only — no conflicts with other features. Runs last to test everything.
