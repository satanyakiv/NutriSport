# Media Budget — Claude Image Limits

Claude.ai (and the SDK underneath Claude Code) imposes a hard limit of **20 images / 30 MB total per thread**. Long sessions that mix Figma frames + emulator screenshots routinely hit this ceiling. This file is the single source of truth for the project-wide rules that prevent that.

## Hard limits (Claude product)

| Limit                       | Value                                                              |
| --------------------------- | ------------------------------------------------------------------ |
| Images per thread           | 20                                                                 |
| Total image size per thread | 30 MB                                                              |
| When the limit is reached   | the thread errors on the next image; cannot be cleared mid-session |

The ceiling is imposed by the Claude product on images **presented to the model in a thread**, independent of which CLI / MCP wrote the file the agent then `Read`s. Every emulator capture or Figma PNG the agent loads consumes one image slot, so the discipline below is identical regardless of transport.

**Compress before the agent reads it.** Raw PNG from a device capture is ~1.3 MB and saturates the 30 MB cap after ~22 captures (sooner once Figma PNGs are mixed in). WebP-compress before reading: `cwebp -q 75 /tmp/cap.png -o screenshots/<name>.webp` (measured ~13x saving). If the capture tool compresses internally, no extra step.

## Project rules

| #   | Rule                                                                           | Why                                                                                                                                                                   |
| --- | ------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | **≤10 screenshots per Claude session.**                                        | Leaves headroom for user-pasted screenshots and `Read` of cached PNGs later in the same thread.                                                                       |
| 2   | **Save Figma PNGs to disk on first fetch** (`screenshots/<frame>__figma.png`). | Re-using the file via `Read` does NOT re-fetch from Figma MCP, but each `Read` of an image still consumes one slot of the per-thread pool. Save once, Read sparingly. |
| 3   | **Emulator screenshots: one per screen, light mode only by default.**          | Dark mode capture only when the user explicitly asks or when a dark-only design drift is suspected.                                                                   |
| 4   | **Visual fidelity check reuses cached files.**                                 | Read `screenshots/<frame>__figma.png` and `screenshots/<frame>__emulator__light.webp`; do not re-fetch from Figma or re-capture from emulator.                        |
| 5   | **Skip `get_screenshot` when `get_design_context` already returns a PNG.**     | `get_design_context` includes a screenshot in its response. Calling `get_screenshot` immediately after for the same frame doubles the cost for no gain.               |
| 6   | **Long sessions checkpoint BEFORE the 10-image cap.**                          | Hand off to a fresh thread. The new thread resets the image pool.                                                                                                     |
| 7   | **No batch / bulk screenshot prefetch.**                                       | Spread captures across implementation time; bulk-prefetch defeats both the rate-limit and the image-budget protections.                                               |

## Where these rules are enforced

- **`figma-handoff` skill** — relies on the `get_design_context` PNG instead of a separate `get_screenshot` whenever possible.
- **`claude-in-mobile` skill** (emulator / simulator capture) — limit to one capture per screen per session.

## Anti-patterns

- Calling `get_screenshot` "just to confirm what a frame looks like" before deciding what to do. Wasteful, costs an image slot for no gain.
- Re-capturing the emulator after every UI change to "re-verify". Once per screen per session, then trust the diff.
- Capturing both light and dark of every screen "just in case". The user will ask if dark drift is suspected.

## Recovery when the limit is hit

1. Halt the current session immediately.
2. Commit any uncommitted work to the branch.
3. Print a continuation prompt with an explicit "image budget exhausted, restart in fresh session" note.
4. User opens a fresh Claude session and pastes the continuation prompt.

## Related

- [figma-mcp-budget SKILL](../skills/figma-mcp-budget/SKILL.md) — separate concern (Figma plan rate limit), pairs with this rule.
- [claude-in-mobile SKILL](../skills/claude-in-mobile/SKILL.md) — emulator / simulator capture transport.
