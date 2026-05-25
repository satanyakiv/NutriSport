# Design System

NutriSport's design system lives in the `:shared:ui` module (`com.nutrisport.shared`). It is a flat, high-contrast brand: an electric yellow primary with an orange secondary on near-white surfaces. There is no custom `Theme` wrapper — composables read brand tokens directly and lean on `MaterialTheme` for the Material3 baseline. All tokens, fonts, and reusable components are defined once here and consumed by every feature module; features never hardcode colors, fonts, or one-off versions of shared components.

## Where it lives

```
shared/ui/src/commonMain/kotlin/com/nutrisport/shared/
  Colors.kt              — brand palette + semantic tokens
  Fonts.kt               — BebasNeueFont(), RobotoFont(), FontSize
  Alpha.kt               — alpha constants
  Resources.kt           — generated-resource helpers
  preview/Previews.kt    — NutriSportPreview() preview helper
  component/             — reusable composables (see table)
  domain/                — ProductCategoryColor (category → color mapping)
shared/ui/src/commonMain/composeResources/font/
  besas_neu_regular.ttf
  roboto_condensed_medium.ttf
```

Generated resources namespace: `nutrisport.shared.ui.generated.resources`.

## Palette

Defined in `Colors.kt`. No inline hex in composables — always reference a token.

| Token group | Tokens                                                                                                                              | Notes                                                         |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------- |
| Brand       | `Yellowish` (#EEFF00), `Orange` (#F24C00)                                                                                           | Primary CTA = Yellowish; secondary accent = Orange            |
| Neutrals    | `White`, `Black`, `Gray` (#F1F1F1), `GrayLighter` (#FAFAFA), `GrayDarker` (#EBEBEB), `Red` (#DD0000)                                |                                                               |
| Surface     | `Surface` (White), `SurfaceLighter`, `SurfaceDarker`, `SurfaceBrand` (Yellowish), `SurfaceSecondary` (Orange), `SurfaceError` (Red) | Screen and card backgrounds                                   |
| Border      | `BorderIdle` (GrayDarker), `BorderSecondary` (Orange), `BorderError` (Red)                                                          |                                                               |
| Text        | `TextPrimary` (Black), `TextSecondary` (Orange), `TextWhite`, `TextBrand` (Yellowish)                                               |                                                               |
| Button      | `ButtonPrimary` (Yellowish), `ButtonSecondary` (GrayDarker), `ButtonDisabled` (GrayDarker)                                          |                                                               |
| Icon        | `IconPrimary` (Black), `IconSecondary` (Orange), `IconWhite`                                                                        |                                                               |
| Category    | `CategoryYellow`, `CategoryBlue`, `CategoryGreen`, `CategoryPurple`, `CategoryRed`                                                  | Mapped per product category via `domain/ProductCategoryColor` |

## Typography

Defined in `Fonts.kt`. Two families, loaded from Compose resources:

| Family           | Composable        | Resource                      | Use                                              |
| ---------------- | ----------------- | ----------------------------- | ------------------------------------------------ |
| Bebas Neue       | `BebasNeueFont()` | `besas_neu_regular.ttf`       | Display / headings — condensed, bold brand voice |
| Roboto Condensed | `RobotoFont()`    | `roboto_condensed_medium.ttf` | Body / UI text                                   |

Sizes come from the `FontSize` object only — never raw `.sp`:

`EXTRA_SMALL` 10 · `SMALL` 12 · `REGULAR` 14 · `EXTRA_REGULAR` 16 · `MEDIUM` 18 · `EXTRA_MEDIUM` 20 · `LARGE` 30 · `EXTRA_LARGE` 40.

## Theme

There is **no** custom theme object. Composables apply brand tokens directly and use `MaterialTheme` for the Material3 baseline (shapes, default typography slots, ripple). This keeps the surface small; if a full theming layer is needed later, it would wrap `MaterialTheme` rather than replace the direct-token approach.

## Components

In `component/`. Reuse these before writing a one-off; if a Figma element maps to one, use it.

| Component                                          | Purpose                                       |
| -------------------------------------------------- | --------------------------------------------- |
| `PrimaryButton`                                    | Primary / secondary CTA button (brand fill)   |
| `CustomTextField`                                  | Standard text input                           |
| `AlertTextField`                                   | Text input with inline validation/alert state |
| `ProductCard`                                      | Product list/grid item                        |
| `QuantityCounter`                                  | +/- quantity stepper (cart)                   |
| `ProfileForm`                                      | Profile field group                           |
| `LoadingCard`                                      | Loading skeleton                              |
| `ErrorCard`                                        | Error state                                   |
| `InfoCard`                                         | Informational card                            |
| `OfflineBanner`                                    | No-network banner                             |
| `dialog/CategoriesDialog`                          | Category picker dialog                        |
| `dialog/CountryPickerDialog` + `CountryPickerItem` | Country picker dialog + row                   |

## Previews

Every public component and Screen gets `@Preview` functions wrapped in the `NutriSportPreview()` helper (`preview/Previews.kt`), per [`.claude/rules/preview.md`](../.claude/rules/preview.md). Previews live inline in the same file, fenced by `// region Previews` … `// endregion`.

## Network images

Remote images load through Coil 3 (used inside feature modules and `:shared:ui`). Local resources use `painterResource(...)` with `androidx.compose.foundation.Image`.

## Related

- [`.claude/rules/preview.md`](../.claude/rules/preview.md) — Preview discipline (inline, light + dark for Screens)
- [`.claude/rules/conventions.md`](../.claude/rules/conventions.md) — Compose conventions, WebP drawables, Route/Screen split
- [`.claude/skills/figma-handoff/SKILL.md`](../.claude/skills/figma-handoff/SKILL.md) — Figma → Compose handoff (maps Figma to these tokens/components)
