Read ~/.claude/skills/anti-ai-slop-writing/SKILL.md
Read ~/.claude/skills/anti-ai-slop-writing/references/banned-words.md
Read .claude/rules/docs.md

## Natural Docs Review

$ARGUMENTS

## Process

1. **READ** the target file or text provided above.

2. **SCAN** for AI writing patterns. Check each category:

   **Vocabulary:** any word or phrase from the banned list.

   **Structural tells:** rule-of-three groupings, uniform sentence length
   (three consecutive sentences of similar length), identical paragraph
   structure (topic-explanation-example-transition), hedging seesaw giving
   equal weight to both sides, excessive bullet points (more than 7 in a row).

   **Punctuation tells:** more than one em dash per section, exclamation marks
   used for enthusiasm, ellipses as transitions, underuse of semicolons and
   colons.

   **NutriSport-specific:** vague openers instead of concrete facts, marketing
   synonyms instead of codebase terminology, architecture described without
   real numbers, changelog entries with narrative, commit messages without
   the "why."

   **Pitch-specific** (when target is in `pitch/` directory):
   - EN technical (`PITCH.md`): hero framing, vague capability claims, missing
     measurable specifics, "passion project", hedged CTAs
   - UK business (`PITCH_UA_CLIENT.md`): tech jargon instead of client value,
     English loanwords, vague reassurance instead of concrete mechanisms

   Present findings in a table:

   | Line | Pattern | Found text | Category |
   | ---- | ------- | ---------- | -------- |

   If zero issues found, say so and stop.

3. **REWRITE** the full text with all patterns eliminated. Preserve every
   technical fact, link, code reference, and structural intent. Change only
   the language and structure.

4. **DIFF SUMMARY** -- brief list of what changed:
   - "Replaced [banned word] with [concrete alternative]"
   - "Broke rule-of-three grouping into four items"
   - "Varied sentence lengths in paragraph N"
   - "Replaced em dashes with semicolons in section N"
   - "Rewrote opener from value judgment to concrete fact"

   Keep the summary under 10 items. Group similar fixes.

## Rules

- Never mention the skill, the banned list, or these rules in the rewritten output
- Preserve all technical accuracy; never invent facts or numbers
- If the target follows .claude/rules/docs.md structure, keep that structure
- If the target is a commit message, output one line only
- If the target is a changelog, output facts-only entries
