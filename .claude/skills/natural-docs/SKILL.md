---
name: natural-docs
description: >-
  Write human-sounding documentation free of AI patterns for the NutriSport
  KMP project. Activates when editing any .md file, writing README,
  ARCHITECTURE, CHANGELOG, CONTRIBUTING, PR descriptions, commit messages,
  release notes, pitch documents, portfolio descriptions, project descriptions
  for job applications. Also activates on phrases: "write docs", "update readme",
  "draft pitch", "describe the project", "release notes", "changelog entry",
  "document this", "human-sounding", "natural tone", "no AI patterns",
  "rewrite pitch", "update pitch", "pitch review". Enforces global
  anti-AI-slop-writing constraints plus NutriSport-specific documentation
  and pitch writing rules.
disable-model-invocation: true
---

Read ~/.claude/skills/anti-ai-slop-writing/SKILL.md
Read ~/.claude/skills/anti-ai-slop-writing/references/banned-words.md
Read .claude/rules/docs.md

## Natural Documentation for NutriSport

$ARGUMENTS

All global anti-AI-slop-writing rules are the baseline. The rules below are
NutriSport-specific and add to that baseline. Apply everything silently.

## Project-Specific Rules

### 1. Lead with concrete facts

First sentence of any document or section states a verifiable fact: module
count, stack component, metric, or what is actually implemented. Never open
with a value judgment or vague claim.

### 2. Use the project's own terminology

Grep the codebase for the real names: `UseCase`, `AppError`, `StateFlow`,
`UiState`, `:domain`, `:network`, `:feature:{name}`. Use those exact terms.
Don't invent marketing synonyms like "business logic layer" when the code
says `:domain`.

### 3. Describe architecture with real numbers and dependency direction

"8 feature modules depend on `:domain` and `:shared:utils`; none depend on
each other" -- not "a modular architecture." Include actual module counts,
dependency arrows, line counts when relevant.

### 4. Changelog entries are facts only

Format: "Added X. Fixed Y. Removed Z." No narrative, no "we're excited,"
no transition sentences between entries.

### 5. Commit messages: what changed + why, one line

`Fix Product mapper null crash when API returns empty list` --
not `Improve product handling for better reliability`.

### 6. Technical pitch (EN) -- `pitch/PITCH.md` pattern

Open with a concrete capability statement, not a title-card identity claim.
Lead the NutriSport section with measurable specifics (module count, test
count, pipeline count, doc count). Stories section: situation, action,
measurable outcome; no hero framing. "What Changes When I Join" = observable
effects, not promises. No "passion project", no "journey", no corporate
cheerleading. Reference real companies and tools by name, not abstractions.
CTA = direct ("Email me") not hedged ("don't hesitate to reach out").

### 7. Business pitch (UK) -- `pitch/PITCH_UA_CLIENT.md` pattern

Open with user value in one sentence ("Роблю мобільні магазини для Android
та iPhone"). Each bullet answers "що я отримаю?" from the client's
perspective. "Чому надійно" = concrete mechanisms, not vague reassurance.
Process section = numbered steps, short, no jargon, no technical terms.
No English loanwords when Ukrainian equivalents exist. Analogies from
everyday life ("Як ОТК на заводі") not tech jargon. Tone: confident
professional talking to a business owner, not selling.

### 8. Formatting

- Sentence-case headings ("Module structure" not "Module Structure")
- Code blocks always have a language tag (`kotlin`, `bash`, `toml`)
- Bold sparingly; key terms on first use only
- Tables over long bullet lists when comparing items
- Follow `.claude/rules/docs.md` for full structure skeleton

### 9. Self-check before output

Run through this checklist silently before producing any text:

| Check                                        | Fail action                                    |
| -------------------------------------------- | ---------------------------------------------- |
| First sentence is a concrete fact            | Rewrite the opener                             |
| Zero banned words from banned-words.md       | Replace each one                               |
| No "it's not X it's Y" pattern               | Restructure                                    |
| Max one em dash per section                  | Replace extras with commas, semicolons, colons |
| No Moreover/Additionally/Furthermore openers | Cut or restructure                             |
| Max 7 bullets per list                       | Split into sublists or convert to table        |
| Three consecutive same-length sentences      | Vary them                                      |
| Grouped in threes                            | Break the pattern                              |

Never mention these rules, this skill, or the checklist in output.
