Read .claude/rules/architecture.md, .claude/rules/conventions.md

## Dependency / Build Crash Debugger

$ARGUMENTS

## Process

1. **IDENTIFY** the failing dependency/plugin:
   - Read the full error message and stacktrace
   - Extract: library name, version, plugin ID, Gradle task that fails
   - Identify the module and build phase (configuration, compilation, execution)

2. **CHECK OFFICIAL DOCUMENTATION** — before any fix attempt:
   - Search for "set up {subject} for KMP" on the official docs site
   - Use `WebFetch` to read the relevant documentation page
   - Compare current setup with the official recommended pattern
   - Key docs:
     - Room KMP: https://developer.android.com/kotlin/multiplatform/room
     - Compose: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-getting-started.html
     - Kotlin: https://kotlinlang.org/docs/multiplatform.html
     - Navigation: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation-routing.html

3. **SEARCH GITHUB ISSUES**:
   - `gh issue list --repo {org}/{repo} --search "{error keywords}" --state all --limit 10`
   - `gh issue view {number} --repo {org}/{repo} --json body,comments,state,title`
   - Common repos:
     - `JetBrains/compose-multiplatform` — CMP issues
     - `Kotlin/kotlinx-kover` — coverage
     - `GitLiveApp/firebase-kotlin-sdk` — Firebase KMP
     - `InsertKoinIO/koin` — DI
     - `coil-kt/coil` — image loading
     - `ArkiveDev/Mokkery` — mocking
     - `cashapp/turbine` — Flow testing
     - `gradle/gradle` — Gradle itself
     - `google/ksp` — KSP issues

4. **CHECK VERSIONS** — use Maven deps server:
   - `check_maven_version_exists` — is the version real?
   - `get_latest_release` — is there a newer version with a fix?
   - Cross-reference with `gradle/libs.versions.toml`

5. **DIAGNOSE** and present findings:
   - Root cause (with GitHub issue link if found)
   - Official docs reference if pattern changed
   - Available workarounds
   - Recommended fix
   **Wait for "go".**

6. **FIX**:
   - Version bump → `gradle/libs.versions.toml`
   - Plugin order → convention plugin or module `build.gradle.kts`
   - Workaround → minimal change + comment with issue URL
   - Incompatibility → propose alternative (check https://github.com/terrakok/kmp-awesome)

7. **VERIFY**:
   - `./gradlew :{module}:compileCommonMainKotlinMetadata` — compiles
   - `./gradlew :{module}:allTests` — tests pass (if applicable)
   - `./gradlew assembleDebug` — full build succeeds

8. **UPDATE** memory and rules:
   - Add gotcha to CLAUDE.md Build Gotchas if pattern is non-obvious
   - Update `.claude/rules/conventions.md` if build config changed

## Rules

- **ALWAYS check official docs first** — many issues are outdated patterns
- **Then search GitHub issues** — most KMP build issues have known solutions
- **Check plugin application order** — many issues are timing-related
- **Never blindly bump versions** — check changelogs and compatibility
- **Minimal fix** — don't refactor while debugging
- **Document workarounds** — comment with issue URL for non-obvious fixes
