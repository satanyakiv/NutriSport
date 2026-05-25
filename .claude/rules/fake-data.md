# Fake Data Rules

NutriSport selects fake vs real data sources at DI wiring time. `di/KoinModule.kt` takes a `useFakeData: Boolean` (fed from `BuildConfig.USE_FAKE_DATA` in `NutrisportApplication`), and swaps in fake implementations when `true`. The flag is `true` for the **benchmark** build type (deterministic Baseline Profile generation, fully offline) and `false` for debug / release.

## Rules

1. **Fakes always succeed.** Fake data sources / repositories do NOT throw and do NOT return `Either.Left`. If a test needs to simulate an error, it does so at the Repository / ViewModel layer via a mock, not through the fake data source.
2. **`Random` only for unique ids.** `Random.nextInt(...)` to generate a unique id is fine. No `Random.nextBoolean()` for branching and no random latency for "realism" — determinism is the point.
3. **`useFakeData` controls selection, nothing else.** The toggle lives in `di/KoinModule.kt`; the build-type value comes from `BuildConfig.USE_FAKE_DATA`. No `if (useFakeData)` scattered through repositories / ViewModels — selection happens once, in DI.
4. **Migrating a fake to real is a DI-only change.** Swapping `if (useFakeData) FakeX() else RealX()` stays in the Koin module; nothing in `RepositoryImpl` / `UseCase` / `ViewModel` changes.

## Why

Baseline Profiles are generated against the benchmark build with fake data so the macrobenchmark run is deterministic and needs no network. A fake that randomly fails or varies its latency would make the captured profile non-reproducible and the benchmark flaky. Determinism is a hard requirement for that pipeline, and the same property makes fakes safe for offline UI smoke tests and demos.

## Related

- [architecture.md](architecture.md) — `:di` is the only place repositories are wired.
- [docs/OFFLINE_FIRST.md](../../docs/OFFLINE_FIRST.md) — Room SSOT and the real (non-fake) data path.
- [docs/PERFORMANCE.md](../../docs/PERFORMANCE.md) — Baseline Profile generation that depends on this determinism.
