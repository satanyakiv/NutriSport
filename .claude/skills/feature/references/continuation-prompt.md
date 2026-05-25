# Continuation prompt — split-session handoff

Template for resuming a partial Phase B run in a fresh session. Used when context budget runs low mid-pipeline and the work needs to continue tomorrow / in a new conversation.

The closing turn of a partial session prints to chat:

1. **What landed** — commit hash + file list.
2. **What's pending** — continuation backlog grouped by skill step.
3. **A self-contained continuation prompt** built from the template below — the next session pastes this verbatim with no other context and resumes from the named pending step.
4. **Commit on the worktree branch** — no push without explicit user authorization. The commit message embeds the gates summary so `git log` is the durable audit trail.

The continuation session does NOT re-run Phase A. It picks up Phase B from the named pending step, follows the same contracts, and produces the same chat-console report at the end (no REPORT.md, no QA_CHECKLIST.md, no email).

## Template

Copy this block, fill the `{{...}}` placeholders, hand the whole thing to the next session as the first prompt.

```markdown
You are continuing Phase B of `/feature {{name}}` from a checkpoint commit. Your context starts empty — everything you need is in the files below.

## Working location

cd {{absolute-path-to-worktree}}

# e.g. /Users/.../NutriSport/.claude/worktrees/{{name}}

This is a git worktree of branch `{{branch}}` (already exists, do NOT recreate). Last commit `{{commit-hash}}` is the scaffolding checkpoint.

## Source of truth

1. **Plan**: `{{absolute-path-to-plan-file}}` — frontmatter (figmaFrames, useCases, flow.positive, flow.negative), step list, all architectural decisions from Phase A clarifications. READ FIRST.
2. **What landed / What's pending** — inlined below in this prompt (no REPORT.md file).
3. **Project rules**:
   - `CLAUDE.md`
   - `.claude/rules/architecture.md`
   - `.claude/rules/conventions.md` (Route/Screen separation, navigation-callback pattern, composable<Screen.X>)
   - `.claude/rules/testing.md`
   - `.claude/rules/fake-data.md`
   - `.claude/rules/models.md` (XxxToUiMapper pattern)
4. **Figma budget**: `.claude/skills/figma-mcp-budget/SKILL.md` — activate BEFORE any series of MCP calls. Pro plan: 200/day.

## What already landed (DO NOT redo)

{{summary-bullet-list-of-completed-steps}}

Compile chain green for: {{list-of-modules-that-compile}}.

## Pending backlog — execute in order

Each pending step. For every step, follow the contract in `.claude/skills/feature/SKILL.md` Phase B and the templates in `.claude/skills/feature/references/output-templates.md`.

### A. Tests (commonTest)

{{list-of-pending-tests-with-paths}}

### B. Platform actuals

{{pending-platform-implementations}}

### C. Shared:testing fakes

{{pending-test-doubles}}

### D. Real UI

{{pending-feature-module-files-grouped-by-screen}}

### E. Figma handoff

- File key: `{{figma-file-key}}`
- Frames (nodeIds, ~3 calls each):
  {{frame-list}}
- Activate `figma-mcp-budget` skill first.

### F. Quality gates (in order)

1. `./gradlew :feature:{{name}}:compileCommonMainKotlinMetadata` — hard
2. `./gradlew detekt` — hard
3. `./gradlew :feature:{{name}}:allTests` — hard
4. `./gradlew :feature:{{name}}:testDebugUnitTest` — hard
5. `./gradlew :androidApp:assembleDebug` — hard
6. ensure an emulator is up (`adb devices`; if none, `android emulator start <avd>` to boot — see `claude-in-mobile` skill) + `android run --apks=<apk>` — hard (soft-skip if no device)
7. `/codex:review` — soft

### G. Outputs

- Append `### {{name}}` section to `docs/E2E_BACKLOG.md` (created by the testing/CI track) from frontmatter `flow.positive` / `flow.negative` (snake_case test method names + 1-line description).
- Capture `android screen capture -o /tmp/cap.png && cwebp -q 75 /tmp/cap.png -o screenshots/<frame>__emulator__light.webp && rm /tmp/cap.png` per frame (and ensure Figma cached PNGs `screenshots/<frame>__figma.png` exist from per-screen verify).
- End-to-end visual fidelity check: load both `__figma.png` and `__emulator__light.webp` per frame, compare side-by-side, log discrepancies in the chat report's `## Visual fidelity` section.
- Print the FULL chat-console report per `references/output-templates.md` — gates table, what landed, attention required, manual QA, visual fidelity, screenshots paths, MCP budget.
- One git commit covering all additional work. The commit message embeds the gates summary so `git log` is the durable audit trail. NO push without user authorization.
- NO email draft, NO REPORT.md, NO QA_CHECKLIST.md, NO `.claude/reports/<date>-<name>.md` — chat console + commit message only.

