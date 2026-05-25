# Wall-clock economy

Infrastructure that shortens the developer feedback loop — fewer seconds between "save" and "answer". The biggest wins on a solo KMP project come from not booting an emulator and from AOT-compiling hot paths.

## 2026-04-01 — Run the whole test suite on the JVM with Robolectric

**Problem.** `compose.uiTest` started in `commonTest`, but CMP resources and fonts need an Android context, so UI smoke tests could only run on a device/emulator — slow to boot, flaky, and awkward in CI.

**Solution.** UI smoke tests moved to `androidHostTest` with Robolectric (Android context on the JVM). Unit tests (domain, ViewModel, mapper) already ran on the JVM. The full suite is now one `./gradlew testAndroidHostTest`, no device.

**Measurement.** 180 `@Test` methods across 34 files complete in ~30s on the JVM with zero emulator boots. The same command runs in `pr.yml` and `debug.yml` on stock GitHub runners (no emulator image, no KVM).

**Status.** stable.

**Tradeoff.** Robolectric does not stub Firebase, so the network/data layer stays at 0% coverage — it would need heavy mocking or instrumented tests. Accepted for a portfolio app (see [TESTING.md](../TESTING.md) "Not Covered").

**References.** [Testing guide](../TESTING.md) · [CI](../CI.md)

## 2026-04-01 — Baseline Profiles from a deterministic fake-data build

**Problem.** Cold startup and first-scroll jank on a Compose app are death by a thousand interpreted methods. Measuring them against live Firebase data is non-deterministic.

**Solution.** A dedicated `:benchmark` module (`com.android.test` + `baselineprofile` plugin) drives `BaselineProfileGenerator` (cold start, home scroll, product detail, bottom-nav) against the `benchmark` build type, which sets `USE_FAKE_DATA=true` and swaps Firebase repos for in-memory fakes from `:shared:testing`. The generated `baseline-prof.txt` ships in `androidApp/src/release/generated/` and is applied by ProfileInstaller on first launch.

**Measurement.** Harness in place: `StartupBenchmarks` compares `None()` vs `Partial()` compilation, `StartupMode.COLD`, 5 iterations each. Device-run before→after numbers not yet captured (benchmarks need a physical device/emulator, not available on CI runners) — `qualitative only — to be measured on a device run`.

**Status.** experimental — harness stable, numbers pending a device run.

**Tradeoff.** Benchmarks cannot run in CI (no API 28+ device on GitHub runners), so regressions are not caught automatically. The fake-data set (6 products) is too small for meaningful scroll-jank metrics.

**References.** [Performance](../PERFORMANCE.md) · [Plan 11 — Performance](../../.claude/features/11-performance.md)
