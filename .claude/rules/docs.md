# Documentation Style Guide

Rules for writing docs in `docs/` directory. Extracted from existing docs (TESTING.md, CI.md, PERFORMANCE.md).

## Location & Language

- All docs live in `docs/` directory
- Language: **English** (not Ukrainian — unlike Claude responses)
- Filename: `SCREAMING_CASE.md` (e.g., `TESTING.md`, `CI.md`, `PERFORMANCE.md`)

## Document Structure

Every doc follows this skeleton in order:

````
# Title                           — short, 1-2 words
## Opening paragraph(s)           — what this covers, high-level approach (2-5 lines)
## Stack / Tools table            — | Tool | Version | Purpose |
## How It Works / Architecture    — ASCII diagram or table
## File / Module Structure        — ``` tree block with — descriptions
## Running / Commands             — ```bash blocks with # comments
## <Topic-specific sections>      — tables, code examples, explanations
## Not Covered (and Why)          — bulleted list: **Bold term** — reason
## Related                        — bullet links to other docs/plans
````

## Formatting Conventions

**Tables** — pipe-separated, left-aligned, dashes separator:

```
| Column A | Column B | Column C |
| -------- | -------- | -------- |
| value    | value    | value    |
```

**ASCII diagrams** — two styles:

- **Vertical flow**: `│`, `├─`, `└─`, `▼` for data/control flow
- **Horizontal pipeline**: `┌──┐`, `└──┘`, `────▶` for sequential pipelines
- **Box diagrams**: `┌───┐ │ │ └─┬─┘` for parallel paths

**Code blocks** — language-tagged:

- ` ```bash ` for commands
- ` ```kotlin ` for code examples
- ` ```toml ` for version catalog
- ` ``` ` (plain) for file trees and ASCII diagrams

**File trees** — 2-space indent, em dash for descriptions:

```
module/
  file.kt                          — description aligned with spaces
  subdir/
    another.kt                     — another description
```

**Emphasis:**

- **Bold** for key terms, important concepts, tool names in prose
- `backtick` for code references (classes, commands, paths, flags)
- `>` blockquotes for important warnings/notes
- Em dash (`—`) not hyphen for explanations after terms

## Style Rules

- **Tone:** factual, concise — "X does Y" not "X is designed to do Y"
- **Length:** ~150-170 lines per doc
- **Tense:** present for what exists, "planned" / "not yet implemented" for future
- **No emojis**
- **"Not Covered" section is mandatory** — each item: `**Bold name** — reason why excluded`
- **"Related" section is mandatory** — links to other `docs/` files and relevant `.claude/features/` plans
- **Versions from source of truth** — always read `gradle/libs.versions.toml`, never hardcode from memory
- **Commands use full Gradle paths** — `:module:task` not just `task`
- **Code examples reference real project code** — not hypothetical/generic

## Source of Truth Principle

Before writing ANY doc:

1. Read the actual source files being documented (build configs, Kotlin code, workflows)
2. Read `gradle/libs.versions.toml` for versions
3. Read existing docs (`docs/TESTING.md`, `docs/CI.md`, `docs/PERFORMANCE.md`) for style reference
4. Never assume — verify commands, file paths, class names from code

## Planned / Future Work Pattern

When documenting features that don't exist yet:

- Mark with `**Status:** NOT STARTED — tracked in <plan reference>`
- Use subsections: "What will change", "Dependencies to add", "Verification (when implemented)"
- Clearly separate from implemented content
