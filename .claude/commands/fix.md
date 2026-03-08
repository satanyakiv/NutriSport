Read .claude/rules/architecture.md, .claude/rules/error-handling.md, .claude/rules/testing.md

## Bug

$ARGUMENTS

## Process (TDD — Red-Green-Refactor)

1. **REPRODUCE**: Find the relevant code. Trace the data flow:
   `Screen → ViewModel → UseCase → Repository → DataSource`
   Show: what's wrong, why, which layer, file paths + line numbers.
   **Wait for "go".**

2. **RED — Write a failing test first:**
   - Name: `` `should {expected} when {condition}` ``
   - AAA pattern, Mokkery for mocks, Turbine for Flows
   - Run: `./gradlew :{module}:allTests --tests "*TestClass"` — confirm FAILS

3. **GREEN — Minimal fix:**
   - Fix only the bug, nothing else
   - Respect layer boundaries: DTOs in data, domain stays pure, Ui in feature
   - If fix crosses layers — fix in the correct layer per architecture

4. **VERIFY:**
   - New test PASSES
   - All tests in affected module(s) PASS: `./gradlew :{module}:allTests`
   - Compile check: `./gradlew :{module}:compileCommonMainKotlinMetadata`

5. **UPDATE** rules if the bug revealed an undocumented gotcha:
   - Add to `.claude/rules/conventions.md` or CLAUDE.md Build Gotchas

## Rules

- Never `./gradlew test` without `--tests` filter
- Never break existing tests
- Minimal diff — don't touch what isn't broken
- If fix needs a new UseCase → follow architecture rules
- If fix needs model changes → update mappers in ALL layers
- If unsure → ask, don't guess
