---
name: firebase-ops
description: Firebase operations for the NutriSport project via the firebase-tools CLI plus a zero-dependency FCM HTTP v1 sender — NO always-on Firebase MCP. Use when the user wants to read or roll back Remote Config, upload Crashlytics native symbols or R8 mapping files, distribute a build via Firebase App Distribution, list / inspect Firebase apps, or send a test FCM push notification (especially while working on push notifications). Triggers on "firebase", "remote config", "crashlytics symbols", "mapping file", "app distribution", "send test push", "FCM", "тестовий пуш", "відправ пуш", "remote config rollback", "firebase apps list", or any Firebase task on this repo that isn't pure client-SDK Kotlin code. NOT for Firestore/Auth data CRUD (no CLI surface — those need the Admin SDK / Console, deliberately out of scope here). NOT for editing google-services.json (that is a Firebase Console + applicationId concern).
---

# firebase-ops

Firebase operations for NutriSport without the always-on Firebase MCP. This skill matches the repo's dominant integration pattern (CLI-wrapped-in-skill, like `infra-weekly`) and respects the project's MCP-budget discipline (`figma-mcp-budget` skill exists precisely because every always-on MCP server is a standing context cost).

## Project constants (baked in — don't re-discover)

| Constant              | Value                                                                              | Source                            |
| --------------------- | ---------------------------------------------------------------------------------- | --------------------------------- |
| Firebase project id   | `utility-inquiry-477410-i3`                                                        | `androidApp/google-services.json` |
| Project number        | `388178172033`                                                                     | same                              |
| Android app (release) | `com.portfolio.nutrisport` → `1:388178172033:android:1c3395f5a1b5c2553a5580`       | same                              |
| Android app (debug)   | `com.portfolio.nutrisport.debug` → `1:388178172033:android:32b819f0d29b06c23a5580` | same                              |

The CLI auto-detects the project from `androidApp/google-services.json` when run from repo root, but pass `--project utility-inquiry-477410-i3` explicitly in CI or ambiguous contexts.

`firebase` CLI is installed via firebase-tools. No `firebase.json` / `.firebaserc` in the repo — Firebase here is pure client-SDK (Crashlytics, FCM, Auth/Firestore at runtime), there are no Hosting/Functions deploy targets.

## What this skill covers (and what it deliberately does NOT)

### Covered via firebase-tools CLI

| Task                                    | Command                                                                                                      |
| --------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| Get current Remote Config template      | `firebase remoteconfig:get --project utility-inquiry-477410-i3 -o -`                                         |
| Get a specific RC version               | `firebase remoteconfig:get -v <N> --project utility-inquiry-477410-i3 -o -`                                  |
| List RC versions                        | `firebase remoteconfig:versions:list --project utility-inquiry-477410-i3`                                    |
| Roll back RC                            | `firebase remoteconfig:rollback -v <N> --project utility-inquiry-477410-i3`                                  |
| Upload native (NDK) Crashlytics symbols | `firebase crashlytics:symbols:upload --app <APP_ID> <symbolDir>`                                             |
| Generate R8 mapping file id             | `firebase crashlytics:mappingfile:generateid -r <res.xml>`                                                   |
| Upload R8 mapping for deobfuscation     | `firebase crashlytics:mappingfile:upload --app <APP_ID> --resource-file <res.xml> <mapping.txt>`             |
| Distribute a build to testers           | `firebase appdistribution:distribute <apk-or-aab> --app <APP_ID> --groups <group> --release-notes "<notes>"` |
| List registered Firebase apps           | `firebase apps:list --project utility-inquiry-477410-i3`                                                     |
| Print an app's SDK config               | `firebase apps:sdkconfig ANDROID <APP_ID>`                                                                   |
| List Android SHA certs                  | `firebase apps:android:sha:list <APP_ID>`                                                                    |

### Covered via the bundled FCM sender