## Contracts you must not break

- Screen — stateless. Route — stateful (`koinViewModel` + `collectAsStateWithLifecycle` + navigation callbacks). No `koinInject`/`koinViewModel`/`NavController` inside Screen.
- ViewModel exposes `state: StateFlow` + `onAction(action)`; navigation is invoked from the Route via callback lambdas the NavGraph wires. Never `NavController` in the Screen.
- Fake datasource always success (no random fail).
- `composable<Screen.X>()` for destinations, wired with navigation-callback lambdas per the existing NavGraph pattern.
- All committed artifacts in English. Ukrainian only in chat replies.

Start: read plan + rules + the inlined "what landed / pending" above. Resume from the first pending step in order. If something doesn't reconcile (Figma frame missing, contract mismatch, build failure not caught at scaffold time), HALT and report instead of writing broken code.
```

## Field reference

| Placeholder                                          | Source                                                                                       |
| ---------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| `{{name}}`                                           | plan frontmatter `name`                                                                      |
| `{{absolute-path-to-worktree}}`                      | `.claude/worktrees/{{name}}/`                                                                |
| `{{branch}}`                                         | plan frontmatter `branch` (existing branch from prior pipeline run, e.g. `feature/{{name}}`) |
| `{{absolute-path-to-plan-file}}`                     | `.claude/plans/feature-{{name}}.md` (or `~/.claude/plans/{{name}}-...md`)                    |
| `{{commit-hash}}`                                    | output of `git rev-parse HEAD` on the worktree                                               |
| `{{summary-bullet-list-of-completed-steps}}`         | inlined from the closing-turn `## What landed`                                               |
| `{{list-of-modules-that-compile}}`                   | from the gates table                                                                         |
| `{{list-of-pending-tests-with-paths}}`               | from `## What's pending → Tests`                                                             |
| `{{pending-platform-implementations}}`               | same → Platform actuals                                                                      |
| `{{pending-test-doubles}}`                           | same → Shared:testing                                                                        |
| `{{pending-feature-module-files-grouped-by-screen}}` | same → Real UI                                                                               |
| `{{frame-list}}`                                     | from plan frontmatter `figmaFrames` (only NOT-yet-handed-off entries)                        |
| `{{figma-file-key}}`                                 | constant `5r0fI3ij4KxETz5HNK5qfc` (NutriSport-Mobile-Yakiv)                                  |
| `{{date}}`                                           | today's date in `YYYY-MM-DD`                                                                 |

## When to use this

- Phase B context budget approaching 50% used.
- A hard-fail gate halted the run mid-scaffold.
- Multi-day work — commit at end of session, resume tomorrow with fresh budget.
- User explicitly asks for a handoff prompt to paste into a fresh session.

## When NOT to use this

- Single-frame quick task — just finish in one session.
- Phase A still incomplete — finish clarifications first, then start Phase B in the same session.
- Continuation in the SAME session — just keep going; no need for a prompt.

## Related

- [`SKILL.md`](../SKILL.md) — Runtime expectations + Continuation handoff sections
- [`plan-template.md`](plan-template.md) — frontmatter spec
- [`output-templates.md`](output-templates.md) — chat-console report format
