# Methodology Router — When to Invoke Superpowers Skills

This file is a **user instruction** per the priority order in `superpowers:using-superpowers` itself ("User's explicit instructions ... highest priority"). It **replaces the default 1%-rule** ("if there's even a 1% chance a skill might apply, you ABSOLUTELY MUST invoke it") for the NutriSport project. Only invoke a superpower skill when a row in §1 matches; otherwise execute directly.

The goal: stop performing a Skill-tool check before every reply on a project where `.claude/rules/` and `.claude/features/NN-*.md` already enforce discipline.

## 1. Auto-invoke matrix

Strong, falsifiable signals only. If multiple rows fire, follow the priority order in `using-superpowers` (process > implementation).

| Signal in user message / repo state                                                                                       | Skill to invoke                              | Rationale                                                                                                                                                                                                                                    |
| ------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| User describes a new feature/component AND no `.claude/features/NN-*.md` plan exists for it                               | `superpowers:brainstorming`                  | Real exploration needed before code                                                                                                                                                                                                          |
| User hands a multi-step spec AND no plan file exists                                                                      | `superpowers:writing-plans`                  | Plan-mode artifact missing                                                                                                                                                                                                                   |
| User says "execute plan" / references `.claude/features/NN-*.md` with unchecked items                                     | `superpowers:executing-plans`                | Already planned, track checkpoints                                                                                                                                                                                                           |
| Test failure / unexpected runtime behavior AND root cause not obvious from the stack trace                                | `superpowers:systematic-debugging`           | Mystery bug, not a typo                                                                                                                                                                                                                      |
| About to write "done / merged / passing / ready" in a user-visible message, OR about to commit / open a PR                | `superpowers:verification-before-completion` | Evidence-before-assertion gate                                                                                                                                                                                                               |
| User asks for code review of own work, or "before I merge"                                                                | `superpowers:requesting-code-review`         | Pre-merge review surface                                                                                                                                                                                                                     |
| Editing code in `auth/`, payment/checkout paths, Firestore security rules, or any security-gated module                   | `superpowers:test-driven-development`        | Critical infra, test-first protects regressions                                                                                                                                                                                              |
| 2+ truly independent subtasks in the same turn (different modules, no shared state)                                       | `superpowers:dispatching-parallel-agents`    | Parallel `Agent` calls win                                                                                                                                                                                                                   |
| About to enter plan mode, OR user types `/plan` / "склади план"                                                           | `superpowers:dispatching-parallel-agents`    | Plan-mode work is research-heavy; fan out to parallel `Agent` calls by default. Fires in addition to brainstorming / writing-plans                                                                                                           |
| Starting a worktree-isolated branch from a `.claude/features/` plan                                                       | `superpowers:using-git-worktrees`            | Isolated workspace                                                                                                                                                                                                                           |
| Receiving code-review feedback (PR comments, codex output) before implementing changes                                    | `superpowers:receiving-code-review`          | Verify before performative compliance                                                                                                                                                                                                        |
| Implementation finished, tests green, decision pending on merge/PR/cleanup                                                | `superpowers:finishing-a-development-branch` | Structured wrap-up                                                                                                                                                                                                                           |
| About to `git commit` on the current branch, OR user says "закомить" / "commit" / "зроби коміт"                           | `codex-review`                               | Pre-commit independent second opinion (`--uncommitted` mode). Wait for output, verify each finding against `.claude/rules/*.md` (false-positive filter), fix or explicitly defer, THEN commit. Exclusion: `feature` skill is active (see §2) |
| User explicitly types `/superpowers:<name>` or "застосуй <skill>"                                                         | the named skill                              | Explicit user intent overrides everything                                                                                                                                                                                                    |
| User makes a non-trivial edit to `.claude/{hooks,skills,rules,settings*}`, `gradle/libs.versions.toml`, or `build-logic/` | `infra-weekly`                               | Real-time capture for the infrastructure portfolio docs                                                                                                                                                                                      |

## 2. Skip zones (no superpower skill — just execute)

Terminal. No second-guessing.

