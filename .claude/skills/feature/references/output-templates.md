# Output template — chat console report

The `feature` pipeline writes ONE artifact: a single console-printed report at the end of Phase B Step 17. **No file is written, no email draft is created.** The chat IS the report; the commit message embeds the gates summary so `git log` is the durable audit trail.

## When to print

After Step 17 (final commit) the pipeline prints the full report block below in a single chat turn. If the run is partial (split across sessions per the continuation-prompt protocol), each closing turn prints the same template with `Status: partial` and a continuation backlog appended.

## Report skeleton

Print this block verbatim, filling placeholders:

```
[feature-pipeline YYYY-MM-DD] <name> — success|partial (X frames, Y unit tests, Z composition tests, K E2E backlog cases, N screenshots, M MCP calls)

## Gates
| # | Gate                | Status | Time   | Notes                                |
| - | ------------------- | ------ | ------ | ------------------------------------ |
| 1 | Compile             | ✓      | <1 s   | warm                                 |
| 2 | Detekt              | ✓      | <1 s   | 0 critical                           |
| 3 | Unit (Y/Y)          | ✓      | <1 s   | navigation-callback assertions match flow[] |
| 4 | Composition         | ✓      | <1 s   | Z screen tests rendered              |
| 5 | Assemble + install  | ✓      | 6 s    | emulator-5554                        |
| 6 | E2E backlog         | ✓      | <1 s   | section appended to docs/E2E_BACKLOG |
| 7 | Screenshots (N/N)   | ✓      | 30 s   | screenshots/<frame>__emulator__light.webp |
| 8 | Visual fidelity     | ✓      | <1 s   | per-frame diff vs screenshots/<frame>__figma.png |
| 9 | Codex review        | soft   | 30 s   | <verdict or "skipped: codex offline"> |

## What landed
- :feature:<name>/                     — 5-piece skeleton + PreviewData × X frames (Previews inline in Screen.kt)
- :domain/.../<name>/                  — models, repository interface, use cases (if scaffolded)
- :network/.../<name>/                 — DTOs, mappers, FakeXxxDataSource, RemoteXxxDataSource (if scaffolded)
- :shared:utils/.../navigation/Screen.kt — appended Screen.<X> entries
- :navigation/.../NavGraph.kt          — appended composable<Screen.X> entries
- :di/.../KoinModule.kt                — appended <name>FeatureModule
- docs/E2E_BACKLOG.md                  — ### <name> section

Branch: feature/<name> (commit <sha>, NOT pushed)

## Visual fidelity
- <Frame1>: PASS — emulator render matches Figma
- <Frame2>: drift — button bg should be ButtonPrimary, rendered as Surface (see screenshots/<frame>__{figma,emulator__light}.png)
- <Frame3>: PASS

## Attention required
1. **<top issue>** — <one-line summary, file path, recommended action>
2. **<...>** — ...
3. **<...>** — ...

(if more than 3 — list them all; do not truncate. If none — write "no blocking issues")

## Manual QA
- [ ] Positive: <brief checks per flow.positive[]>
- [ ] Negative: <brief checks per flow.negative[]>
- [ ] Neutral: <slow network, rotation, back-button>
- [ ] Design follow-ups: <items needing designer review>
- [ ] Navigation contract: Screen has no koinViewModel/koinInject, Route wires navigation callbacks, NavGraph uses composable<Screen.X>

## Screenshots
- screenshots/<frame1>__figma.png            — cached Figma frame (used by visual fidelity check)
- screenshots/<frame1>__emulator__light.webp  — emulator render
- ... (one pair per frame)

## MCP budget (informational)
- Total Figma calls: M (target ≤ 3 × X frames + 1 sanity = 3X+1)
- Burst: <max calls in 60s window — should be ≤ 3>
- 429 events: 0
```

## Continuation block (split-session runs only)

If the pipeline checkpoints mid-run, append this block AFTER the report skeleton above:

```
## Pending steps
- [ ] Step <N>: <brief description>
- [ ] Step <N+1>: <...>
- ...

## Continuation prompt
<paste the full self-contained prompt from references/continuation-prompt.md, with "what landed" / "pending steps" inlined>
```

The user copies the continuation prompt block into a fresh session as the first message. The next session resumes Phase B from the named pending step — no Phase A re-run.

## Commit message template

Step 17 also creates ONE commit on `feature/<name>`. The commit message embeds the gates summary verbatim so `git log` is the durable audit trail. Format:

```
feat(<name>): scaffold <X> screens — <name> flow

Gates: compile ✓ | detekt ✓ | unit Y/Y ✓ | composition Z/Z ✓ | install ✓ | E2E backlog ✓ | screenshots N/N | fidelity ✓ | codex <verdict>

Frames: <Frame1>, <Frame2>, ...
Branch: feature/<name>
MCP calls: M (Figma) · CLI captures: N (claude-in-mobile)

Visual fidelity: <PASS for all | drift on <FrameX>>

Attention required:
1. <top issue>
2. ...

Plan: .claude/plans/feature-<name>.md
```

`git log feature/<name>` after the run shows the same gates summary that Step 17 printed to chat — this is the durable record. No separate file artifact.

## Do not

- Do NOT call any email/Gmail draft tool
- Do NOT create `REPORT.md`, `QA_CHECKLIST.md`, or any file under `.claude/reports/`
- Do NOT save the report to `.claude/worktrees/<name>/REPORT.md` — that file is no longer part of the workflow
- Do NOT push the branch without explicit user authorization (project safety rule — applies to every commit, not just this one)
