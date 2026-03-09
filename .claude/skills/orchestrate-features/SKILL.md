# orchestrate-features

Orchestrate feature implementation using parallel worktree agents.

## Usage

```
/orchestrate-features all           — run all groups (A, B, C, D, then E)
/orchestrate-features group:A       — run Group A features (01, 03, 10)
/orchestrate-features group:B       — run Group B features (04→06→05→07)
/orchestrate-features 01,03         — run specific features by number
/orchestrate-features status        — show status of all features
```

## Prompt

You are orchestrating NutriSport feature implementation. Read the orchestrator and feature files first:

1. Read `.claude/features/00-orchestrator.md` for parallel groups, merge order, and conflict matrix
2. Read `.claude/rules/plan-mode.md` for status conventions
3. Read the specific feature file(s) from `.claude/features/`

### For `all`:
Launch agents for Groups A, B, C, D in parallel (each in a worktree). Wait for all to complete. Then launch Group E (tests).

### For `group:X`:
Launch agent(s) for the specified group:
- **Group A**: Launch 3 parallel agents (01, 03, 10 — no conflicts)
- **Group B**: Launch 1 agent doing 04→06→05→07 sequentially
- **Group C**: Launch 1 agent doing 09→08 sequentially
- **Group D**: Launch 1 agent doing 11→12 sequentially
- **Group E**: Launch 1 agent doing 02 (tests for everything)

### For specific numbers (e.g., `01,03`):
Check conflict matrix. If no conflicts, launch in parallel worktrees. If conflicts exist, run sequentially.

### For `status`:
Read all feature files and report their Status field in a table.

### Agent pattern:
```
Agent(
  subagent_type="general-purpose",
  isolation="worktree",
  prompt="Implement feature NN. Read .claude/features/NN-name.md for full spec. Follow .claude/rules/ for conventions. Update feature file status to IN_PROGRESS at start and IMPLEMENTED when done."
)
```

### After completion:
1. Update feature file status to `IMPLEMENTED`
2. Report results: files created/modified, tests passing, build status
3. If merge needed, provide merge commands (do NOT auto-merge without user confirmation)

### Error handling:
- If a feature fails to compile: mark as `BLOCKED`, report error, continue with independent features
- If conflict detected: stop, report, ask user for resolution
