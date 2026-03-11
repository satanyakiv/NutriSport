Read .claude/rules/architecture.md, .claude/rules/models.md, .claude/rules/testing.md, .claude/rules/conventions.md

## Refactor

$ARGUMENTS

## Process

1. **AUDIT**: Read all affected files. List violations against architecture rules.
   Show:
   - Files to change (with line numbers)
   - What moves where (which layer/module)
   - What gets extracted/merged/renamed
   - Model boundaries: any Dto leaking? Domain model in UI? Missing mappers?
   **Wait for "go".**

2. **TEST FIRST** (if tests don't exist yet):
   - Write tests capturing CURRENT behavior before refactoring
   - These are the safety net — must still pass after refactor
   - Run: `./gradlew :{module}:allTests --tests "*TestClass"` — must PASS

3. **REFACTOR**: Apply changes. Zero behavior changes.
   - Move models to correct layers with proper suffixes (Dto/Ui)
   - Extract/update mappers (`.toDomain()`, `.toUi()`)
   - Keep ViewModel logic in feature, data logic in network, domain pure
   - Extract UseCase if ViewModel business logic > 10 lines

4. **VERIFY**:
   - `./gradlew :{module}:allTests` — all pass
   - `./gradlew :{module}:compileCommonMainKotlinMetadata` — compiles

5. **CHECKLIST**:
   - [ ] No file > 150 lines, no function > 20 lines
   - [ ] No Dto outside `:network`, no domain models in Composables
   - [ ] No Firebase/network imports in feature modules
   - [ ] All mappers tested
   - [ ] Convention plugins used (no manual plugin config)
   - [ ] Koin registration in `:di` only
   - [ ] Screen uses Route-Screen separation (ViewModel not injected in Screen)

6. **UPDATE** `.claude/rules/` files if architecture changed

## Rules
- **ZERO behavior changes.** Same inputs → same outputs.
- Never `./gradlew test` without `--tests` filter
- Never break existing tests
- If unsure → ask, don't guess
