# Refactor recipes

Five archetype recipes anchored to real NutriSport screens. Each recipe shows the split pattern for a class of Screen, the proposed destinations per composable, and the expected `XxxScreen.kt` shape after the split. The skill uses these as templates, not as commitments.

**No NutriSport Screen currently breaches the threshold** — the longest is ~173 lines and every feature already uses a sibling `component/` directory. These recipes are preventive: they show what a split would look like _when_ a Screen grows, and they anchor on screens that already demonstrate the target structure. Re-verify any size claim with `grep -c "^@Composable"` and `wc -l` before relying on it.

---

## Recipe 1 — Index / dashboard Screen

**Anchor:** [`feature/profile/.../profile/ProfileScreen.kt`](../../../../feature/profile/src/commonMain/kotlin/com/nutrisport/profile/ProfileScreen.kt) (153 lines today — healthy). Use this archetype when a profile/dashboard Screen accumulates a header block, several settings/quick-action blocks, and legal/footer links until it crosses the threshold.

**Split pattern (Balanced variant):**

| New file                                                | Destination   | Source composables                   | Why                                            |
| ------------------------------------------------------- | ------------- | ------------------------------------ | ---------------------------------------------- |
| `feature/profile/.../component/ProfileHeaderBlock.kt`   | feature-local | header with avatar + name            | 30+ lines, screen-only                         |
| `feature/profile/.../component/ProfileSettingsBlock.kt` | feature-local | settings rows + their row composable | Same row layout; keep the group together       |
| `feature/profile/.../component/ProfileLegalBlock.kt`    | feature-local | legal/version links                  | small but unique to Profile                    |
| `:shared:ui/.../component/SectionHeading.kt`            | shared-UI     | generic section heading              | only if the same heading appears on ≥2 Screens |

`ProfileScreen.kt` after split: Screen + Content + block invocations + the inline `// region Previews`.

**Don't extract:** the `Content` layer stays in `ProfileScreen.kt` (Screen↔Content split kept intact, project Route↔Screen contract).

---

## Recipe 2 — Long form Screen

**Anchor:** [`feature/adminPanel/manageProduct/.../ManageProductScreen.kt`](../../../../feature/adminPanel/manageProduct/src/commonMain/kotlin/com/nutrisport/manage_product/ManageProductScreen.kt) (173 lines). This screen **already demonstrates the target structure** — its `component/` sibling holds `ProductFormFields.kt`, `ProductSwitchRow.kt`, `ThumbnailUploader.kt`, `ManageProductTopBar.kt`. Use it as the reference for any form-heavy Screen.

**Split pattern (Balanced):**

| New file                                       | Destination   | Source                                            | Why                                                 |
| ---------------------------------------------- | ------------- | ------------------------------------------------- | --------------------------------------------------- |
| `feature/<x>/.../component/<X>FormFields.kt`   | feature-local | the field-by-field column body                    | the bulk of a form, extracted as one cohesive block |
| `feature/<x>/.../component/<X>PickerDialog.kt` | feature-local | any Material3 picker dialog + its date/time utils | self-contained dialog + its own utilities           |
| `feature/<x>/.../component/<X>TopBar.kt`       | feature-local | screen-specific top bar                           | screen-only                                         |
| Stay inline                                    | —             | the Screen body that wires fields together        | extracting per-field would be over-extraction       |

**Note:** a form Screen that hosts editable text needs `Modifier.imePadding()`; preserve it when extracting any sub-section that owns text inputs.

---

## Recipe 3 — List Screen with reuse candidate

**Anchor:** [`feature/home/productsOverview/.../ProductsOverviewScreen.kt`](../../../../feature/home/productsOverview/src/commonMain/kotlin/com/nutrisport/products_overview/ProductsOverviewScreen.kt) (145 lines) — already extracts `MainProductCard.kt` into its `component/` dir. Same pattern in `feature/home/cart` (`CartItemCard.kt`).

**Split pattern (Balanced):**

