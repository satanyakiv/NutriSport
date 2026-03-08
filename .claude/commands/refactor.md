Read .claude/rules/architecture.md, .claude/rules/testing.md, .claude/rules/conventions.md

## Project context

NutriSport KMP (Android + iOS). Compose Multiplatform.
Layers: feature (presentation) → shared (domain) → data.
DI in `:di` (Koin). Navigation in `:navigation`.
Models: Domain (no suffix) → Dto (data) → Ui (feature).
Mappers: `.toDomain()` in data, `.toUi()` in feature.

## Refactor

$ARGUMENTS

## Process

1. **AUDIT**: Read all affected files. List violations against architecture rules.
   Show me:
   - Files to change (with line numbers)
   - What moves where (which layer/module)
   - What gets extracted/merged/renamed
   - Model boundaries: any Dto leaking? Domain model in UI? Missing mappers?
   **Wait for my "go".**

2. **TEST FIRST (if tests don't exist yet)**:
   - Write tests that capture CURRENT behavior before refactoring
   - Use AAA, Turbine for Flows, Mokkery for mocks
   - Run: `./gradlew :{module}:allTests --tests "*TestClass"` — must PASS
   - These tests are the safety net — they must still pass after refactor

3. **REFACTOR**: Apply changes. Zero behavior changes.
   - Move models to correct layers with proper suffixes (Dto/Ui)
   - Extract/update mappers (`.toDomain()`, `.toUi()`)
   - Keep ViewModel logic in feature, data logic in data, domain pure

4. **VERIFY**: Run all related tests:
   `./gradlew :{module}:allTests`
   All must pass. If any fail — the refactor broke something, revert and retry.

5. **CHECKLIST**: Show post-refactor state:
   - [ ] No file > 150 lines
   - [ ] No function > 20 lines
   - [ ] No Dto models outside `:data`
   - [ ] No domain models directly in Composables (use Ui models)
   - [ ] No Firebase/network imports in feature modules
   - [ ] No duplicate models across layers
   - [ ] All mappers tested
   - [ ] Convention plugins used (no manual plugin config)
   - [ ] Koin registration in `:di` only

## Rules
- **ZERO behavior changes.** Same inputs → same outputs.
- **NEVER** run `./gradlew test` without `--tests` filter.
- **NEVER** break existing tests.
- Respect Clean Architecture layer boundaries.
- If unsure about a change — ask, don't guess.
