# 01 — CI/CD Pipeline

Status: IMPLEMENTED
Group: A
Depends on: none

## Context

No CI/CD exists. For a portfolio project, GitHub Actions pipeline demonstrates professional DevOps practice. Should run on PRs and main pushes.

## Files to Create

- [ ] `.github/workflows/ci.yml` — main CI workflow
- [ ] `.github/workflows/release.yml` — release workflow (tag-triggered)

## Files to Modify

- [ ] `CLAUDE.md` — add CI commands section

## Dependencies (libs)

None — GitHub Actions only.

## Implementation Steps

1. Create `.github/workflows/ci.yml`:
   - Trigger: `push` to main, `pull_request` to main
   - Jobs:
     - **lint**: `./gradlew detekt`
     - **compile**: `./gradlew compileCommonMainKotlinMetadata`
     - **test**: `./gradlew allTests` (commonTest on JVM)
     - **coverage**: `./gradlew koverVerify`
     - **build-android**: `./gradlew assembleDebug`
   - Cache: Gradle wrapper + dependencies
   - JDK 21 setup via `actions/setup-java@v4`
   - Gradle setup via `gradle/actions/setup-gradle@v4`

2. Create `.github/workflows/release.yml`:
   - Trigger: `push` tags `v*`
   - Job: `assembleRelease` + artifact upload

3. Update `CLAUDE.md` with CI badge and notes

## Verification

```bash
# Validate workflow syntax
cat .github/workflows/ci.yml | head -5  # should start with 'name:'
# Dry-run (local act if available, otherwise just syntax check)
```

## Conflict Zones

None — only touches `.github/` directory.
