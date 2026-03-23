---
name: replay-session
description: >-
  Read a Tracey session dump file, map events to code paths,
  reconstruct the user journey, and identify where things went wrong.
  Use when user provides a crash dump, says "replay this session",
  "what happened before the crash", "analyze this dump", "tracey dump",
  or provides a path to a replay JSON file.
---

## Session Dump

$ARGUMENTS

## Process

1. **Read and parse** the Tracey JSON replay file at the given path.
   Extract: session ID, device info, crash flag, event timeline, stacktrace (if crash).

2. **Build timeline table** — for each event, resolve corresponding source:

   | #   | Time | Type | Detail | Code Path |
   | --- | ---- | ---- | ------ | --------- |

   Event type mapping:
   - `SCREEN` → destination in `navigation/.../NavGraph.kt`
   - `CLICK`/`SWIPE`/`SCROLL`/`LONG_PRESS`/`PINCH` → Screen composable at active route
   - `LOG` → `Tracey.log()` call in ViewModel (Grep for the log message)
   - `FOREGROUND`/`BACKGROUND` → lifecycle event
   - `CRASH` → stacktrace + AppError mapping

3. **Identify failure point:**
   - Last successful state/event before crash
   - First error event in sequence
   - Time gap between last gesture and crash (user-perceived latency)
   - If no crash — identify anomalous state transitions

4. **If `captureAndExportTest()` output** is available alongside the dump:
   - Show the generated Kotlin test code
   - Adapt to project conventions (AAA pattern, test naming `should X when Y`)
   - Suggest target test file per testing.md rules

5. **If `claude-in-mobile` MCP is available** and user wants replay:
   - `mcp__claude-in-mobile__launch_app(package="com.portfolio.nutrisport.debug")`
   - For each gesture event: `mcp__claude-in-mobile__tap(x, y)` + `mcp__claude-in-mobile__wait(ms=delta)`
   - `mcp__claude-in-mobile__screenshot()` at failure point
   - Compare with expected state from dump

6. **Generate markdown report:**

   ## Session Replay: {sessionId}

   **Platform:** {platform} | **Duration:** {first event} → {last event}
   **Screens visited:** {list}
   **Crash:** {yes/no} | **Exception:** {type at file:line}

   ### Timeline

   (table from step 2)

   ### Failure Analysis
   - **Last good state:** {event before failure}
   - **First error:** {error event}
   - **Root cause:** {analysis}

   ### Recommended Next Steps
   - `/debug-crash {dump_path}` — for full fix workflow
   - `/gen-test` — generate regression test from dump

## Rules

- Always read the JSON file first — never guess from filename
- Map every event to actual code paths in the project
- If dump is not a crash (manual capture) — analyze for anomalies
- Keep report concise — focus on the failure, not every event
