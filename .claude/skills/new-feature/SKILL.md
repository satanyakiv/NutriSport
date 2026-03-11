---
name: new-feature
description: Scaffold a new feature module with all boilerplate following NutriSport Clean Architecture. Use this skill when the user wants to add a new screen, create a new feature module, or says "new feature", "add screen", "create module for X". Handles module creation, navigation registration, DI wiring, and Gradle setup.
disable-model-invocation: true
---

Create new feature module: $ARGUMENTS

Read .claude/rules/architecture.md for full rules (section "Adding a New Feature").

## Steps

1. **Choose convention plugin** based on feature needs:
   - `nutrisport.kmp.feature` — basic screen (ViewModel + Compose + Koin)
   - `nutrisport.kmp.feature.full` — needs images (Coil), deep navigation, or network (Ktor)

2. **Create module** `feature/{name}/build.gradle.kts`:
   ```kotlin
   plugins {
       id("nutrisport.kmp.feature") // or feature.full
   }
   kotlin {
       android {
           namespace = "com.nutrisport.feature.{name}"
           compileSdk = libs.versions.android.compileSdk.get().toInt()
           minSdk = libs.versions.android.minSdk.get().toInt()
           withHostTest {}
       }
       sourceSets {
           commonMain.dependencies {
               implementation(project(":domain"))
               implementation(project(":shared:utils"))
               implementation(project(":shared:ui"))
           }
       }
   }
   ```

3. **Add route** — `@Serializable` to `Screen` sealed class in `shared/utils/.../Screen.kt`

4. **Create ViewModel** — `{Name}ViewModel` in `feature/{name}/`
   - Inject repository via constructor
   - Expose `StateFlow<UiState<T>>` for async data
   - Use `viewModelScope` for coroutines

5. **Create Route** — `{Name}Route` composable in `feature/{name}/`
   - Get ViewModel via `koinViewModel<{Name}ViewModel>()`
   - Collect state: `val state by viewModel.state.collectAsStateWithLifecycle()`
   - Pass state + callbacks to Screen

6. **Create Screen** — `{Name}Screen` composable
   - Pure UI: receive state + event callbacks as parameters (no DI, no ViewModel)
   - Navigation callbacks: `goBack: () -> Unit`, `navigateTo{X}: (args) -> Unit`

7. **Create UI models** (if needed) in `feature/{name}/model/`
   - `{Entity}Ui` data class + `.toUi()` extension mapper

8. **Register in navigation** — `navigation/NavGraph.kt`
   - Add `private fun NavGraphBuilder.{name}Destination(navController: NavController)`
   - Call it from `SetupNavGraph`
   - Navigation uses `{Name}Route`, NOT `{Name}Screen` directly

9. **Register in DI** — `di/KoinModule.kt`
   - `viewModelOf(::{Name}ViewModel)`

10. **Update Gradle** — `settings.gradle.kts`
    - `include(":feature:{name}")`

11. **Skeleton test** — create `{Name}ViewModelTest` in `commonTest`
    - Setup: `StandardTestDispatcher` + `setMain`/`resetMain`
    - Create `Fake{Entity}Repository` with `MutableSharedFlow` if needed
    - Add one smoke test: verify initial state is `Loading`
    - Reference: [gen-test examples](../gen-test/examples/)

12. **Verify**: `./gradlew :feature:{name}:compileCommonMainKotlinMetadata`
