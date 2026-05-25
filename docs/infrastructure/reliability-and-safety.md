# Reliability & safety

Guardrails that fail safe: hooks that never block real work, a hard stop on committing secrets, and deterministic fake data so demos and benchmarks can't flake. The principle is "a broken guardrail must not break the developer".

## 2026-05-25 — Fail-safe hooks (PostToolUse never blocks work)

**Problem.** A hook that errors or hangs can wedge the whole agent loop. A guardrail that breaks the workflow is worse than no guardrail.

**Solution.** The 5 PostToolUse hook scripts (`auto-append-memory`, `gradle-output-notice`, `navigation-compile-gate`, `skill-frontmatter-check`, plus the disabled `figma-budget-guard`) all exit 0 on any internal error — they advise, never block. The only intentional blocker is a PreToolUse guard on `Edit|Write` that refuses to touch `google-services.json`, `local.properties`, or `*.keystore` (`exit 1`). All scripts pass `bash -n` and are committed executable.

**Measurement.** 5 hook scripts + 1 inline sensitive-file guard; every script verified with `bash -n` and `test -x`. Blocking surface is exactly one matcher (sensitive files), by design.

**Status.** stable.

**Tradeoff.** Fail-safe means a genuinely-useful hook can silently no-op if its environment is wrong (e.g. `figma-budget-guard` is disabled because its env-var bypass doesn't propagate — see [lessons-learned.md](lessons-learned.md)). Advisory hooks trade enforcement for never-wedging.

**References.** [settings.json](../../.claude/settings.json) · [hooks dir](../../.claude/hooks/)

## 2026-04-01 — Deterministic fake-data path for demos and benchmarks

**Problem.** A demo or a Baseline-Profile run that hits live Firebase is non-deterministic — flaky for screenshots, useless for repeatable benchmarks.

**Solution.** `USE_FAKE_DATA` (BuildConfig) routes Koin to `FakeNetworkModule` (4 fake repos, 6 products, 1 customer, 2 cart items) from `:shared:testing`, skipping `Firebase.initialize()`. The `benchmark` build type sets it `true`; fakes always succeed (no random failures), so the happy path is always demonstrable.

**Measurement.** `qualitative` — deterministic by construction; no flaky-run rate to report because the path removes the network variable entirely.

**Status.** stable.

**Tradeoff.** Fake data can mask real integration bugs; it is strictly for demos/benchmarks, never the release path (`release` build type keeps `USE_FAKE_DATA=false`).

**References.** [Performance](../PERFORMANCE.md) · [Fake-data rule](../../.claude/rules/fake-data.md)
