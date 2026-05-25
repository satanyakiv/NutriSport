# Entry template

Copy this skeleton, paste it as a new H2 section into the right topic file (`token-economy.md`, `wall-clock-economy.md`, etc.). Date format: `YYYY-MM-DD`.

```markdown
## YYYY-MM-DD — <Short title>

**Problem.** What hurt. 1–2 sentences, concrete.

**Solution.** What was done. 2–4 sentences. Reference the actual files / commands / tools, not abstractions.

**Measurement.** Numbers. Before → after. How many sessions / runs / changes. If you have no number, write `qualitative only` — that's a flag to come back and measure.

**Status.** `experimental` | `stable` | `abandoned` (with reason for the latter two).

**Tradeoff.** What got worse, what is hidden, what needs re-checking later.

**References.** [Plan](../../.claude/features/NN-…) · [Docs](../X.md) · [Commits](https://github.com/satanyakiv/NutriSport/compare/SHA…SHA) · [Memory](~/.claude/projects/-Users-…/file.md)
```

## Field rules

- **Title.** Action phrase, ≤60 chars. "Move UI smoke tests to Robolectric on JVM", not "tests".
- **Problem first.** Without a problem statement the entry is just "we did X" — useless for portfolio narrative.
- **Measurement is mandatory.** No exceptions. If you have nothing yet, write `qualitative only — to be measured by <date>`.
- **Status `abandoned`** must include the reason. Future-you will not remember.
- **Tradeoff field is non-negotiable.** Every infra change has one; if you can't name it you didn't think hard enough.
- **References must use links** (markdown `[label](path)`), not bare paths. Portfolio readers click.

## Example (real)

```markdown
## 2026-04-01 — Run the whole test suite on the JVM with Robolectric

**Problem.** `compose.uiTest` was in `commonTest`, but CMP resources and fonts need an Android context, so UI smoke tests could only run on a device/emulator — slow, flaky, and impossible in plain CI.

**Solution.** Moved UI smoke tests to `androidHostTest` with Robolectric so they get an Android context while still running on the JVM. Unit tests (domain, ViewModel, mapper) already ran on the JVM. The full suite is now a single `./gradlew testAndroidHostTest` with no device.

**Measurement.** 180 `@Test` methods across 34 files run in ~30s on the JVM, zero emulator boot. Kover aggregate 37.4% (tested packages average 80%+: `domain:usecase` 98.8%, `feature:productsOverview` 90.3%).

**Status.** stable.

**Tradeoff.** Network/data layer (Firebase repos) stays at 0% — Robolectric does not stub Firebase, so that layer needs either heavy mocking or instrumented tests. Accepted: diminishing returns for a portfolio app.

**References.** [Testing guide](../TESTING.md) · [CI](../CI.md)
```
