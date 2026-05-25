# Screen ↔ Figma frame map

Lookup table mapping each `Screen` route (sealed class in [`shared/utils/.../navigation/Screen.kt`](../../shared/utils/src/commonMain/kotlin/com/nutrisport/shared/navigation/Screen.kt)) to its Figma frame nodeId in the file `5r0fI3ij4KxETz5HNK5qfc` ("NutriSport-Mobile-Yakiv"). Consumed by the [`figma-handoff`](../skills/figma-handoff/SKILL.md) skill Step 1.5: when the user says "implement `Profile`", the skill resolves the nodeId from here instead of asking for a URL.

## Status

**Stub — nodeIds not yet captured.** This file is scaffolded ahead of the Figma frames being identified. Fill the `Content nodeId` column as each frame is located (open the frame in Figma Desktop, copy the URL `?node-id=X-Y`, convert `X-Y` → `X:Y`). Until a row has a real nodeId, the `figma-handoff` skill falls back to asking for a Figma URL.

## How to read

- **Screen route** — exact `Screen` subtype name as used in code and conversation (e.g. `Details`, `Checkout`).
- **Content nodeId** — the inner frame holding the screen body. Form `X:Y`. `TBD` until captured.
- **Notes** — route args, gating, or which frame variant to prefer.

## Map

| Screen route       | Content nodeId | Notes                                                |
| ------------------ | -------------- | ---------------------------------------------------- |
| `Auth`             | TBD            | Sign in / sign up entry (Google auth)                |
| `HomeGraph`        | TBD            | Bottom-nav host (drawer + products)                  |
| `ProductsOverview` | TBD            | Main product list (public landing)                   |
| `Details`          | TBD            | Product details; arg `id: String?`                   |
| `Categories`       | TBD            | Category grid                                        |
| `CategorySearch`   | TBD            | Filtered list; arg `category: String`                |
| `Cart`             | TBD            | Cart contents (auth-gated)                           |
| `Checkout`         | TBD            | Checkout; arg `totalAmount: Double` (auth-gated)     |
| `PaymentCompleted` | TBD            | Payment result; args `isSuccess`/`error`/`token`     |
| `Profile`          | TBD            | User profile (auth-gated)                            |
| `AdminPanel`       | TBD            | Admin product management (admin-gated)               |
| `ManageProduct`    | TBD            | Create/edit product; arg `id: String?` (admin-gated) |

## Related

- [`figma-handoff`](../skills/figma-handoff/SKILL.md) — consumer (Step 1.5 nodeId resolution)
- [`figma-mcp-budget`](../skills/figma-mcp-budget/SKILL.md) — Figma MCP rate-limit discipline
