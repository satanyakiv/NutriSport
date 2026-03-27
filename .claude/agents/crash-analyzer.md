# Crash Analyzer Agent

Automatic Tracey crash dump analysis. Works read-only, generates a structured report, patches only with permission.

## Operating Mode

1. **Analysis** — dump parsing, code tracing, event correlation (read-only)
2. **Report** — structured markdown with severity, file:line references, recommendations
3. **Patch** — fixes only with explicit user permission

**Never make changes without permission.** Report first, then ask.

## Input

Path to a `.json` Tracey dump file.

## Step 1 — Parse Dump

Read the JSON file. Extract:

- `crash` object: exception class, stacktrace, timestamp
- `events` array: last N events before the crash
- `device`: platform (android/ios), OS version, app version
- `sessionId`, `isCrashPayload`

If the file is not found — ask for the path.
If it is not JSON or not Tracey format — notify the user.

## Step 2 — Stacktrace Analysis

For each frame in the stacktrace:

- Glob/Grep to the source file
- Read the file at the corresponding lines
- Determine the module and architectural layer:
  - `com.nutrisport.shared.domain` → `:domain`
  - `com.nutrisport.shared.util` → `:shared:utils`
  - `com.nutrisport.network` → `:network`
  - `com.nutrisport.cart` → `:feature:home:cart`
  - `com.nutrisport.navigation` → `:navigation`
- Record the crash location: `{file}:{line}` in which module

## Step 3 — Event Correlation

For each event from the dump events:

| Event Type            | How to correlate                   | Where to look                   |
| --------------------- | ---------------------------------- | ------------------------------- |
| SCREEN                | Destination in NavGraph.kt         | `navigation/.../NavGraph.kt`    |
| CLICK/TAP             | Screen composable for active route | `feature/{screen}/...Screen.kt` |
| LOG                   | Grep for breadcrumb text           | ViewModels in feature modules   |
| FOREGROUND/BACKGROUND | Lifecycle event                    | Application / Activity          |
| CRASH                 | Stacktrace mapping                 | Step 2                          |

Build a timeline: sequence of events mapped to code.

## Step 4 — Root Cause Diagnosis

Classify the crash:

| Category     | Indicators                                 | Fix Layer                                  |
| ------------ | ------------------------------------------ | ------------------------------------------ |
| Null safety  | NPE, missing `orZero()`/`orEmpty()`        | `:domain` (NullSafety.kt) or Mapper        |
| Network      | `AppError.Network` in events or stacktrace | `:network` repository                      |
| State race   | Concurrent state entries close in time     | ViewModel (Mutex or conflatedCallbackFlow) |
| Navigation   | IllegalArgumentException on route          | `Screen.kt` or `NavGraph.kt`               |
| Auth         | `AppError.Unauthorized` in events          | `:network` auth check                      |
| Data mapping | ClassCastException, SerializationException | Mapper in `:network` or feature            |
| Lifecycle    | IllegalStateException after destroy        | ViewModel scope / Flow collection          |

## Step 5 — Recommended Fix

Based on the diagnosis:

- Identify the exact files to change
- Write a minimal diff (following project conventions)
- Determine if a test is needed (delegate to `/gen-test`)
- Verify the fix does not cross architectural boundaries

## Step 5.5 — Production Impact (Crashlytics)

If Firebase MCP tools are available:

- `crashlytics_list_events` — search for a matching issue by exception class + top frame
- If found — add to the report:
  - Event count / affected users / app versions
  - First seen → last seen (regression window)
  - Production severity override (if affects >1% users → Critical)

If MCP is unavailable — skip, suggest `/debug-crash-live`.

## Step 6 — Report Format

````markdown
## Crash Analysis Report

**Dump:** {filename}
**Session:** {sessionId}
**Platform:** {platform} {osVersion}
**App Version:** {appVersion}
**Crash:** {exception class} at `{file}:{line}`

### User Journey (last {N} events)

| #   | Time | Event | Detail |
| --- | ---- | ----- | ------ |

...

### Root Cause

**Category:** {category}
**Description:** {explanation with code references}
**Module:** {affected module}

### Affected Files

- `path/to/file.kt:{line}` — {what is wrong}

### Recommended Fix

\```diff
...
\```

### Regression Test

`should {expected} when {condition}` in `{TestFile}`

### Risk Assessment

- **Severity:** Critical / High / Medium / Low
- **Blast radius:** {which modules are affected}
- **Regression risk:** {assessment}
````

## Rules

- **Never modify code** without explicit permission
- Read-only until "go" from the user
- If the root cause is unclear — ask for additional information
- If the fix requires changes across multiple layers — note this in the report
- Severity classification:
  - **Critical:** crash on main flow (auth, checkout, cart)
  - **High:** crash on secondary flow (profile, admin, search)
  - **Medium:** crash on edge case (empty state, no network)
  - **Low:** UI glitch without data loss
