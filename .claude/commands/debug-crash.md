Read .claude/rules/architecture.md, .claude/rules/error-handling.md

## Crash Dump

$ARGUMENTS

## Process

1. **PARSE** — read the Tracey JSON replay file:
   - Crash event + stacktrace
   - Last 10-15 events before the crash (timeline)
   - Screen transitions (SCREEN events), gesture events (CLICK/SWIPE/SCROLL), breadcrumbs (LOG)
   - Device info, session ID, isCrashPayload flag

2. **CORRELATE** — for each stacktrace frame:
   - Glob/Grep to source file + read relevant lines
   - Determine module + architectural layer (domain/network/feature/navigation)
   - For SCREEN events → find destination in `navigation/.../NavGraph.kt`
   - For LOG events → find `Tracey.log()` call in ViewModel
   - For gesture events → find the Screen composable that was active

3. **ANALYZE** — root cause classification:
   | Category | Indicators | Fix Layer |
   |----------|-----------|-----------|
   | Null safety | NPE in stacktrace | Domain (NullSafety.kt) or Mapper |
   | Network | AppError.Network | :network repository |
   | State race | concurrent state entries | ViewModel (mutex/conflate) |
   | Navigation | IllegalArgumentException | Screen.kt / NavGraph.kt |
   | Auth | AppError.Unauthorized | :network auth check |
   | Data mapping | ClassCast/Serialization | Mapper in :network or feature |
   | Lifecycle | IllegalState after onDestroy | ViewModel scope/Flow collection |

4. **PRESENT** findings:

   ## User Journey (from dump)

   | #   | Time | Event | Detail | Code Path |
   | --- | ---- | ----- | ------ | --------- |

   ## Root Cause

   Category + explanation with `file:line` references

   ## Proposed Fix

   Minimal diff in correct architectural layer

   **Wait for "go".**

5. **FIX** — delegate to `/fix` with identified bug:
   `/fix <root cause description from analysis>`

## Bonus

If dump contains Tracey-generated Kotlin test (via `captureAndExportTest()`), show it as a ready-to-use regression test.

## Dump Location

Default: `./tracey-dumps/replay_<sessionId>.json`
Pull from device: `adb pull /data/data/com.portfolio.nutrisport.debug/files/tracey/ ./tracey-dumps/`

## Crashlytics Correlation

If Firebase MCP tools are available (`crashlytics_list_events`, `crashlytics_get_issue`):

- Search Crashlytics for matching crash signature (exception class + top frame)
- If found — show production impact: event count, affected users, app versions
- Suggest `/debug-crash-live` for full production-wide analysis

This connects local Tracey dumps (debug) with production Crashlytics data (release).

## Rules

- Always Read the dump file first, parse JSON
- Correlate every stacktrace frame with actual source files
- If dump references a ViewModel → read its full source
- If dump shows AppError → trace error origin in :network
- Suggest fix in the correct architectural layer
- Never modify code without "go" — present analysis first
