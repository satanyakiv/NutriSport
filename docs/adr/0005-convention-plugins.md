# 0005 — Convention plugins for multi-module consistency

Status: Accepted (2026-04-01)

## Context

The project spans 25 Gradle modules. If each module hand-rolls its KMP targets, Compose setup, test stack, and Kover wiring, a single toolchain bump means editing 25 build files, and configuration drifts between modules over time.

## Decision

Centralize build configuration in three precompiled convention plugins under `build-logic/convention`:

- `nutrisport.kmp.library` — KMP + Compose + Mokkery + `compose.uiTest` + Robolectric + Kover.
- `nutrisport.kmp.feature` — library + Koin Compose.
- `nutrisport.kmp.feature.full` — feature + Compose Navigation + Ktor clients.

Each module applies exactly one plugin and declares only its own dependencies.

## Consequences

- A toolchain, test-stack, or Kover change is a one-file edit in `build-logic`, applied uniformly across 25 modules.
- Module `build.gradle.kts` files stay short and uniform.
- `compileSdk`/`minSdk` can't live in precompiled plugins (AGP limitation), so they stay per-module.
- Editing a convention plugin invalidates the whole build cache.

## Alternatives considered

- **Copy-paste config per module.** Zero abstraction, maximal drift. Rejected.
- **A single god-plugin for all module types.** Couples libraries and feature modules that have genuinely different needs (navigation, Ktor). Rejected in favor of three focused plugins.
