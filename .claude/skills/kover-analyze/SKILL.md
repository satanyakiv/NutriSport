---
name: kover-analyze
description: Analyze Kover coverage report and provide prioritized recommendations. Use when user asks about test coverage, coverage gaps, what to test next, coverage analysis, or says "analyze coverage", "show coverage", "what needs tests". Also triggers on `/kover-analyze` with optional module filter argument.
disable-model-invocation: true
---

Analyze Kover coverage report. Filter: $ARGUMENTS

## Process

### Step 1 — Generate XML report

Run `./gradlew koverXmlReport` to produce a JaCoCo-format XML report.

The report location: `build/reports/kover/xml/report.xml`

If the build fails, use `/debug-deps` to diagnose.

### Step 2 — Parse XML report

Read `build/reports/kover/xml/report.xml`. For each `<class>` element, extract LINE coverage from the `<counter type="LINE">` element:

```
coverage% = covered / (covered + missed) * 100
```

If `$ARGUMENTS` contains a module filter (e.g., `:data`), only include classes whose package matches that module.

### Step 3 — Classify classes

Classify each class into one of these categories using pattern matching (check in order — first match wins):

| Category | Pattern | Testable? | Target |
|----------|---------|-----------|--------|
| Generated | `*.generated.resources.*`, `*BuildConfig*` | EXCLUDE | — |
| Screen/Preview | `*Screen*`, `*Preview*`, `*ComposableSingletons*` | EXCLUDE | — |
| DI/Navigation | `com.nutrisport.di.*`, `com.nutrisport.navigation.*` | EXCLUDE | — |
| Database | `com.nutrisport.database.*` | EXCLUDE | — |
| Analytics | `com.nutrisport.analytics.*` | EXCLUDE | — |
| Component | `*.component.*` | EXCLUDE | — |
| DTO | `*Dto`, `*.dto.*` | EXCLUDE | — |
| UI Model | `*Ui` (in `feature` package) | EXCLUDE | — |
| Domain Model | `*.shared.domain.*` (data class, no logic) | EXCLUDE | — |
| ViewModel | `*ViewModel*` | TEST | 80%+ |
| UseCase | `*UseCase*`, `*.usecase.*` | TEST | 90%+ |
| Repository | `*Repository*`, `*RepositoryImpl*` | TEST | 70%+ |
| Mapper | `*Mapper*`, `*.mapper.*` | TEST | 90%+ |
| Utils | `*.shared.util.*` | TEST | 90%+ |
| Other | everything else | REVIEW | — |

### Step 4 — Output report

Generate a report with 4 sections:

#### A) Summary

Table of testable categories with current vs target coverage:

```
| Category   | Classes | Current | Target | Status |
|------------|---------|---------|--------|--------|
| ViewModel  | 5       | 62%     | 80%    | BELOW  |
| UseCase    | 7       | 88%     | 90%    | BELOW  |
| Repository | 4       | 71%     | 70%    | OK     |
| Mapper     | 3       | 95%     | 90%    | OK     |
| Utils      | 2       | 85%     | 90%    | BELOW  |
```

#### B) Priority Targets (top 10)

List the 10 testable classes with the largest gap between current and target coverage. Sort by priority: Repository > ViewModel > Mapper > UseCase > Utils.

For each class show: name, category, current%, target%, gap, suggested action.

#### C) New excludes

List classes that Kover currently tracks but should be excluded (matched EXCLUDE categories above but NOT in the current excludes block in `build.gradle.kts:40-58`).

Group by category. Show the glob pattern needed to exclude them.

#### D) Ready `excludes` block

Merge existing excludes from `build.gradle.kts:40-58` with new patterns from section C. Output a complete `classes(...)` block ready to copy-paste into `build.gradle.kts`.

### Step 5 — Suggest action

Based on the report:
- If there are classes below target → suggest `/gen-test <ClassName>` for the top priority class
- If there are new excludes → suggest updating `build.gradle.kts` excludes block
- If all targets met → congratulate and suggest raising thresholds

## Key rules

- **XML, not HTML** — JaCoCo XML has stable schema with `missed`/`covered` counters
- **LINE coverage** — instruction coverage is inflated by Kotlin compiler; line = what developer sees
- **Read-only** — never modify `build.gradle.kts`, only recommend changes
- **Current excludes** are in `build.gradle.kts:40-58` — always merge with them, never replace
- Coverage targets come from `.claude/rules/testing.md`: ViewModel 80%, Repository 70%, Mapper 90%, Utils 90%
- UseCase target: 90% (pure logic)
