# CI Budget Discipline

GitHub Actions minutes are not unlimited. Every CI run costs real budget. Failed runs waste it just as effectively as successful ones, except without the deliverable.

**Rule:** before pushing to a branch that triggers CI, or before tagging / dispatching a workflow, reproduce the CI's critical steps locally. The goal is to fail fast on the dev machine instead of paying for a remote build that ends in a config error.

## When this rule applies

- Any push to `main` or a PR branch that triggers `pr.yml` or `debug.yml`.
- Any tag push `v*` that triggers `release.yml`.
- Any `workflow_dispatch` of `ios-release.yml` (and `e2e.yml` / `screen-audit.yml` once added).
- Any first run after editing a workflow YAML, a Gradle build file, a signing config, or a secret-bearing env var.

## What "verify locally" means

The minimum bar is to reproduce the failing-loud parts of the workflow on the dev machine.

| CI workflow                                        | Local equivalent (must pass)                                                                                                                                                                                                                                                 |
| -------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `pr.yml` (detekt + unit + composition tests)       | `./gradlew detekt :androidApp:testReleaseUnitTest :feature:**:testAndroidHostTest` on the touched modules                                                                                                                                                                    |
| `debug.yml` (Firebase App Distribution, debug APK) | `./gradlew :androidApp:assembleDebug :androidApp:appDistributionUploadDebug` with the same `GOOGLE_APPLICATION_CREDENTIALS` + Firebase app id the CI uses. Do NOT skip the upload step. Firebase rejects the APK there if the package name and the Firebase app id disagree. |
| `release.yml` (signed release + FAD)               | `./gradlew :androidApp:assembleRelease` with the signing config wired, then the upload step                                                                                                                                                                                  |
| `ios-release.yml` (Fastlane + TestFlight)          | `xcodebuild build -scheme iosApp -destination "platform=iOS Simulator,name=<sim>"`; for the upload itself, `fastlane` against a non-shipping lane. Do not waste real TestFlight slots.                                                                                       |
| `e2e.yml` (instrumented Compose UI tests)          | `workflow_dispatch` only. Local equivalent is `./gradlew :androidApp:connectedDebugAndroidTest`.                                                                                                                                                                             |

For multi-step workflows, run the steps most likely to fail (auth, env-var-dependent, third-party uploads), not just `compile`. A green compile that uploads to the wrong Firebase app is still a wasted run.

## What "verify" does NOT mean

- A green `assembleDebug` is **not** sufficient if the workflow's failure mode is in `appDistributionUploadDebug`.
- Caching from `setup-gradle` does NOT excuse the local check. The failure modes are usually configuration, not compilation.

## When the rule is suspended

- Bug-hunt on the workflow itself (you intentionally trigger CI to inspect runner-side state). Note this in the trigger commit message or in chat so the discipline lapse is visible.
- Workflows with zero `secrets.*` references that only run `detekt` or unit tests. Those have the same outcome locally and remotely, so the local check is the workflow.

## Anti-patterns

- "I'll just push and see what CI says."
- Re-triggering the same failing workflow with the same inputs hoping for a different result. The runner is deterministic. Check secrets, env, config first.
- Pushing a workflow patch and the trigger tag in the same `git push`. Patch first, observe, then trigger.

## Related

- [bash-output.md](bash-output.md) — local gradle output discipline.
- [docs/CI.md](../../docs/CI.md) — workflow inventory and trigger logic.
