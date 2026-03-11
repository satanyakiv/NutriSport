# Error Handling

## Domain/Data Layer

All error types live in `:domain` module.

```kotlin
// Either — generic result type (domain/.../util/Either.kt)
sealed class Either<out L, out R> { Left(value: L), Right(value: R) }

// AppError — typed domain errors (domain/.../util/AppError.kt)
sealed class AppError(message: String) {
    Network, NotFound, Unauthorized, Unknown
}

// DomainResult — standard alias (domain/.../util/AppError.kt)
typealias DomainResult<T> = Either<AppError, T>
```

## Repository API

```kotlin
// Repositories return DomainResult (no callbacks)
interface CustomerRepository {
    suspend fun updateCustomer(customer: Customer): DomainResult<Unit>
    fun readCustomerFlow(): Flow<DomainResult<Customer>>
}
```

## Presentation Layer

```kotlin
// UiState wraps DomainResult for UI consumption
sealed class UiState<out T> {
    Idle, Loading, Content(result: DomainResult<T>)
}

// ViewModel wraps repo flows into UiState
val customer = customerRepository.readCustomerFlow()
    .map { UiState.Content(it) }
    .onStart { emit(UiState.Loading) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.Loading)

// One-shot operations: fold the result
val result = repository.updateCustomer(customer)
result.fold(
    ifLeft = { error -> showError(error.message) },
    ifRight = { onSuccess() },
)
```

## Rules

- **Domain/Data:** `DomainResult<T>` (`Either<AppError, T>`) — type-safe errors
- **Presentation:** `UiState<T>` — `Idle`, `Loading`, `Content(DomainResult<T>)`
- Use `Either.fold()` for branching on success/error
- Never swallow exceptions silently
- Log errors before wrapping into `Either.Left(AppError.*)`
- **No callbacks** (`onSuccess`/`onError`) in repository interfaces — return `DomainResult`
