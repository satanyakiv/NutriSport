# 08 — Accessibility

Status: IDLE
Group: C (sequence: 09→08)
Depends on: 09-dark-theme

## Context

No accessibility support. For a portfolio project, proper `contentDescription`, `semantics`, role annotations, and TalkBack compatibility demonstrate quality engineering.

## Files to Create

- [ ] `shared/ui/.../util/Semantics.kt` — shared semantic constants and helpers

## Files to Modify

### All Screen Composables (add contentDescription, semantics)
- [ ] `feature/home/.../HomeScreen.kt`
- [ ] `feature/home/.../components/ProductCard.kt`
- [ ] `feature/cart/.../CartScreen.kt`
- [ ] `feature/cart/.../components/CartItemCard.kt`
- [ ] `feature/details/.../DetailsScreen.kt`
- [ ] `feature/checkout/.../CheckoutScreen.kt`
- [ ] `feature/profile/.../ProfileScreen.kt`
- [ ] `feature/adminPanel/.../AdminPanelScreen.kt`

### Shared UI Components
- [ ] `shared/ui/.../components/NutriSportButton.kt` — role, state description
- [ ] `shared/ui/.../components/NutriSportTextField.kt` — labels, error announcements
- [ ] `shared/ui/.../components/DisplayResult.kt` — live region for state changes
- [ ] `shared/ui/.../util/UiState.kt` — semantics extensions

## Dependencies (libs)

None — Compose Multiplatform semantics API is built-in.

## Implementation Steps

1. Create `Semantics.kt` with:
   - Test tag constants for shared components
   - Helper functions for common semantic patterns
   - Live region helpers for dynamic content
2. Update all interactive elements:
   - `contentDescription` on images and icons
   - `Role.Button` on clickable non-Button elements
   - `stateDescription` on toggles and selections
   - `heading()` semantics on section headers
3. Update `DisplayResult`:
   - `liveRegion = LiveRegionMode.Polite` for state changes
   - Announce loading/error states
4. Update text fields:
   - Proper `label` parameter
   - Error message announced via `error()` semantics
5. Ensure minimum touch targets (48dp) on all interactive elements
6. Add `testTag` to key elements for UI testing

## Verification

```bash
./gradlew assembleDebug
# Manual TalkBack testing on device
# UI tests can verify semantics:
# onNodeWithContentDescription("Add to cart").assertExists()
```

## Conflict Zones

- `shared/ui` components — also modified by 09 (dark theme), hence sequential
