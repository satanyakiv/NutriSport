# Bash Output Discipline (rtk)

`rtk-ai/rtk` is a CLI that filters verbose Bash output before it reaches Claude's context. Used opt-in via an explicit subcommand prefix (`rtk find …`, `rtk err …`). It is NOT installed as a hook. Claude calls `rtk` voluntarily based on this rule.

## Allowlist — when to use `rtk`

| Pattern                              | Use this               |
| ------------------------------------ | ---------------------- |
| `./gradlew *:compile*` / `assemble*` | `rtk err ./gradlew …`  |
| `./gradlew *test* --tests *`         | `rtk test ./gradlew …` |
| `find … -name "*.<ext>"`             | `rtk find …`           |
| `ls -la <heavy dir>`                 | `rtk ls -la …`         |
| `git show <ref>`                     | `rtk git show <ref>`   |

## Excluded — do NOT route through rtk

| Command                                | Why                                                                                                                                          |
| -------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| `git status`                           | ~0% gain on a clean tree, just noise.                                                                                                        |
| `git log`                              | rtk can inflate the output.                                                                                                                  |
| `git diff`                             | Highly variable, re-measure before relying on it.                                                                                            |
| `adb logcat`                           | No dedicated subcommand; raw + grep is faster.                                                                                               |
| Any debugging session chasing warnings | `rtk err` collapses a success to a one-liner and hides KLIB / deprecation warnings. Use raw gradle when investigating.                       |
| Gradle commands expected to fail       | `rtk err` / `rtk test` on a failed build hide the actual compiler `e:` / JUnit failure line inline. Use raw gradle when failure is expected. |

## Failure fallback

If `rtk <subcommand> <args>` exits non-zero or returns suspicious output:

1. Re-run the raw command (drop the `rtk` prefix) on the next attempt.
2. Do NOT ask the user. This is a routine fallback, not a decision.

## What rtk hides (acceptable trade-off)

`rtk err` on a successful gradle build collapses the output to `[ok] Command completed successfully`. Hidden: KLIB resolver duplicate warnings (already documented as "ignore" in [conventions.md](conventions.md)) and the deprecated-Gradle-features warning. For active debugging (chasing flakes, investigating a deprecation), bypass rtk by running the raw gradle command.

## Related

- [ci-budget.md](ci-budget.md) — local verification before paying for CI.
- [conventions.md](conventions.md) — KLIB warnings that rtk legitimately hides.
