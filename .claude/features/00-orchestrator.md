# 00 — Orchestrator

Status: IDLE

## Parallel Groups

```
Group A (parallel, merge FIRST):     01-ci-cd, 03-readme, 10-android-widget
Group B (sequential 04→06→05→07):    offline-first → search → pagination → favorites
Group C (sequential 09→08):          dark-theme → accessibility
Group D (sequential 11→12):          performance → detekt-strict
Group E (LAST, after all):           02-tests
```

**Groups A, B, C, D run in parallel** (separate git worktrees).
Within B, C, D — sequential due to file conflicts.
Group E runs last — tests cover all new code.

## Merge Order

1. Merge Group A branches (no conflicts between them)
2. Merge Group B branch (rebased on updated main)
3. Merge Group C branch (rebased on updated main)
4. Merge Group D branch (rebased on updated main)
5. Merge Group E branch (covers everything)

## Conflict Matrix

| File | Features that modify it |
|------|------------------------|
| `database/.../NutriSportDatabase.kt` | 04, 06, 07 |
| `domain/.../domain/ProductRepository.kt` | 04, 05, 06 |
| `network/.../ProductRepositoryImpl.kt` | 04, 05, 06 |
| `database/.../dao/ProductDao.kt` | 05, 06 |
| `di/.../KoinModule.kt` | 04, 07 |
| `navigation/.../Screen.kt` | 07 |
| `navigation/.../SetupNavGraph.kt` | 07 |
| `shared/ui/.../theme/*` | 08, 09 |
| `detekt/config.yml` | 12 |
| `build-logic/convention/` plugins | 11 |
| `gradle/libs.versions.toml` | 04, 05, 10, 11 |
| `.github/workflows/` | 01 |
| `README.md` | 03 |
| `androidApp/.../widget/` | 10 |

## Worktree Commands

```bash
# Create worktrees for each group
git worktree add ../NutriSport-groupA -b feature/group-a
git worktree add ../NutriSport-groupB -b feature/group-b
git worktree add ../NutriSport-groupC -b feature/group-c
git worktree add ../NutriSport-groupD -b feature/group-d

# After all groups done, create tests branch from merged main
git worktree add ../NutriSport-groupE -b feature/group-e
```

## Status Tracking

| # | Feature | Group | Status | Branch |
|---|---------|-------|--------|--------|
| 01 | CI/CD | A | IDLE | `feature/ci-cd` |
| 02 | Tests | E | IDLE | `feature/tests` |
| 03 | README | A | IDLE | `feature/readme` |
| 04 | Offline-first | B | IDLE | `feature/offline-first` |
| 05 | Pagination | B | IDLE | `feature/pagination` |
| 06 | Search & Filtering | B | IDLE | `feature/search` |
| 07 | Favorites | B | IDLE | `feature/favorites` |
| 08 | Accessibility | C | IDLE | `feature/accessibility` |
| 09 | Dark Theme | C | IDLE | `feature/dark-theme` |
| 10 | Android Widget | A | IDLE | `feature/android-widget` |
| 11 | Performance | D | IDLE | `feature/performance` |
| 12 | Detekt Strict | D | IDLE | `feature/detekt-strict` |

## Agent Invocation Pattern

Each group runs as a Claude Code agent with `isolation: "worktree"`:

```
Agent(subagent_type="general-purpose", isolation="worktree", prompt="Implement features NN, NN...")
```

Groups A features can run in 3 parallel agents (no conflicts).
Groups B, C, D each run as 1 agent doing sequential features.
Group E waits for main to have all prior merges.
