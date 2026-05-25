---
name: infra-weekly
description: Weekly review of infrastructure changes for the NutriSport portfolio docs. Activates on "/infra-weekly", "/infra-review", "weekly infra", "тижневий ревʼю інфри", "що ми зробили цього тижня в інфрі", or any time the user mentions Sunday review of infra wins. Scans the last 7 days of git commits, diff'd files in .claude/{hooks,skills,rules,settings*}, gradle/libs.versions.toml, and build-logic/, then drafts portfolio entries against docs/infrastructure/templates/entry.md and updates docs/infrastructure/WEEKLY_LOG.md. User work: ~15 minutes confirming and editing drafts.
---

# infra-weekly — Sunday review of infrastructure portfolio

Drives the weekly review cadence for [`docs/infrastructure/`](../../../docs/infrastructure/). The directory exists so portfolio narratives ("how I shipped a KMP app solo in 3 months") are pre-written from real measured wins, not reconstructed from memory under interview pressure. This skill is what makes the weekly cadence actually happen.

## When to use

Activate when the user:

- Types `/infra-weekly` or `/infra-review`
- Asks "що ми зробили цього тижня в інфрі?", "тижневий ревʼю", "Sunday review", "weekly infra"
- Mentions wanting to "catch up on infra docs" or "fill in last week's portfolio entries"
- Closes a large infra change and says "let's add this to the portfolio"

Do NOT use for: single-entry additions outside Sunday cadence (just edit the topic file directly per [`docs/infrastructure/templates/entry.md`](../../../docs/infrastructure/templates/entry.md)). The skill is for the weekly sweep, not ad-hoc capture.

## Protocol (6 steps, ~15 min)

### Step 1 — Scan commits (read-only)

Run:

```bash
git log --since="7 days ago" --pretty="%h %s" --all
```

Note the commit count + SHA range. If the working tree has uncommitted infra changes, include them in the scan via `git diff --name-only` and `git status --short`.

### Step 2 — Identify infra-touching changes

For each commit, check if any file matches:

- `.claude/hooks/**`
- `.claude/skills/**`
- `.claude/rules/**`
- `.claude/settings*.json`
- `.claude/scripts/**`
- `.claude/features/**`
- `gradle/libs.versions.toml`
- `build-logic/**`
- `docs/infrastructure/**` (meta-changes to the system itself)

Build a candidate list in chat: "X commits touched infra files; Y look documentable".

### Step 3 — Confirm with user

Read the candidate list to user. For each candidate ask: "Document this in `<topic>.md`? (y/n/skip-and-defer)". Defer reasons: too early to measure, abandoned mid-week, trivial. Deferred items go to "Entries deferred" in `WEEKLY_LOG.md`.

### Step 4 — Draft entries

For each confirmed candidate:

1. Pick the topic file ([`token-economy.md`](../../../docs/infrastructure/token-economy.md), [`wall-clock-economy.md`](../../../docs/infrastructure/wall-clock-economy.md), [`reliability-and-safety.md`](../../../docs/infrastructure/reliability-and-safety.md), [`process-and-workflow.md`](../../../docs/infrastructure/process-and-workflow.md), [`ai-workforce.md`](../../../docs/infrastructure/ai-workforce.md), [`lessons-learned.md`](../../../docs/infrastructure/lessons-learned.md)).
2. Read [`templates/entry.md`](../../../docs/infrastructure/templates/entry.md).
3. Draft the entry by reading the diff and any associated plan / docs / commits. Fill all 6 fields: **Problem · Solution · Measurement · Status · Tradeoff · References**.
4. **Measurement is mandatory.** If you cannot find a number, write `qualitative only — to be measured by <date>` and add the entry to "Entries deferred" instead of the topic file.
5. Show the draft to user. Apply requested edits. Append as new H2 section to the topic file.

### Step 5 — Reflection prompts

Ask three questions, save answers verbatim into `WEEKLY_LOG.md`:

1. "Що цього тижня працювало особливо добре?"
2. "Що пробували і відкинули? Чому?"
3. "Що хочеш спробувати наступного тижня?"

These are not optional. Pattern: a portfolio without "what didn't work" reads as fabrication.

### Step 6 — Append to WEEKLY_LOG.md

Add a new H2 section to [`WEEKLY_LOG.md`](../../../docs/infrastructure/WEEKLY_LOG.md) using the skeleton at the bottom of that file. Fill in:

- Date (today, formatted `YYYY-MM-DD — Week N`)
- Commits scanned (count + SHA range, link to GitHub compare URL when both SHAs exist on `origin`)
- Modified infra files (counts per directory)
- Entries added (links to topic file H2 anchors)
- Entries deferred (one-liners + planned re-evaluate date)
- Three reflection answers from Step 5

## Anti-patterns

- **Adding entries without numbers.** Forces user back into "I think it was about 30%" territory. Defer instead.
- **Cramming a deferred entry into a topic file** because user "wants to keep momentum". Defer is for incomplete data, not laziness.
- **Skipping reflection prompts.** They are why this is a portfolio system, not a changelog.
- **Editing past WEEKLY_LOG entries.** Append-only. If a past entry was wrong, append a correction note to the next week's section.
- **Writing entries during the work week** (between Sundays). The whole point of cadence is batched review — real-time edits live in the topic file via the proactive `methodology.md` trigger, but `WEEKLY_LOG.md` only updates on Sunday.

## Cross-references

- [docs/infrastructure/README.md](../../../docs/infrastructure/README.md) — portfolio index
- [docs/infrastructure/templates/entry.md](../../../docs/infrastructure/templates/entry.md) — entry skeleton
- [.claude/rules/methodology.md](../../rules/methodology.md) §1 — proactive trigger row that captures candidates in real-time
- [.claude/rules/docs.md](../../rules/docs.md) — documentation style guide (English-only, no emojis)
- Existing global skill `wrap-up` (`~/.claude/skills/wrap-up/`) — model for "extract learnings from session". This skill is the project-specific sibling.

## Why this exists

Without `/infra-weekly`, `docs/infrastructure/` becomes another well-intended directory that decays. The skill makes the cadence concrete: 15 minutes on Sunday, with Claude doing the boring extraction work (diff parsing, commit-message reading, draft prose). Three reflection questions are cheap; their answers compound across months into actual portfolio narrative.
