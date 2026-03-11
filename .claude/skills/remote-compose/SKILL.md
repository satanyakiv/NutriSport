# Remote Compose Architecture Guide

Skill for answering questions about Remote Compose, helping integrate it, and providing architecture guidance.

## Trigger

Use when user asks about Remote Compose, server-driven UI with AndroidX, or wants to integrate Remote Compose into the project.

## Instructions

Read the following reference files before responding:
- `.claude/references/remote-compose.md` — full guide (API, modules, limitations, platforms)
- `.claude/references/remote-compose-repos.md` — repository diagrams and comparisons

### Key Facts

1. **Remote Compose is Android-only** for playback. No iOS support. Not a KMP solution.
2. **Status: alpha** (1.0.0-alpha05). NOT production-ready.
3. **Two sides:** Creation (JVM server) + Player (Android client).
4. **Binary format** (.rc) — not JSON. Captured drawing operations.
5. **Player is View-based** (`RemoteComposePlayer` extends View), wrap in `AndroidView` for Compose.
6. **No official test artifacts** for external use.
7. **minSdk 23** (from alpha04).

### For NutriSport Integration

If asked to integrate Remote Compose into NutriSport:
- Creation side would be a **separate JVM module** or **backend service** (not in the app)
- Player side would go in a **new feature module** with `remote-player-*` deps
- This does NOT replace Compose Multiplatform — it's a different paradigm
- Consider `compose-remote-layout` (utsmannn) instead if KMP (iOS) support is needed
- Warn: alpha status means API instability, not recommended for production features

### Architecture Pattern

```
Server (JVM):
  @RemoteComposable code → RemoteComposeWriter → ByteArray (.rc)

Network:
  HTTP / Firebase / file

Client (Android):
  ByteArray → RemoteComposePlayer.setDocument(bytes) → Canvas rendering
  Click actions → callback → NavController
```

### Dependencies

```kotlin
// Player only (client app)
implementation("androidx.compose.remote:remote-core:1.0.0-alpha05")
implementation("androidx.compose.remote:remote-player-core:1.0.0-alpha05")
implementation("androidx.compose.remote:remote-player-view:1.0.0-alpha05")

// Creation (server/converter)
implementation("androidx.compose.remote:remote-creation-core:1.0.0-alpha05")
implementation("androidx.compose.remote:remote-creation-jvm:1.0.0-alpha05")
```
