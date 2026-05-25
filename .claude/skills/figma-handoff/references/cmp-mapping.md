# Figma → Compose Multiplatform mapping

Quick reference for translating Figma frames to NutriSport's design system. Source of truth: the code itself — `shared/ui/.../com/nutrisport/shared/Colors.kt`, `Fonts.kt`, and `shared/ui/.../component/`.

## Colors

Color tokens live in `shared/ui/src/commonMain/kotlin/com/nutrisport/shared/Colors.kt`, package `com.nutrisport.shared`. Import the named token directly (e.g. `com.nutrisport.shared.Surface`). Prefer a `MaterialTheme.colorScheme.*` slot when one fits the semantic intent; otherwise use the named token.

### Brand

| Figma intent  | Token          |
| ------------- | -------------- |
| Primary brand | `Yellowish`    |
| Brand accent  | `Orange`       |
| Brand surface | `SurfaceBrand` |
| Text on brand | `TextBrand`    |

### Surfaces

| Figma intent           | Token              |
| ---------------------- | ------------------ |
| Page / card background | `Surface`          |
| Lighter surface        | `SurfaceLighter`   |
| Darker surface         | `SurfaceDarker`    |
| Secondary surface      | `SurfaceSecondary` |
| Error surface          | `SurfaceError`     |

### Borders

| Figma intent     | Token             |
| ---------------- | ----------------- |
| Idle border      | `BorderIdle`      |
| Error border     | `BorderError`     |
| Secondary border | `BorderSecondary` |

### Text

| Figma intent         | Token           |
| -------------------- | --------------- |
| Primary text         | `TextPrimary`   |
| Secondary text       | `TextSecondary` |
| Text on dark / white | `TextWhite`     |
| Text on brand        | `TextBrand`     |

### Buttons & Icons

| Figma intent     | Token             |
| ---------------- | ----------------- |
| Primary button   | `ButtonPrimary`   |
| Secondary button | `ButtonSecondary` |
| Disabled button  | `ButtonDisabled`  |
| Primary icon     | `IconPrimary`     |
| Secondary icon   | `IconSecondary`   |
| Icon on dark     | `IconWhite`       |

### Category accents

`CategoryYellow`, `CategoryBlue`, `CategoryGreen`, `CategoryPurple`, `CategoryRed` — use for per-category tinting.

**Rule:** never inline hex (`Color(0xFFAABBCC)`) in a Composable. Map every Figma hex to a named token, adding a new one to `Colors.kt` if nothing fits.

## Typography

Fonts live in `shared/ui/.../com/nutrisport/shared/Fonts.kt`.

| Figma role     | Font              | Size               |
| -------------- | ----------------- | ------------------ |
| Hero / display | `BebasNeueFont()` | EXTRA_LARGE (40)   |
| Large header   | `BebasNeueFont()` | LARGE (30)         |
| Section title  | `BebasNeueFont()` | EXTRA_MEDIUM (20)  |
| Page title     | `BebasNeueFont()` | MEDIUM (18)        |
| Subtitle       | `RobotoFont()`    | EXTRA_REGULAR (16) |
| Body           | `RobotoFont()`    | REGULAR (14)       |
| Caption        | `RobotoFont()`    | SMALL (12)         |

`FontSize` members: EXTRA_SMALL=10, SMALL=12, REGULAR=14, EXTRA_REGULAR=16, MEDIUM=18, EXTRA_MEDIUM=20, LARGE=30, EXTRA_LARGE=40.

Prefer `MaterialTheme.typography.*` slots where a Material slot fits the role.

## Components

| Figma element           | Code              |
| ----------------------- | ----------------- |
| Primary CTA button      | `PrimaryButton`   |
| Text input              | `CustomTextField` |
| Inline-alert text input | `AlertTextField`  |
| Loading skeleton        | `LoadingCard`     |
| Error state             | `ErrorCard`       |
| Info banner             | `InfoCard`        |
| No-network banner       | `OfflineBanner`   |
| Product tile            | `ProductCard`     |
| Profile fields          | `ProfileForm`     |
| Quantity stepper        | `QuantityCounter` |

## Icons

NutriSport uses Material `Icons.Default.X` directly in feature code. There is no icon-indirection layer.

Common ones:

- Back button → `Icons.Default.ArrowBack`
- Close → `Icons.Default.Close`
- Search → `Icons.Default.Search`
- Cart → `Icons.Default.ShoppingCart`

## Alpha

Use `Alpha.X` from `Alpha.kt` instead of inline floats.

## Shapes

Starting assumptions until confirmed via Figma:

- Cards: 12-16dp
- Buttons: 24-28dp (pill for primary)
- Inputs: 12dp

Confirm via `get_design_context` for the relevant frame.

## Spacing

No centralized spacing tokens yet — values come per-frame from `get_design_context`. Common observed values: 4, 8, 12, 16, 20, 24, 32, 40dp. If a project-wide spacing scale emerges, capture it as `Spacing.kt` in `:shared:ui`.
