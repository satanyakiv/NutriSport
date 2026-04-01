# Tracey Flight Recorder

## Overview

Tracey (`com.himanshoe:tracey:0.0.2-RC`) is a flight recorder for Compose. It records the last 30 seconds of app events into a ring buffer. On crash it saves a JSON session dump to disk. On next launch it dispatches the dump to reporters.

Replaces vague bug descriptions ("I tapped here, went there, crashed") with structured session dumps: gesture paths, navigation history, crash stacktraces, lifecycle events.

## Stack

| Library                 | Version    | Purpose                            |
| ----------------------- | ---------- | ---------------------------------- |
| `tracey`                | 0.0.2-RC   | Core: ring buffer, gestures, crash |
| `tracey-navigation`     | 0.0.2-RC   | NavController tracking integration |
| `kotlinx.serialization` | (built-in) | ReplayPayload JSON encoding        |

## Architecture

```
User gesture / nav / lifecycle / Tracey.log()
       │
       ▼
RecordingEngine (classify → InteractionEvent)
       │
       ├──▶ RingBuffer.add() (30s window, max 500)
       │
       └──▶ OverlayState.addEvent() (live debug UI)

On crash:
  RingBuffer.snapshotUnsafe() → ReplayPayload → saveToDisk()

Next launch:
  loadFromDisk() → ReporterDispatcher → [Logcat, File, HTML, Test, Custom]
```

## Key Components

### 1. Ring Buffer (`RingBuffer.kt`)

Thread-safe `ArrayDeque` protected by `Mutex`:

- `add(event)` — appends event + **lazy pruning**: removes events older than `maxDurationMs` (30s) or exceeding `maxEvents` (500)
- `snapshot()` — suspending safe copy of the buffer (for manual capture)
- `snapshotUnsafe()` — synchronous copy **without mutex**. Used by crash handler because the process is dying and coroutine context is unavailable

```
┌─────────────────────────────────────────────────┐
│ ArrayDeque (circular)                           │
│ [oldest] ← pruned on add ──▶ [newest event]    │
│                                                 │
│ Pruning rules:                                  │
│   1. timestampMs < (now - 30_000) → remove      │
│   2. size > 500 → remove oldest                 │
└─────────────────────────────────────────────────┘
```

### 2. Gesture Capture (`RecordingEngine.kt`)

A single `Modifier.pointerInput(Unit)` attached at root composable. Classifies touch input via thresholds:

| Gesture       | Condition                                  |
| ------------- | ------------------------------------------ |
| **Click**     | movement < 12dp                            |
| **LongPress** | movement < 12dp + hold > 500ms             |
| **Swipe**     | movement > 50px                            |
| **Scroll**    | movement + deltaX/deltaY                   |
| **Pinch**     | two-finger gesture with zoom/rotation data |

Velocity tracked via Compose `VelocityTracker` for all gesture types.

### 3. Semantic Path Resolution (`SemanticPathResolver.kt`)

Converts tap coordinates into human-readable paths by walking the Compose semantics tree:

```
HomeScreen > ProductCard[3] > AddButton
```

**Resolution priority:**

1. `testTag` — highest priority (set via `Modifier.testTag("...")`)
2. `contentDescription` — fallback for accessible composables
3. Coordinate fallback — `Screen[x=150,y=300]` when semantics unavailable

Walks from deepest node at `(x, y)` up to root, collecting ancestor labels.

### 4. TraceyHost (`TraceyHost.kt`)

Root composable wrapper. Does 4 things:

1. **Screenshot capture** — `GraphicsLayer.record { drawContent() }` gives you `toImageBitmap()` for PNG snapshots
2. **Recording modifier** — attaches `recordingEngine.modifier()` (pointerInput) to root `Box`
3. **Debug overlay** — when `showOverlay = true`, renders live event log + gesture trails on Canvas
4. **Event wiring** — `recordingEngine.onEventRecorded = overlayState::addEvent`. Each event goes to both the buffer and the overlay

### 5. Crash Handling (`CrashHandler.kt`)

Two-phase design:

**Phase 1 — crash (current session):**

```
UncaughtExceptionHandler fires
  → snapshotUnsafe() (no suspend — process dying)
  → build ReplayPayload synchronously
  → saveToDisk(JSON)
  → forward to original handler
  → process dies
```

**Phase 2 — next launch:**

```
Tracey.install()
  → checkAndDispatchPendingReplay()
  → loadFromDisk()
  → dispatch(payload) to all reporters
  → deleteFromDisk()
```

**Re-entrance guard:** `handlerInvoked` flag prevents infinite recursion if payload serialization itself crashes.

### 6. Navigation Tracking (`tracey-navigation`)

`rememberTraceyNavController()` wraps standard `rememberNavController()`:

- Adds `OnDestinationChangedListener` to `NavController`
- Records `InteractionEvent.ScreenView(route)` on each navigation
- Cleanup via `DisposableEffect`. Listener removed automatically on disposal

### 7. Reporter System

```kotlin
interface TraceyReporter {
    suspend fun onReplayReady(payload: ReplayPayload)
}
```

