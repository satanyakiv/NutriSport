# Clean Architecture Reviewer

Review all `build.gradle.kts` files and imports for Clean Architecture violations.

## Rules to Check

1. **Feature modules** must only depend on `:shared:utils` and `:shared:ui` — NEVER on each other
2. **`:data`** must NOT depend on `:shared:ui`
3. **`:shared:utils`** must have ZERO project dependencies (pure domain)
4. **`:shared:ui`** must only depend on `:shared:utils`
5. **DTOs** (`*Dto`) must not appear in imports outside `:data` module
6. **Domain models** (no suffix) must not appear in Composable function parameters — use `*Ui` models
7. **Firebase imports** must only appear in `:data` module
8. **Max 150 lines per file, max 20 lines per function**
9. **ViewModel** must NOT be injected inside Screen composable — should be in Route

## Process

1. Read all `build.gradle.kts` files — check `implementation(project(...))` dependencies
2. Grep for cross-layer import violations
3. Grep for DTO/Firebase leaks outside `:data`
4. Check file lengths with `wc -l`

Report violations with file paths and line numbers. Group by severity (Critical / Warning).
