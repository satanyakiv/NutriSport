# Navigation Rules

How navigation is structured in NutriSport. Decision rationale lives in [docs/adr/0004-compose-navigation-library.md](../../docs/adr/0004-compose-navigation-library.md). This file is the prescriptive contract.

NutriSport has **no top-level bottom tabs**, so there is no multi-back-stack / `TabRoot` / `switchTab` machinery. The single bottom bar inside `HomeGraph` is local tab state owned by a nested `HomeNavHost` and does **not** go through the `Router` (see "HomeGraph internal nav" below).

## Stack

| Piece                                                        | Where                                                     | Role                                                                                                           |
| ------------------------------------------------------------ | --------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| `org.jetbrains.androidx.navigation:navigation-compose:2.9.2` | `gradle/libs.versions.toml` (`navigation`)                | Compose Navigation (KMP fork). Source of `NavHost`, `NavController`, typed `composable<T>`.                    |
| `Screen` sealed class                                        | `:shared:utils` `shared/navigation/Screen.kt`             | Type-safe destinations. `@Serializable`.                                                                       |
| `NavigationCommand` sealed class                             | `:domain` `shared/domain/navigation/NavigationCommand.kt` | What ViewModels emit when they want to navigate. `NavigateTo`, `Replace`, `Back`, `PopUpTo`, `PopToRoot`.      |
| `Router` interface                                           | `:domain` `shared/domain/navigation/Router.kt`            | Pure-Kotlin contract. Injected into ViewModels via Koin. `awaitReady()` supports cold-start deeplink delivery. |
| `DefaultRouter`                                              | `:navigation` `navigation/DefaultRouter.kt`               | Single Koin singleton. Owns a `MutableSharedFlow<NavigationCommand>` (`replay=0`, `extraBufferCapacity=16`).   |
| `SetupNavGraph`                                              | `:navigation` `navigation/NavGraph.kt`                    | Top-level Composable. Bridges `Router.commands` to the live `NavController` via `LaunchedEffect`.              |
| `FakeRouter`                                                 | `:shared:testing` `shared/test/FakeRouter.kt`             | Records emitted commands into `recordedCommands`. For ViewModel unit tests.                                    |

`Router` and `NavigationCommand` reference `Screen` (from `:shared:utils`). Any module that implements or asserts on the `Router` (e.g. `:shared:testing`) must depend on `:shared:utils` in addition to `:domain`, because `:domain` pulls `:shared:utils` via `implementation` (not `api`).

## State Hoisting (mandatory)

Every feature destination follows the same skeleton:

1. **`XxxViewModel`** — exposes UI state plus navigation methods. Injects use cases and `Router` via Koin constructor. Navigation methods are thin: `fun goBack() = router.back()`, `fun navigateToDetails(id: String) = router.navigateTo(Screen.Details(id))`.
2. **`XxxRoute` (Composable)** — stateful container. The only place a feature module touches DI:
   ```kotlin
   @Composable
   fun XxxRoute() {
       val viewModel = koinViewModel<XxxViewModel>()
       XxxScreen(
           goBack = viewModel::goBack,
           // ... state + other callbacks
       )
   }
   ```
   When a destination carries route args (`Screen.Checkout(totalAmount)`), `SetupNavGraph` reads them via `toRoute<...>()` and passes them as plain parameters into the Route (`XxxRoute(totalAmount = ...)`). The ViewModel reads the same args from `SavedStateHandle`.
3. **`XxxScreen` (Composable, stateless)** — pure function of state + callbacks. Receives navigation callbacks (`goBack`, `navigateToDetails`, ...) wired from `viewModel::method`. Never touches `Router`, `koinViewModel`, or `NavController`.

### Forbidden

- Passing `NavController` into a feature module.
- Passing `Router` into a Composable. `Router` is for ViewModels only.
- A Route that takes navigation lambdas (`goBack: () -> Unit`) from the graph. Navigation lambdas are wired from `viewModel::method`, not from `SetupNavGraph`.
- Putting `viewModelScope.launch` inside a Composable.

## Router contract

```kotlin
// :domain
sealed class NavigationCommand {
    data class NavigateTo(val destination: Screen) : NavigationCommand()
    data class Replace(val destination: Screen) : NavigationCommand()
    data object Back : NavigationCommand()
    data class PopUpTo(val destination: Screen, val inclusive: Boolean = false) : NavigationCommand()
    data object PopToRoot : NavigationCommand()
}

interface Router {
    val commands: SharedFlow<NavigationCommand>
    suspend fun awaitReady()
    fun navigateTo(destination: Screen)
    fun replaceWith(destination: Screen)   // go to destination, clear the entire back stack
    fun back()
    fun popUpTo(destination: Screen, inclusive: Boolean = false)
    fun popToRoot()
}
```

`DefaultRouter` is registered as a `single<Router>` in `:di`. Inject it into ViewModels by constructor:

