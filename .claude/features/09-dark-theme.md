# 09 — Dark Theme & Material You

Status: IDLE
Group: C (sequence: 09→08)
Depends on: none

## Context

App likely has only light theme. Adding dark theme with Material You dynamic colors demonstrates modern Android design. Compose Multiplatform supports Material 3 theming cross-platform.

## Files to Create

- [ ] `shared/ui/.../theme/DarkColorScheme.kt` — dark color palette
- [ ] `shared/ui/.../theme/DynamicTheme.kt` — expect/actual for dynamic colors (Android) / static fallback (iOS)

## Files to Modify

- [ ] `shared/ui/.../theme/Theme.kt` — add dark theme support, `isSystemInDarkTheme()`
- [ ] `shared/ui/.../theme/Color.kt` — organize into light/dark palettes
- [ ] `shared/ui/.../theme/Type.kt` — ensure readability in dark mode (if contrast issues)
- [ ] `composeApp/.../AppContent.kt` — pass theme preference
- [ ] All Screen composables — replace hardcoded colors with `MaterialTheme.colorScheme.*`

### Screens to audit for hardcoded colors:
- [ ] `feature/home/.../HomeScreen.kt`
- [ ] `feature/cart/.../CartScreen.kt`
- [ ] `feature/details/.../DetailsScreen.kt`
- [ ] `feature/checkout/.../CheckoutScreen.kt`
- [ ] `feature/profile/.../ProfileScreen.kt`
- [ ] `shared/ui/.../components/*.kt` — all shared components

## Dependencies (libs)

None — Material 3 is already included via Compose Multiplatform.

## Implementation Steps

1. Audit existing `Color.kt` and `Theme.kt` for current color setup
2. Create `DarkColorScheme.kt` with dark palette following Material 3 guidelines:
   - Surface colors: dark backgrounds
   - On-surface: light text
   - Primary/Secondary: adjusted for dark backgrounds
   - Error colors: adjusted contrast
3. Create `DynamicTheme.kt`:
   - Android: `dynamicDarkColorScheme(context)` / `dynamicLightColorScheme(context)`
   - iOS: static fallback to defined palettes
   - `expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?`
4. Update `Theme.kt`:
   - Accept `darkTheme: Boolean = isSystemInDarkTheme()`
   - Priority: dynamic colors > custom dark/light > default Material
5. Audit all screens for hardcoded colors:
   - Replace `Color(0xFF...)` with `MaterialTheme.colorScheme.xxx`
   - Replace hardcoded backgrounds with `MaterialTheme.colorScheme.surface`
   - Ensure text uses `MaterialTheme.colorScheme.onSurface`
6. Test both themes in preview

## Verification

```bash
./gradlew :shared:ui:compileCommonMainKotlinMetadata
./gradlew assembleDebug

# Visual check: toggle system dark mode and verify
# Preview functions should show both light and dark
```

## Conflict Zones

- `shared/ui` theme files — runs before 08 (accessibility) in same group
- Color changes may affect component appearance checked by 08