`ReporterDispatcher` calls all reporters **concurrently** in supervised coroutines. One reporter failure does not block others. Errors are swallowed and logged.

**Built-in reporters:**

| Reporter             | Output                                  |
| -------------------- | --------------------------------------- |
| `LogcatReporter`     | Formatted log to Android logcat         |
| `FileReporter`       | JSON file on disk                       |
| `ReplayHtmlExporter` | Self-contained HTML replay report       |
| `TestCaseExporter`   | Generated Kotlin Compose UI test source |

## Event Types

```
sealed interface InteractionEvent
├── Click(x, y, path, timestampMs)
├── LongPress(x, y, holdDurationMs)
├── Scroll(deltaX, deltaY, velocityX, velocityY)
├── Swipe(startX/Y, endX/Y, velocityX/Y)
├── Pinch(centroidX/Y, zoomDelta, rotationDelta)
├── ScreenView(screenName)
├── Breadcrumb(message)           ← Tracey.log("...")
├── AppForeground
└── AppBackground
```

All types are `@Serializable` via `kotlinx.serialization`.

## ReplayPayload

```kotlin
data class ReplayPayload(
    val sessionId: String,
    val appVersion: String,
    val capturedAtMs: Long,
    val durationMs: Long,
    val crashReason: String?,         // null for manual captures
    val deviceInfo: DeviceInfo,
    val events: List<InteractionEvent>,
    val timeline: String,             // Pre-formatted human-readable log
    val screenshotPng: ByteArray?,    // @Transient — excluded from JSON
)
```

**Timeline format:**

```
00:01.234  CLK        HomeScreen > ProductCard[3] > AddButton
00:03.567  SCROLL     HomeScreen > LazyColumn (Δx=0.0, Δy=-48.5)
00:05.890  SWIPE      HomeScreen > LazyColumn (50,200 → 50,500)
00:08.123  LONG PRESS HomeScreen > Item[5] (523ms)
00:12.456  SCREEN     ProfileScreen
00:15.789  LOG        User tapped checkout
```

## Configuration

```kotlin
data class TraceyConfig(
    val enabled: Boolean = true,
    val showOverlay: Boolean = false,
    val bufferDurationSeconds: Int = 30,
    val maxEvents: Int = 500,
    val reporters: List<TraceyReporter> = emptyList(),
    val redactedTags: List<String> = emptyList(),   // testTags to exclude from recording
    val trackLifecycle: Boolean = true,
    val generateHtmlReport: Boolean = false,
    val sessionIdProvider: () -> String = { generateUUID() },
)
```

## NutriSport Integration

Tracey is integrated via **Strategy pattern + build-type DI**. Zero impact on release/iOS builds. See architecture details in the main [CLAUDE.md](../CLAUDE.md) and commit `21b5253`.

```
DebugToolkit (interface, :navigation/commonMain)
├── NoOpDebugToolkit  — release/iOS (all methods are no-op)
└── TraceyDebugToolkit — debug only (androidApp/src/debug/)

DebugModuleProvider (exists in BOTH src/debug/ and src/release/)
├── debug:   single<DebugToolkit> { TraceyDebugToolkit() }
└── release: single<DebugToolkit> { NoOpDebugToolkit() }
```

**Key files:**

| File                                                | Purpose                         |
| --------------------------------------------------- | ------------------------------- |
| `navigation/.../debug/DebugToolkit.kt`              | Interface contract (commonMain) |
| `navigation/.../debug/NoOpDebugToolkit.kt`          | Release/iOS no-op (commonMain)  |
| `androidApp/src/debug/.../TraceyDebugToolkit.kt`    | Tracey wiring (debug only)      |
| `androidApp/src/debug/.../ClaudeReporter.kt`        | Custom reporter → file export   |
| `androidApp/src/debug/.../DebugModuleProvider.kt`   | Koin module (debug)             |
| `androidApp/src/release/.../DebugModuleProvider.kt` | Koin module (release)           |

**Dump location:** `/data/data/com.portfolio.nutrisport.debug/files/tracey/`

```bash
adb pull /data/data/com.portfolio.nutrisport.debug/files/tracey/ ./tracey-dumps/
```

## Not Covered (and Why)

- **Tracey iOS support** — library is Android-only as of 0.0.2-RC; iOS uses `NoOpDebugToolkit`
- **Network request recording** — Tracey captures UI events only, not HTTP traffic
- **Screenshot replay** — `screenshotPng` is captured but not rendered in HTML report yet
- **Production crash reporting** — Tracey is debug-only; production crashes are handled by [Firebase Crashlytics](CRASHLYTICS.md) with automated analysis via `/debug-crash-live`

## Related

- [CLAUDE.md](../CLAUDE.md) — DebugToolkit strategy pattern, build gotchas
- `.claude/commands/debug-crash.md` — crash dump analysis workflow
- `.claude/agents/crash-analyzer.md` — automated crash severity classification
- `.claude/features/05-tracey-flight-recorder.md` — original feature plan