```kotlin
class AdminPanelViewModel(
    private val adminRepository: AdminRepository,
    private val router: Router,
) : ViewModel() {
    fun goBack() = router.back()
    fun goToManageProduct(id: String?) = router.navigateTo(Screen.ManageProduct(id))
}
```

Because every feature DI module registers its ViewModel via `viewModelOf(::XxxViewModel)`, adding a `Router` constructor parameter is auto-resolved by Koin once `single<Router> { DefaultRouter() }` is registered. No DI module edit is needed per feature.

### Command → NavController mapping (the collector)

`SetupNavGraph` collects `router.commands` in a `LaunchedEffect(navController, router)` and translates each command:

| Command                    | NavController call                                                           |
| -------------------------- | ---------------------------------------------------------------------------- |
| `NavigateTo(dest)`         | `navigate(dest)`                                                             |
| `Replace(dest)`            | `navigate(dest) { launchSingleTop = true; popUpTo(0) { inclusive = true } }` |
| `Back`                     | `popBackStack()`                                                             |
| `PopUpTo(dest, inclusive)` | `popBackStack(route = dest, inclusive = inclusive)`                          |
| `PopToRoot`                | `popBackStack(graph.findStartDestination().id, inclusive = false)`           |

`Replace` clears the **entire** back stack (`popUpTo(0)`), so login (`Auth → HomeGraph`), logout (`HomeGraph → Auth`), and "back to home after payment" all map to `replaceWith(...)` and produce a single-entry stack regardless of the dynamic start destination.

## Args via SavedStateHandle

`Router` handles **outbound** navigation (leaving the screen). `SavedStateHandle` handles **inbound** args (entering the screen). Never mix.

```kotlin
class CheckoutViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val router: Router,
) : ViewModel() {
    private val totalAmount = savedStateHandle.get<Double>("totalAmount").orZero()
    fun navigateBack() = router.back()
}
```

## Transitions

Destinations use plain `composable<T>` inside a single `NavHost`. Transitions are disabled at the `NavHost` level (`enterTransition = { EnterTransition.None }`, and the matching exit / popEnter / popExit). There is no per-destination transition wrapper. The `NavHostController` comes from `koinInject<DebugToolkit>().rememberNavController()` so debug builds can wrap it for inspection.

## HomeGraph internal nav (NOT via Router)

`HomeGraph` hosts its own bottom bar (ProductsOverview / Cart / Categories) through a **nested** `HomeNavHost` (`feature/home/component/HomeNavHost.kt`) with its own `rememberNavController()`. Switching bottom-bar tabs is local UI state and stays inside that nested host (`navController.navigate(destination.screen) { launchSingleTop = true; popUpTo<Screen.ProductsOverview> { saveState = true }; restoreState = true }`). Only **outbound** navigation that leaves `HomeGraph` (to `Details`, `CategorySearch`, `Checkout`, `Profile`, `AdminPanel`, or `Auth` on logout) goes through the `Router` via `HomeGraphViewModel`. Do not route the bottom-bar switching through the `Router`.

## Tests

ViewModels depend on `Router` (`:domain`). In unit tests use `FakeRouter` from `:shared:testing` — it records every emitted `NavigationCommand` into `recordedCommands`. Never instantiate `DefaultRouter` or a real `NavController` in a unit test.

```kotlin
val router = FakeRouter()
val viewModel = AuthViewModel(customerRepository = fakeRepo, router = router)
viewModel.goToHome()
assertThat(router.recordedCommands).containsExactly(
    NavigationCommand.Replace(Screen.HomeGraph),
)
```

## Adding a new feature destination — checklist

1. Add the route to `Screen` (or a nested sealed sub-class) with `@Serializable`.
2. Create `XxxViewModel` exposing state + navigation methods. Inject `Router`.
3. Create `XxxRoute` (Composable container) and `XxxScreen` (stateless). Screen receives nav callbacks wired from `viewModel::method`.
4. Register the ViewModel via `viewModelOf(::XxxViewModel)` in the feature's Koin module.
5. Wire the destination in `NavGraph.kt` via `composable<Screen.Xxx> { XxxRoute() }` (read route args with `toRoute<...>()` and pass them as parameters when present).
6. Tests: `XxxViewModelTest` with `FakeRouter` from `:shared:testing`.

## Compile gate

After a navigation change, compile everything for Android with the KMP Android task:

```bash
./gradlew :composeApp:compileAndroidMain
```

(NutriSport uses the Android KMP library plugin, so the task is `compileAndroidMain`, not `compileDebugKotlinAndroid`.)

## Related

- [docs/adr/0004-compose-navigation-library.md](../../docs/adr/0004-compose-navigation-library.md) — decision record
- [architecture.md](architecture.md) — module dependency graph
- [conventions.md](conventions.md) — Kotlin / Compose / Koin conventions
- [testing.md](testing.md) — unit / composition / E2E test pyramid; `FakeRouter` usage
- [error-handling.md](error-handling.md) — `Either`, `DomainResult`, `UiState`
- [preview.md](preview.md) — Route vs Screen separation (why Routes are not previewable)
