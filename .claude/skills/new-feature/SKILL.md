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
               implementation(project(":shared:utils"))
               implementation(project(":shared:ui"))
           }
       }
   }
   ```

3. **Add route** — `@Serializable` to `Screen` sealed class in `shared/utils/.../navigation/Screen.kt`

4. **Create ViewModel** — `{Name}ViewModel` in `feature/{name}/`
   - Inject repository via constructor
   - Expose `StateFlow<UiState<T>>` for async data
   - Use `viewModelScope` for coroutines

5. **Create Screen** — `{Name}Screen` composable
   - State hoisting: receive state + event callbacks as parameters
   - Navigation callbacks: `goBack: () -> Unit`, `navigateTo{X}: (args) -> Unit`

6. **Create UI models** (if needed) in `feature/{name}/model/`
   - `{Entity}Ui` data class + `.toUi()` extension mapper

7. **Register in navigation** — `navigation/NavGraph.kt`
   - Add `private fun NavGraphBuilder.{name}Destination(navController: NavController)`
   - Call it from `SetupNavGraph`

8. **Register in DI** — `di/KoinModule.kt`
   - `viewModelOf(::{Name}ViewModel)`

9. **Update Gradle** — `settings.gradle.kts`
   - `include(":feature:{name}")`

10. **Verify**: `./gradlew :feature:{name}:compileCommonMainKotlinMetadata`
