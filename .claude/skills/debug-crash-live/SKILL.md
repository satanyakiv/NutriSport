---
name: debug-crash-live
description: >-
  Pull live crash data from Firebase Crashlytics via MCP, correlate with
  source code, and generate an analysis report with proposed fix.
  Use when user says "analyze crashes", "check crashlytics",
  "what's crashing in production", "crashlytics issue", "production crashes",
  or provides a Crashlytics issue ID.
---

Read .claude/rules/architecture.md, .claude/rules/error-handling.md

## Live Crash Analysis

$ARGUMENTS

## Prerequisites

Firebase MCP plugin must be installed and authenticated:

```bash
claude plugin marketplace add firebase/firebase-tools
claude plugin install firebase@firebase
npx firebase-tools@latest login
```

If MCP tools are unavailable, inform the user and guide through setup.

## Process

1. **LIST** top issues:
   - Use `crashlytics_list_events` to fetch recent crash/exception events
   - Present top 5 issues: title, event count, affected users, last seen, app version
   - If user provided a specific issue ID — skip to step 2

2. **FETCH** issue details:
   - `crashlytics_get_issue` — full issue data (stacktrace, metadata, device info)
   - `crashlytics_batch_get_events` — last 5-10 events for device/OS distribution

3. **CORRELATE** stacktrace with code:
   - For each frame in the stacktrace:
     - Glob/Grep to source file + Read relevant lines
     - Determine module + architectural layer:
       | Package | Module |
       |---------|--------|
       | `com.nutrisport.shared.domain` | `:domain` |
       | `com.nutrisport.shared.util` | `:shared:utils` |
       | `com.nutrisport.network` / `com.nutrisport.data` | `:network` |
       | `com.nutrisport.{feature}` | `:feature:{feature}` |
       | `com.nutrisport.navigation` | `:navigation` |
       | `com.portfolio.nutrisport` | `:androidApp` |

4. **CROSS-REFERENCE** with Tracey dumps (if available):
   - Check `./tracey-dumps/` for matching crash signature
   - If found — merge Tracey user journey timeline with Crashlytics data
   - Tracey gives gestures/navigation before crash, Crashlytics gives production impact

5. **CLASSIFY** root cause:

   | Category     | Indicators                   | Fix Layer                        |
   | ------------ | ---------------------------- | -------------------------------- |
   | Null safety  | NPE in stacktrace            | Domain (NullSafety.kt) or Mapper |
   | Network      | AppError.Network             | :network repository              |
   | State race   | concurrent state entries     | ViewModel (mutex/conflate)       |
   | Navigation   | IllegalArgumentException     | Screen.kt / NavGraph.kt          |
   | Auth         | AppError.Unauthorized        | :network auth check              |
   | Data mapping | ClassCast/Serialization      | Mapper in :network or feature    |
   | Lifecycle    | IllegalState after onDestroy | ViewModel scope/Flow collection  |

6. **PRESENT** findings:

   ## Crashlytics Issue: {title}

   **Events:** {count} | **Users:** {count} | **Last seen:** {date}
   **App versions:** {list} | **Devices:** {top 3}

   ### Stacktrace (key frames)

   | #   | File:Line | Module | Code |
   | --- | --------- | ------ | ---- |

   ### Root Cause

   **Category:** {category}
   **Explanation:** with `file:line` references

   ### Proposed Fix

   Minimal diff in correct architectural layer

   ### Production Impact
   - Severity: Critical / High / Medium / Low
   - Affected user percentage
   - First seen → last seen (regression window)

7. **ANNOTATE** issue (with user permission):
   - `crashlytics_create_note` — write root cause summary + fix reference
   - `crashlytics_update_issue` — mark as acknowledged (never close without permission)

   **Wait for user approval before annotating or fixing.**

## Rules

- Always fetch data via MCP first — never guess from issue title
- Correlate every stacktrace frame with actual source files
- If crash is in a ViewModel → read its full source
- If crash shows AppError → trace error origin in :network
- Never close/resolve Crashlytics issues without explicit user approval
- If MCP tools are not connected — guide user through Firebase plugin setup
- Suggest `/debug-crash <dump>` if Tracey dump exists for the same crash
