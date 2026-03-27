# CI/CD

NutriSport uses GitHub Actions for continuous integration and delivery. There are three workflows for Android and one for iOS.

## Workflows

### Pull Request (`pr.yml`)

Runs on every PR to `main`. Validates code quality before merge.

```
trigger: pull_request → main

                  ┌──────────────────────────────────────────────────────┐
                  │                    actionlint                        │
                  │          (only when .github/** changed)              │
                  └──────────┬───────────────────────────┬───────────────┘
                             │                           │
               Android (ubuntu-latest)        iOS (macos-14)
                             │                           │
                             ▼                           ▼
                    ┌──────────────┐       ┌───────────────────────┐
                    │     lint     │       │ ios-simulator-build   │
                    │   (detekt)   │       │ (K/Native link +      │
                    └──────┬───────┘       │  xcodebuild, no sign) │
                           │               └───────────────────────┘
                           ▼
                    ┌──────────────┐
                    │    build     │
                    │(assembleDbg) │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │    test      │
                    │  (JVM unit)  │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │   coverage   │
                    │   (Kover)    │
                    └──────────────┘
```

**Android pipeline** (sequential, on `ubuntu-latest`):

1. **actionlint** — validates GitHub Actions workflow syntax (only when `.github/` files changed)
2. **lint** — Detekt static analysis
3. **build** — `assembleDebug`
4. **test** — JVM unit tests via `testAndroidHostTest`, uploads test reports as artifacts
5. **coverage** — Kover HTML report, uploaded as artifact

**iOS pipeline** (parallel with Android, on `macos-14`):

6. **ios-simulator-build** — links Kotlin/Native debug framework (`linkDebugFrameworkIosSimulatorArm64`), then runs `xcodebuild` for simulator without signing. Validates K/Native linking, Swift-Kotlin interop, SPM resolution, and Xcode build config. No secrets required — uses placeholder `GoogleService-Info.plist`. Uploads `xcodebuild.log` on failure.

### Main branch CI (`debug.yml`)

Runs on every push to `main`. Same lint/build/test pipeline as PR, plus Firebase deploy.

```
trigger: push → main

  ┌──────────┐     ┌──────────┐     ┌──────────────┐     ┌───────────────────┐
  │   lint   │────▶│  build   │────▶│     test     │──┬──▶│  deploy-debug    │
  │ (detekt) │     │(assemble)│     │  (JVM unit)  │  │   │ (Firebase App    │
  └──────────┘     └──────────┘     └──────────────┘  │   │  Distribution)   │
                                                      │   └───────────────────┘
                                                      │
                                                      │   ┌───────────────────┐
                                                      └──▶│    coverage       │
                                                          │    (Kover)        │
                                                          └───────────────────┘
```

### Android Release (`release.yml`)

Triggered by pushing a `v*` tag (e.g., `v1.2.3`). Requires `release` environment in GitHub.

```
trigger: tag v*

  ┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
  │ decode keystore  │────▶│  assembleRelease │────▶│  upload artifact │────▶│  Firebase App    │
  │ (from secrets)   │     │  (signed APK)    │     │  (.apk)          │     │  Distribution    │
  └──────────────────┘     └──────────────────┘     └──────────────────┘     └──────────────────┘
```

Required secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `FIREBASE_APP_ID`, `FIREBASE_SERVICE_ACCOUNT`.

### iOS Release (`ios-release.yml`)

**Currently disabled.** Manual-only workflow (`workflow_dispatch`) with confirmation gate — requires typing "release" to execute.

```
trigger: manual (workflow_dispatch, type "release" to confirm)

  ┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
  │ Gradle: compile  │────▶│ Fastlane Match   │────▶│ Fastlane: build  │────▶│ Upload to        │
  │ K/Native arm64   │     │ (fetch certs)    │     │ & sign .ipa      │     │ TestFlight       │
  └──────────────────┘     └──────────────────┘     └──────────────────┘     └──────────────────┘
```

Requires Apple Developer Account ($99/year) and additional secrets: `MATCH_PASSWORD`, `MATCH_GIT_BASIC_AUTH`, `APP_STORE_CONNECT_KEY_ID`, `APP_STORE_CONNECT_ISSUER_ID`, `APP_STORE_CONNECT_KEY_CONTENT`.

## Shared Actions

### `gradle-setup` (`.github/actions/gradle-setup/`)

Composite action reused by all jobs. Sets up JDK 21 (Temurin) and configures Gradle via `gradle/actions/setup-gradle`.

## Fastlane (iOS)

Fastlane configuration lives in `fastlane/` and is used only by `ios-release.yml`:

- `Appfile` — Bundle ID, Apple ID, Team ID
- `Matchfile` — certificate storage config (encrypted git repo)
- `Fastfile` — `beta` lane: Match → build → upload to TestFlight

Ruby dependencies are declared in the root `Gemfile`.

## File Structure

```
.github/
  actions/
    gradle-setup/action.yml     — JDK 21 + Gradle setup
  workflows/
    pr.yml                      — PR checks (Android + iOS simulator build)
    debug.yml                   — main branch CI + Firebase deploy
    release.yml                 — Android release (tag-triggered)
    ios-release.yml             — iOS release (manual, requires Apple Developer Program)
fastlane/
  Appfile                       — Apple identifiers
  Matchfile                     — certificate management
  Fastfile                      — build & deploy lanes
Gemfile                         — Ruby deps (fastlane)
iosApp/
  ExportOptions.plist           — Xcode export config for App Store
```

## Secrets Reference

### Android (configured)

| Secret                     | Used in                    | Purpose                           |
| -------------------------- | -------------------------- | --------------------------------- |
| `FIREBASE_APP_ID`          | `debug.yml`, `release.yml` | Firebase App Distribution app ID  |
| `FIREBASE_SERVICE_ACCOUNT` | `debug.yml`, `release.yml` | Firebase service account JSON     |
| `KEYSTORE_BASE64`          | `release.yml`              | Release keystore (base64-encoded) |
| `KEYSTORE_PASSWORD`        | `release.yml`              | Keystore password                 |
| `KEY_ALIAS`                | `release.yml`              | Signing key alias                 |
| `KEY_PASSWORD`             | `release.yml`              | Signing key password              |

The `ios-simulator-build` job in `pr.yml` requires **no secrets** — it builds for simulator without signing.

### iOS Release (not configured)

| Secret                          | Used in           | Purpose                                 |
| ------------------------------- | ----------------- | --------------------------------------- |
| `MATCH_PASSWORD`                | `ios-release.yml` | Decryption password for Match cert repo |
| `MATCH_GIT_BASIC_AUTH`          | `ios-release.yml` | GitHub PAT for Match cert repo access   |
| `APP_STORE_CONNECT_KEY_ID`      | `ios-release.yml` | App Store Connect API key ID            |
| `APP_STORE_CONNECT_ISSUER_ID`   | `ios-release.yml` | App Store Connect API issuer ID         |
| `APP_STORE_CONNECT_KEY_CONTENT` | `ios-release.yml` | `.p8` key file content (base64)         |
