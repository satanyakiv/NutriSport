# Plan Mode Rules

## Feature Plan Files

Feature plans live in `.claude/features/` with numbered prefixes (`00-12`).

### Status Markers

Each plan file has a `Status` field in the header:

| Status | Meaning |
|--------|---------|
| `IDLE` | Not started |
| `IN_PROGRESS` | Currently being implemented |
| `IMPLEMENTED` | Done, verified, merged to main |
| `BLOCKED` | Waiting on dependency or decision |

### Template

```markdown
# {NN} — {Feature Name}

Status: IDLE
Group: {A|B|C|D|E}
Depends on: {list of prerequisite feature numbers, or "none"}

## Context
Why this feature exists and what's currently in place.

## Files to Create
- [ ] `path/to/NewFile.kt` — purpose

## Files to Modify
- [ ] `path/to/Existing.kt` — what changes

## Dependencies (libs)
- `group:artifact:version` — purpose (source: official / kmp-awesome)

## Implementation Steps
1. Step with detail
2. ...

## Verification
```bash
# commands to verify
```

## Conflict Zones
Files shared with other features (reference conflict matrix in 00-orchestrator.md).
```

### Rules

1. **One feature per file.** Never combine unrelated changes.
2. **Update status** when starting (`IN_PROGRESS`) and finishing (`IMPLEMENTED`).
3. **Check dependencies** — never start a feature whose prerequisite is not `IMPLEMENTED`.
4. **Checklist discipline** — tick off files as they are created/modified.
5. **Verification is mandatory** — run all listed commands before marking `IMPLEMENTED`.
6. **Conflict zones** — if two features share files, they MUST run sequentially (same group).
7. **Orchestrator** (`00-orchestrator.md`) defines parallel groups and merge order.
