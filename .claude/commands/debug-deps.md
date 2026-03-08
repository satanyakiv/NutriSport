Read .claude/rules/architecture.md, .claude/rules/conventions.md

## Dependency / Build Crash Debugger

Diagnose and fix dependency conflicts, build crashes, and plugin incompatibilities.

$ARGUMENTS

## Process

1. **IDENTIFY** the failing dependency/plugin:
   - Read the full error message and stacktrace
   - Extract: library name, version, plugin ID, Gradle task that fails
   - Identify the module and build phase (configuration, compilation, execution)

2. **SEARCH GITHUB ISSUES** — this is the most critical step:
   - Use `gh issue list --repo {org}/{repo} --search "{error keywords}" --state all --limit 10`
   - For the top relevant issues: `gh issue view {number} --repo {org}/{repo} --json body,comments,state,title`
   - Look for: workarounds, version fixes, plugin order issues, configuration changes
   - Common repos to search:
     - `Kotlin/kotlinx-kover` — coverage issues
     - `JetBrains/compose-multiplatform` — Compose KMP issues
     - `GitLiveApp/firebase-kotlin-sdk` — Firebase KMP issues
     - `InsertKoinIO/koin` — DI issues
     - `coil-kt/coil` — image loading issues
     - `ArkiveDev/Mokkery` — mocking issues
     - `cashapp/turbine` — Flow testing issues
     - `gradle/gradle` — Gradle itself
     - For AGP issues, search Android issue tracker via web

3. **CHECK VERSIONS** — use Maven deps server to verify:
   - Is the version real? `check_maven_version_exists`
   - Is there a newer version with a fix? `get_latest_release`
   - Are there incompatible version combinations?

4. **DIAGNOSE** and present findings:
   - Root cause (with link to GitHub issue if found)
   - Available workarounds from issue comments
   - Recommended fix
   **Wait for "go".**

5. **FIX** — apply the solution:
   - If version bump: update `gradle/libs.versions.toml`
   - If plugin order: fix in convention plugin or module build.gradle.kts
   - If workaround: apply minimal change, add comment with issue link
   - If incompatibility: propose alternative library or approach

6. **VERIFY**:
   - `./gradlew :{module}:tasks` — configuration succeeds
   - `./gradlew :{module}:allTests` — tests pass (if applicable)
   - `./gradlew assembleDebug` — full build succeeds

## Rules

- **ALWAYS search GitHub issues first** — most KMP build issues have known solutions
- **Check plugin application order** — many issues are timing-related (e.g., Kover #772)
- **Never blindly bump versions** — check changelogs and compatibility
- **Minimal fix** — don't refactor surrounding code while debugging
- **Document workarounds** — add comment with issue URL if applying a non-obvious fix
- When searching for KMP libraries → check https://github.com/terrakok/kmp-awesome