- A `.claude/features/NN-*.md` plan exists for the current task. Execute it; brainstorming / writing-plans already happened upstream.
- Mechanical refactor: rename, move file, extract function, bump dependency version, add a string resource, format / lint fixes, sort imports.
- Reading-only questions: "що робить ця функція", "де лежить X", "як працює Y".
- Tasks under an active project skill (`figma-handoff`, `r8-analyzer`, `feature`, `compose-screen-splitter`, `claude-in-mobile`, `dev-jump`, `firebase-ops`, `gen-test`, `new-feature`, `kover-analyze`, `orchestrate-features`, `replay-session`, `debug-crash-live`, `natural-docs`, and the `/fix` `/refactor` `/clean-arch` `/debug-deps` `/security-audit` `/debug-crash` commands). That skill owns the discipline; do not stack a superpower on top.
- **`feature` skill is the active orchestrator** — even if the next planned action is `git commit`, do NOT auto-invoke `codex-review`. The feature pipeline runs many commits; per-commit reviews would multiply wall-clock and burn the token budget. The §1 commit row applies only when `feature` is NOT the active skill.
- Single-line bug fix where the root cause is named in the stack trace.
- Documentation edits in `docs/` that don't introduce a new architecture decision (no ADR change).
- Configuration edits: `settings.json`, `.gitignore`, `gradle/libs.versions.toml` bumps without API-surface changes.
- User confirms an already-discussed approach ("так", "ок, роби", "плюс") and the plan is already on screen.
- Trivial chat: greetings, status questions, clarifications about prior output.

## 3. Always-on, project-orthogonal skills (matrix does NOT gate them)

These keep their own descriptions / triggers and fire independently:

- `interpreting-voice-input` — every Ukrainian/Russian message with dictation artifacts.
- `figma-handoff` — Figma URL or "use design from Figma".
- `r8-analyzer` — new library added, ProGuard/R8 questions, pre-release.
- `feature` — explicit `/feature` or a full-flow autonomous build request.
- `claude-in-mobile` — emulator / simulator screenshot / device-state requests.
- `infra-weekly` — `/infra-weekly`, "weekly infra review".
- NutriSport's own skills (`gen-test`, `new-feature`, `kover-analyze`, `orchestrate-features`, `replay-session`, `debug-crash-live`, `natural-docs`) fire on their own descriptions.
- `notebooklm`, `research`, `wrap-up`, `claude-api`, `anti-ai-slop-writing` — fire on their own descriptions.

## 4. Self-check before reply

Run silently before any non-trivial reply:

1. Does a row in §1 match? → invoke that skill, follow it exactly.
2. Does a row in §2 match? → execute directly, no Skill tool call.
3. Neither matches? → execute directly. **Default is action, not ceremony.**
4. About to claim work is complete / fixed / passing / merged? → §1 row 5 fires regardless of §2.

## 5. Maintenance

If a real session shows a wrong invocation (skill fired when it shouldn't have, or didn't fire when it should have), edit this file. Pure markdown, no rebuild. Add a row to §1 with a concrete signal, or an item to §2 with the explicit skip case.

## 6. Prompt caching policy

The repo's always-on rules surface (`CLAUDE.md` + `.claude/rules/*.md`) is loaded into every session and covered by Anthropic's `cache_control: ephemeral`. To keep it cacheable:

- **No per-session timestamps or conditional prose in rule bodies.** Stable text only; ephemeral state belongs in plan files or memory, not in cached rules.
- **Append new rules to the end of a section** instead of inserting in the middle, to preserve the cache prefix.
- Schedule template / skeleton edits together so cache invalidation happens once.

## Related

- `superpowers:using-superpowers` — the skill being overridden (its priority clause is the legal basis for this file).
- [plan-mode.md](plan-mode.md) — `.claude/features/NN-*.md` discipline this matrix relies on.
- [architecture.md](architecture.md) — module boundaries that define "critical infra" in §1.
- [testing.md](testing.md) — test pyramid that `test-driven-development` operates against.
- [bash-output.md](bash-output.md) — rtk allowlist + fallback policy.
