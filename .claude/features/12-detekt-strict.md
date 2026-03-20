# 12 — Detekt Strict Mode

Status: IMPLEMENTED
Group: D (sequence: 11→12)
Depends on: 11-performance (NOTE: 11 is still IN_PROGRESS — Baseline Profile done, Compose stability pending. Detekt was implemented ahead of schedule since its changes don't conflict with the remaining @Immutable work.)

## Context

Currently `maxIssues: -1` (report only). Setting `maxIssues: 0` enforces zero violations. Must fix all existing violations first. Runs after performance (11) because `@Immutable` annotations may affect detekt rules.

## Files to Create

- [ ] `detekt/custom-rules.yml` — project-specific rule overrides (if needed)

## Files to Modify

- [ ] `detekt/config.yml` — set `maxIssues: 0`, tune rules for KMP/Compose
- [ ] All `.kt` files with violations — fix each violation

## Dependencies (libs)

None — detekt already configured.

## Implementation Steps

1. Run detekt to get current violation count:
   ```bash
   ./gradlew detekt 2>&1 | tail -20
   ```
2. Categorize violations by rule:
   - `complexity` — long methods, large classes
   - `style` — naming, formatting
   - `potential-bugs` — unreachable code, unused
   - `performance` — inefficient patterns
3. Fix violations in priority order:
   - **Auto-fixable**: formatting, import ordering → `./gradlew detekt --auto-correct`
   - **Style**: rename, restructure
   - **Complexity**: extract methods, split classes
   - **Suppress selectively**: only for false positives (e.g., Compose conventions like `@Composable` naming)
4. Add Compose-specific suppressions in config:
   - Allow PascalCase `@Composable` functions (not flagged as class names)
   - Allow `Modifier` parameter patterns
   - Allow `Preview` function naming
5. Set `maxIssues: 0` in config
6. Verify clean detekt run

## Verification

```bash
# Must pass with zero issues
./gradlew detekt

# Check config
grep "maxIssues" detekt/config.yml  # should show 0
```

## Conflict Zones

- `detekt/config.yml` — unique to this feature
- `.kt` files — changes are fixes (rename, extract), may conflict with any concurrent feature
- Must run after all other code changes (hence last in Group D)
