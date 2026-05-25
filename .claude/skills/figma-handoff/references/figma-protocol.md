# Figma Dev Mode MCP — Technical Reference

## Server identity

- Server: the `mcp__figma-dev-mode__*` Dev Mode MCP
- Tool prefix: `mcp__figma-dev-mode__<name>`
- Authenticated as: `yakiv.bondar@gmail.com`

## Tools

| Tool                                  | Purpose                                                                                                                      | Selection required |
| ------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- | ------------------ |
| `get_design_context(fileKey, nodeId)` | CMP-like code + screenshot + metadata. Main entry point.                                                                     | Yes                |
| `get_screenshot(fileKey, nodeId)`     | Image only. Cheaper, useful for quick context.                                                                               | No                 |
| `get_variable_defs(fileKey, nodeId)`  | Token values from Figma Variables. **SKIP by default — restore only if a specific frame is proven to use custom variables.** | Yes                |
| `get_metadata(fileKey, nodeId)`       | XML structure of nodes. Times out on large pages.                                                                            | No                 |

## Selection requirement

`get_design_context` and `get_variable_defs` only work when a layer is selected in Figma Desktop. If you receive `"You currently have nothing selected"`, ask the user:

> "Open file `NutriSport-Mobile-Yakiv` in Figma Desktop and click frame `<name>`. Tell me when selected — I'll re-run the request."

`get_screenshot` and `get_metadata` work without selection — use them first when you have a known `nodeId`.

## File key

Active file: `5r0fI3ij4KxETz5HNK5qfc` — file "NutriSport-Mobile-Yakiv". Single source of truth for design-system primitives AND app screens.

URL: `https://www.figma.com/design/5r0fI3ij4KxETz5HNK5qfc/NutriSport-Mobile-Yakiv`

Root page node: `0:1` (times out — too large). Other node IDs come from the URL when clicking a frame: `?node-id=X-Y` → pass as `"X:Y"` (dash → colon).

### Extracting file key from URL

- `https://www.figma.com/design/<FILEKEY>/Name?node-id=1-2` → fileKey is the segment after `/design/`
- `https://www.figma.com/file/<FILEKEY>/...` → same rule
- Branch files `https://www.figma.com/design/:fileKey/branch/:branchKey/...` → use `branchKey` as the fileKey, NOT the original

## Library context

The file uses:

- Material 3 (Google) for component patterns
- iOS / macOS system libraries for some components
- Inline styling on individual frames

**Implication:** when real values live as inline frame styles, not centralized variables, always pull `get_design_context` per-frame to get accurate hex/sp/dp values.

## Fonts in Figma

- Display: map to `BebasNeueFont()` in `:shared:ui`
- Body: map to `RobotoFont()` in `:shared:ui`
- Line heights: typically `+8..12sp` over font size

## Common pitfalls

- **`get_design_context` returns React or HTML** — Figma's code suggestions are framework-agnostic and often non-Compose. Treat them as a reference, not a target.
- **`get_metadata` times out on large pages** — drill down to a specific frame instead of starting from the page root.
- **Selection silently changes** — if the user clicks something else in Figma between calls, your `get_design_context` will return data for the new selection. Re-confirm if results look unexpected.
- **Branch files** — easy to miss. Always check the URL pattern; `/branch/<key>/` segment changes which file key to pass.
