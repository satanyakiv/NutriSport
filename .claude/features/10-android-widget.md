# 10 — Android Glance Widget

Status: IDLE
Group: A
Depends on: none

## Context

No home screen widget exists. A Glance widget showing cart item count demonstrates Android-specific platform integration alongside KMP shared code. Impressive for portfolio.

## Files to Create

- [ ] `androidApp/.../widget/CartWidget.kt` — GlanceAppWidget implementation
- [ ] `androidApp/.../widget/CartWidgetReceiver.kt` — GlanceAppWidgetReceiver
- [ ] `androidApp/.../widget/CartWidgetContent.kt` — widget UI composable
- [ ] `androidApp/src/main/res/xml/cart_widget_info.xml` — widget metadata
- [ ] `androidApp/src/main/res/drawable/widget_preview.xml` — preview image

## Files to Modify

- [ ] `androidApp/src/main/AndroidManifest.xml` — register widget receiver
- [ ] `gradle/libs.versions.toml` — add Glance dependency
- [ ] `androidApp/build.gradle.kts` — add Glance dependency

## Dependencies (libs)

- `androidx.glance:glance-appwidget:1.1.1` — official Jetpack Glance (Google official)
- `androidx.glance:glance-material3:1.1.1` — Material 3 theming for widgets

## Implementation Steps

1. Add Glance dependencies to version catalog and androidApp
2. Create `CartWidgetContent` composable:
   - Show cart item count (badge style)
   - "NutriSport" branding
   - Tap opens app to cart screen
   - Material 3 widget theming
3. Create `CartWidget` extending `GlanceAppWidget`:
   - Override `provideGlance()` to compose widget content
   - Read cart count from Room database (via Koin)
4. Create `CartWidgetReceiver`:
   - `override val glanceAppWidget = CartWidget()`
5. Create `cart_widget_info.xml`:
   - Min width/height: 2x1 cells
   - Preview image
   - Resize mode: horizontal + vertical
6. Register in `AndroidManifest.xml`:
   - `<receiver>` with `android.appwidget.action.APPWIDGET_UPDATE`
   - `<meta-data>` pointing to widget info XML
7. Add widget update trigger when cart changes (in CartRepository or via broadcast)

## Verification

```bash
./gradlew assembleDebug
# Install on device, long-press home screen, add widget
# Verify cart count updates when items added/removed
```

## Conflict Zones

- `libs.versions.toml` — also modified by 04, 05, 11
- All other files are Android-only, no conflicts with KMP features
