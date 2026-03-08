Read .claude/rules/architecture.md, .claude/rules/error-handling.md, .claude/rules/testing.md

## Project context

NutriSport KMP (Android + iOS). Compose Multiplatform.
Layers: feature (presentation) → shared (domain) → data.
DI in `:di` (Koin). Navigation in `:navigation`.
Models: Domain (no suffix) → Dto (data) → Ui (feature).

## Bug

$ARGUMENTS

## Process (TDD — Red-Green-Refactor)

1. **REPRODUCE**: Find the relevant code. Trace the data flow through layers:
   `Screen → ViewModel → Repository → Data Source`
   Explain: what's wrong, why it happens, which layer is affected.
   Show me the broken flow with file paths and line numbers.
   **Wait for my "go".**

2. **RED — Write a failing test first:**
   - Test name: `should {expected behavior} when {condition}`
   - Use AAA pattern (Arrange-Act-Assert)
   - Mock dependencies (Mokkery), test Flows (Turbine)
   - Run: `./gradlew :{module}:allTests --tests "*TestClass"` — confirm it FAILS

3. **GREEN — Minimal fix:**
   - Fix only the bug. Do not refactor surrounding code.
   - If the bug is in a mapper — fix the mapper, test the mapper.
   - If the bug crosses layers — fix in the correct layer per architecture.
   - Ensure DTOs don't leak, domain stays pure, UI models stay in feature.

4. **VERIFY:**
   - Run the new test — must PASS
   - Run all tests in affected module(s) — must PASS
   - `./gradlew :{module}:allTests`
   - Show: root cause, what changed, which layer was affected

## Rules

- **NEVER** run `./gradlew test` without `--tests` filter
- **NEVER** break existing tests
- **Minimal diff.** Don't touch what isn't broken.
- If fix requires a new UseCase — follow architecture rules
- If fix requires model changes — update mappers in ALL layers
- Respect model boundaries: Dto stays in data, Ui stays in feature
- If unsure about a change — ask, don't guess