`scripts/fcm-send.sh` — send a test push through the FCM HTTP v1 API. Zero extra deps (python3 stdlib + openssl + curl). Needs a service-account key (one-time human step, see below). This is the primary value for **push-notification work** — verify a payload reaches a real device without writing throwaway Admin-SDK code.

### Deliberately NOT covered (honest limitations)

| Want                                     | Why not here                                                                                  | Where instead                                                                                               |
| ---------------------------------------- | --------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| Query Crashlytics issues / crash trends  | `firebase-tools` CLI has NO crash-query command (only symbol/mapping _upload_)                | Firebase Console, or BigQuery Crashlytics export, or the Firebase MCP `crashlytics_*` tools if ever adopted |
| Auth user CRUD (get/update/delete users) | No first-class CLI; needs Admin SDK                                                           | A dedicated Admin-SDK script, or Firebase Console                                                           |
| Firestore / RTDB data read-write         | No general CLI data API                                                                       | Admin SDK script, or Console                                                                                |
| Edit `google-services.json`              | That is an applicationId + Firebase Console concern, with `applicationIdSuffix=.debug` nuance | Manual Console re-download per `.claude/rules/conventions.md`                                               |

If a future need genuinely requires the Admin-SDK surface (bulk Auth migration, Firestore backfill), that's a separate purpose-built script with its own service-account scope — do not reach for the always-on Firebase MCP just for a one-off.

## Service-account key — one-time human step

`scripts/fcm-send.sh` (and any future Admin-SDK helper) needs a Firebase service-account JSON key. This is a credential with the same hygiene class as the iOS signing artifacts.

Tell the user to do this once:

1. Firebase Console → Project Settings (gear) → **Service accounts** tab
2. Click **Generate new private key** → confirm → a JSON downloads (named like `utility-inquiry-477410-i3-firebase-adminsdk-xxxxx.json`)
3. Move + rename it:
   ```bash
   mkdir -p ~/.nutrisport/firebase
   mv ~/Downloads/utility-inquiry-477410-i3-firebase-adminsdk-*.json \
      ~/.nutrisport/firebase/service-account.json
   chmod 600 ~/.nutrisport/firebase/service-account.json
   ```
4. Strongly advise copying it into 1Password / Bitwarden alongside the iOS signing backup. If lost, generate a new one (old one can be revoked in the same Console tab).

The key lives **outside the repo** by design. `.gitignore` also carries defensive patterns (`*-firebase-adminsdk-*.json`, `service-account*.json`) in case someone drops one into the tree by accident — but the canonical location is `~/.nutrisport/firebase/` (or override per-invocation with `--sa <path-to-service-account.json>`).

When Claude asks the user to do this: surface it as a blocking prerequisite. Claude must never type or fabricate the key contents.

## FCM sender usage

```bash
# Single device, basic notification
bash .claude/skills/firebase-ops/scripts/fcm-send.sh \
  --token "<DEVICE_FCM_REGISTRATION_TOKEN>" \
  --title "NutriSport" \
  --body "Your order has shipped"

# Topic broadcast with a data payload (deep-link routing)
bash .claude/skills/firebase-ops/scripts/fcm-send.sh \
  --topic "promos" \
  --title "New protein flavor" \
  --body "Chocolate peanut butter just dropped" \
  --data screen=Details --data productId=prod_123

# Build + print the payload WITHOUT sending or minting a token (safe inspect)
bash .claude/skills/firebase-ops/scripts/fcm-send.sh \
  --token "<TOKEN>" --title T --body B --dry-run
```

Always start with `--dry-run` when iterating on payload shape — it skips the OAuth round-trip and just shows the exact JSON FCM would receive. Only drop `--dry-run` once the payload is right.

Get a real device registration token from a debug build's logcat (the app logs its FCM token on first launch once push wiring calls `FirebaseMessaging.getToken()`), or from a Firestore `users/<uid>.fcmToken` field if the backend stores it.