| New file                                     | Destination                        | Source                          | Why                                                                                   |
| -------------------------------------------- | ---------------------------------- | ------------------------------- | ------------------------------------------------------------------------------------- |
| `feature/<x>/.../component/<X>TopBar.kt`     | feature-local                      | top bar (filters/title)         | screen-specific                                                                       |
| `feature/<x>/.../component/<Item>Card.kt`    | feature-local                      | the list item card              | screen-only unless reused                                                             |
| `:core:<name>/.../component/<Item>Card.kt`   | **`:core:<name>` (cross-feature)** | item card reused by ≥2 features | only if a second feature needs the same card. NutriSport has no `:core:*` module yet. |
| `feature/<x>/.../component/<X>EmptyState.kt` | feature-local                      | empty + error states            | keep co-located with their use site                                                   |

**Bootstrap note:** NutriSport has no `:core:*` module yet (the cross-feature track is defined in [`architecture.md`](../../../rules/architecture.md) Rule 12). The skill MUST NOT create a new `:core:*` module on its own. If a card genuinely needs cross-feature reuse and the user approves the bootstrap, follow the convention-plugin pattern. Otherwise fall back to feature-local extraction and leave a `// TODO(:core:<name>)` note.

---

## Recipe 4 — Detail Screen with many sections

**Anchor:** [`feature/details/.../DetailsScreen.kt`](../../../../feature/details/src/commonMain/kotlin/com/nutrisport/details/DetailsScreen.kt) (169 lines). This screen **already demonstrates the target structure** — its `component/` sibling holds `DetailsTopBar.kt`, `FlavorChip.kt`, `PriceChangeBanner.kt`, `ProductDetailsContent.kt`, `ReconnectedPrompt.kt`. Use it as the reference for any detail Screen with multiple sections.

**Split pattern (Balanced):**

| New file                                                            | Destination   | Source                              | Why                                    |
| ------------------------------------------------------------------- | ------------- | ----------------------------------- | -------------------------------------- |
| `feature/<x>/.../component/<X>TopBar.kt`                            | feature-local | top bar                             | screen-specific                        |
| `feature/<x>/.../component/<X>Content.kt`                           | feature-local | the section-by-section Content body | the layout backbone                    |
| `feature/<x>/.../component/<Section>Chip.kt` / `<Section>Banner.kt` | feature-local | each distinct section widget        | section + its row pair travel together |
| `:shared:ui/.../component/SectionHeading.kt`                        | shared-UI     | generic section heading             | only if shared across ≥2 Screens       |

`DetailsScreen.kt` stays the thin Screen entry + inline Previews.

---

## Recipe 5 — Already-good example (do not refactor)

**Anchor:** [`feature/details/.../DetailsScreen.kt`](../../../../feature/details/src/commonMain/kotlin/com/nutrisport/details/DetailsScreen.kt) — 169 lines with a 5-file `component/` sibling.

This is the project's **"good already" reference**. The skill auto-skips a Screen under the rule: _"Auto-skip: Screen has a sibling `component/` directory with ≥4 files AND <300 lines"_. DetailsScreen qualifies. The skill should:

1. Audit normally.
2. Note that the `component/` track exists and is being used correctly (sections extracted, not a `Components.kt` bag).
3. Suggest the next candidate only if line count keeps growing.
4. Do NOT propose a destructive split. The Screen is healthy.

This recipe exists to teach the skill that **structure already in place is positive evidence, not a target**. Reward the codebase, do not refactor it for the sake of refactoring.

---

## Cross-feature dedup (when it appears)

When the same visual primitive (separator, section heading, status badge with no domain types in its signature) starts appearing across two or more feature Screens, it is a `:shared:ui` candidate, not a per-feature copy. This is the article's anti-pattern §7.5 ("copy-paste reuse") and violates [`models.md`](../../../rules/models.md) Rule 8.

Recommended consolidation when it occurs:

1. New file `:shared:ui/.../component/<Primitive>.kt` with `internal fun`s for the shared primitives.
2. Migrate the Screens to import from `:shared:ui` and delete the local copies.
3. Add Preview functions per [`preview.md`](../../../rules/preview.md) (light-only for components).
4. Run `./gradlew :shared:ui:compileAndroidMain` then the affected `:feature:*:compileAndroidMain` tasks.

NutriSport does not currently have this duplication — its primitives already live in `:shared:ui/.../component/` (`PrimaryButton`, `CustomTextField`, `ProductCard`, `LoadingCard`, `ErrorCard`, …). The skill proposes consolidation only if a real duplicate surfaces during an audit, and executes it only after the user confirms the cross-feature edit.
