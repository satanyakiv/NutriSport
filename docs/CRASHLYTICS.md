# Crashlytics

Firebase Crashlytics for automatic crash reporting in production builds. Integrated directly in `androidApp` without abstractions — Crashlytics auto-captures all unhandled exceptions.

## Stack

| Tool                      | Version        | Purpose                                   |
| ------------------------- | -------------- | ----------------------------------------- |
| Firebase Crashlytics SDK  | via BOM 33.1.2 | Native Android crash capture              |
| Crashlytics Gradle Plugin | 3.0.2          | Mapping file upload for deobfuscation     |
| Firebase MCP Plugin       | latest         | Automated crash analysis from Claude Code |

## Architecture

```
Production crash (release build)
       │
       ▼
Firebase Crashlytics SDK (auto-capture)
       │
       ├──▶ Firebase Console (dashboard, alerts)
       │
       └──▶ Firebase MCP ──▶ Claude Code
                               │
                               ├── /debug-crash-live (live analysis)
                               ├── crashlytics:connect (guided workflow)
                               └── Correlate with Tracey dumps (debug)
```

**Two complementary systems:**

| System          | Environment  | Captures                                            | Analysis                                   |
| --------------- | ------------ | --------------------------------------------------- | ------------------------------------------ |
| **Tracey**      | Debug only   | Gestures, navigation, breadcrumbs, crash stacktrace | `/debug-crash`, `/replay-session`          |
| **Crashlytics** | Release only | Crash stacktraces, device info, event counts        | `/debug-crash-live`, `crashlytics:connect` |

## Configuration

### SDK Setup

Crashlytics is wired in 3 files:

```
gradle/libs.versions.toml       — plugin version + library entry
androidApp/build.gradle.kts      — plugin applied + dependency
androidApp/.../NutrisportApplication.kt — via FirebaseConfigurator (Strategy pattern)
```

> Note: Crashlytics plugin is NOT declared in root `build.gradle.kts` — only in `androidApp`. Root declaration causes "Google-Services plugin not found" error.

### Collection Toggle (via FirebaseConfigurator Strategy)

Firebase initialization and Crashlytics configuration use the Strategy pattern — zero `if/else` in `NutrisportApplication`:

```kotlin
// NutrisportApplication.onCreate():
getKoin().get<FirebaseConfigurator>().initialize(this)
```

| Build Type | Implementation                | Firebase Init | Crashlytics                               |
| ---------- | ----------------------------- | ------------- | ----------------------------------------- |
| debug      | `DebugFirebaseConfigurator`   | Yes           | **Disabled** — avoids polluting dashboard |
| release    | `ReleaseFirebaseConfigurator` | Yes           | **Enabled** — all crashes auto-reported   |
| benchmark  | `NoOpFirebaseConfigurator`    | No            | No — isolated from Firebase               |

Implementations live in `androidApp/src/{debug,release,benchmark}/`, wired via `DebugModuleProvider`.

### Mapping Files (Deobfuscation)

The Crashlytics Gradle plugin automatically uploads ProGuard/R8 mapping files during `assembleRelease`. This enables readable stacktraces in the Firebase Console.

Existing ProGuard rule covers GitLive Firebase classes:

```
-keep class dev.gitlive.firebase.** { *; }
```

## Firebase MCP Plugin

Enables automated crash analysis directly from Claude Code.

### Setup

```bash
# Install Firebase plugin for Claude Code
claude plugin marketplace add firebase/firebase-tools
claude plugin install firebase@firebase

# Authenticate (one-time)
npx firebase-tools@latest login

# Verify
claude mcp list
```

### Available MCP Tools

| Tool                           | Purpose                                    |
| ------------------------------ | ------------------------------------------ |
| `crashlytics_get_issue`        | Fetch issue details (stacktrace, metadata) |
| `crashlytics_list_events`      | Query recent crash events with filters     |
| `crashlytics_batch_get_events` | Batch fetch specific events                |
| `crashlytics_get_report`       | Numerical aggregations (event/user counts) |
| `crashlytics_create_note`      | Add debugging notes to issues              |
| `crashlytics_update_issue`     | Change issue state (fixed, reopened)       |

### Guided Workflow

```
crashlytics:connect
```

Starts a conversational debugging session: prioritizes issues, fetches sample events, generates proposed root cause and solution based on the codebase.

### Claude Code Skills

| Skill               | Trigger                                 | Purpose                                                            |
| ------------------- | --------------------------------------- | ------------------------------------------------------------------ |
| `/debug-crash-live` | "analyze crashes", Crashlytics issue ID | Live crash analysis via MCP                                        |
| `/debug-crash`      | Tracey dump file path                   | Offline crash dump analysis (updated with Crashlytics correlation) |
| `/replay-session`   | Tracey dump file path                   | Session replay and timeline reconstruction                         |

## Not Covered (and Why)

- **iOS Crashlytics** — requires Apple Developer Account for meaningful crash reports (dSYM upload + TestFlight distribution)
- **Non-fatal exception logging from commonMain** — current setup is Android-only native SDK; add GitLive `firebase-crashlytics:2.4.0` KMP wrapper when needed
- **BigQuery export** — advanced analytics for high-volume apps; not needed at current scale
- **Custom crash keys/user context** — can be added incrementally via `FirebaseCrashlytics.setCustomKey()` / `.setUserId()`
- **Crash alerts** — configured in Firebase Console, not in code

## Related

- [TRACEY.md](TRACEY.md) — debug-only flight recorder (gestures, navigation, breadcrumbs)
- [FIREBASE_SETUP.md](FIREBASE_SETUP.md) — Firebase project configuration, security rules, config files
- `.claude/skills/debug-crash-live/SKILL.md` — live crash analysis skill definition
- `.claude/commands/debug-crash.md` — offline crash dump analysis command
- `.claude/agents/crash-analyzer.md` — automated crash severity classification agent