Exit codes: `0` ok, `1` arg error, `2` auth/token-mint error (usually a missing or wrong service-account key), `3` FCM API rejected the message (read the printed JSON — common causes: stale token = `UNREGISTERED`, malformed data values must be strings).

## Common operations — worked examples

### Inspect / roll back Remote Config

```bash
cd /Users/taxistsamael/AndroidStudioProjects/NutriSport
firebase remoteconfig:get --project utility-inquiry-477410-i3 -o - | python3 -m json.tool
firebase remoteconfig:versions:list --project utility-inquiry-477410-i3
firebase remoteconfig:rollback -v 12 --project utility-inquiry-477410-i3   # revert to version 12
```

RC is the right lever for the `AppConfig.useFakeData` / feature-flag style toggles the project leans on (`.claude/rules/fake-data.md`). Reading the live template is also a good sanity check before a stakeholder demo.

### Crashlytics deobfuscation upload (release builds)

R8 obfuscates release stack traces. After a release build, the mapping file at `androidApp/build/outputs/mapping/release/mapping.txt` must be uploaded so Crashlytics can deobfuscate:

```bash
APP_ID=1:388178172033:android:1c3395f5a1b5c2553a5580   # release
firebase crashlytics:mappingfile:upload \
  --app "$APP_ID" \
  --resource-file androidApp/src/main/res/values/strings.xml \
  androidApp/build/outputs/mapping/release/mapping.txt
```

This is a natural addition to a release CI job. The Crashlytics Gradle plugin usually auto-uploads, but this CLI path is the manual fallback when the plugin is disabled (e.g., benchmark variant per `.claude/rules/conventions.md` Strategy Pattern).

### App Distribution (Android beta, parallels iOS TestFlight)

```bash
firebase appdistribution:distribute \
  androidApp/build/outputs/apk/release/androidApp-release.apk \
  --app 1:388178172033:android:1c3395f5a1b5c2553a5580 \
  --groups "developers" \
  --release-notes "$(git log -1 --pretty=%s)"
```

This is the Android analog of an iOS TestFlight pipeline. If the team later wants Android beta distribution that mirrors an iOS sandbox/production split, this command is the building block — a follow-up `android-appdistribution.yml` workflow would wrap it.

## Why CLI+skill, not MCP (rationale for future maintainers)

This decision was made deliberately:

- The repo's integration philosophy is CLI-wrapped-in-skill. `infra-weekly` wraps `git`; zero skills are MCP. `figma-mcp-budget` exists specifically to discipline MCP cost.
- `firebase-tools` is already installed and is exactly what the Firebase MCP shells out to internally — adopting the MCP would add ~30 always-on deferred tools to every session's context pool for capability we mostly get from one already-present binary.
- The genuine MCP-only gap (send-test-FCM, Auth CRUD, RTDB) is narrow. The single high-value one — send-test-FCM for push-notification work — is solved here by `fcm-send.sh` (a one-time ~120-line zero-dep script) instead of a standing MCP server.
- A skill can bake in project constants (project id, app ids, the fake-data/RC tie-in) and respect repo conventions; a generic MCP cannot.

If the team ever needs autonomous, unprompted Crashlytics querying mid-debug (the one place MCP genuinely wins), revisit — but that contradicts this repo's deterministic `methodology.md` routing (explicit anti-"invoke on every turn"), so it's unlikely to be worth the standing cost.

## Files

```
firebase-ops/
├── SKILL.md            (this file)
└── scripts/
    └── fcm-send.sh     (zero-dep FCM HTTP v1 sender: python3 stdlib + openssl JWT + curl)
```

## Related

- `.claude/rules/fake-data.md` — Remote Config is the production lever for the fake-data toggle
- `.claude/rules/conventions.md` — Crashlytics plugin Strategy Pattern (when manual mapping upload is the fallback)
- `figma-mcp-budget` skill — the MCP-cost discipline this skill's CLI-not-MCP choice follows
