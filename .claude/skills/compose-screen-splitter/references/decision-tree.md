# Decision tree — where does this composable belong?

For each composable found in Step 1 of the protocol, walk this tree top-to-bottom. The first branch that matches is the destination. If two branches feel correct, pick the one that gives the smaller blast radius (`feature/<x>/component/` over `:core:`, `:core:` over `:shared:ui`).

```
Composable to extract?
│
├─[A]─ Used by 2+ existing :feature:* modules?
│       │
│       ├─ YES ─→ :core:<name>/.../component/Xxx.kt
│       │         (per architecture.md Rule 12)
│       │         BLOCKED if :core:<name> doesn't exist yet — abort and tell user
│       │
│       └─ NO  ─→ continue to [B]
│
├─[B]─ Generic visual primitive? (no domain-shape parameters,
│      e.g. takes only String/Int/Modifier/Color/Painter)
│       │
│       ├─ YES ──┬─ Already in :shared:ui/component/?
│       │        │   ├─ YES ─→ REUSE — delete the local copy, import existing
│       │        │   └─ NO  ─→ Will it be reused project-wide? (check by:
│       │        │              grep similar shape across feature/**/*Screen.kt)
│       │        │              ├─ YES ─→ :shared:ui/.../component/Xxx.kt
│       │        │              └─ NO  ─→ continue to [C]
│       │
│       └─ NO  ─→ continue to [C]
│
├─[C]─ Used only inside this feature module?
│       │
│       ├─ Reusable across screens of THIS feature?
│       │   (e.g. Cart uses it on CartScreen + CheckoutScreen)
│       │   ├─ YES ─→ feature/<x>/.../component/Xxx.kt
│       │   │         (sibling of all screen subdirs)
│       │   │
│       │   └─ NO ─→ continue to [D]
│       │
│       └─ continue to [D]
│
└─[D]─ Used only by this one Screen?
        │
        ├─ Has its own state (remember{}, mutableStateOf, internal animation)?
        │   ├─ YES ─→ feature/<x>/.../<screen>/component/Xxx.kt
        │   │         (sibling of XxxScreen.kt)
        │   │
        │   └─ NO ─→ continue
        │
        ├─ ≥15 lines of body?
        │   ├─ YES ─→ feature/<x>/.../<screen>/component/Xxx.kt
        │   └─ NO  ─→ continue
        │
        └─ <15 lines, single-use, stateless ─→ LEAVE INLINE
                                                (article anti-pattern §7.2:
                                                 over-extraction)
```

## Worked examples (anchored on current NutriSport code)

### Example 1 — `MainProductCard` (in `ProductsOverviewScreen.kt`)

Walk:

- [A] Used by `ProductsOverviewScreen` only currently. **No.**
- [B] Signature takes a `Product` (domain-shaped type). **NOT a generic primitive.**
- [C] Inside `:feature:home:productsOverview` only. Used on multiple home screens? Not yet. **No.**
- [D] Used only by `ProductsOverviewScreen`. ≥15 lines? Yes. **Destination: `feature/home/productsOverview/.../component/MainProductCard.kt`.**

This is exactly where it already lives — a clean real-world result of the tree.

Note: if a second feature (e.g. a wishlist) starts rendering the same card, re-walk → [A] becomes YES → migrate to `:core:<name>` (after bootstrapping that module, which NutriSport does not have yet).

### Example 2 — `FlavorChip` (in `DetailsScreen.kt`)

Walk:

- [A] Used by `DetailsScreen` only. **No.**
- [B] Signature takes a `String` label + `Boolean` selected — close to a generic primitive, but it carries Details-specific styling. Treat as feature-shaped.
- Reused project-wide? `grep -rn "FlavorChip"` → only Details. **No.**
- [C] Used by other Details screens? No.
- [D] Single-Screen, ≥15 lines. **Destination: `feature/details/.../component/FlavorChip.kt`.**

Again, this matches where it already lives.

### Example 3 — `SectionHeading` (hypothetical shared primitive)

Walk:

- [A] If the same heading appeared on `ProfileScreen` + `DetailsScreen` → 2 features. **YES.**
- BUT [B] also matches: signature is `@Composable fun SectionHeading(text: String)` — no domain types — generic visual primitive. **YES.**

Two branches matched. Tie-breaker rule: pick smaller blast radius. `:shared:ui` is the smallest for a true primitive (no `:core:*` module needed, just an import). **Destination: `:shared:ui/.../component/SectionHeading.kt`.** This is the consolidation move — verify with `grep -r "fun SectionHeading" shared/ui` before creating it.

### Example 4 — `PriceChangeBanner` (in `DetailsScreen.kt`)

Walk:

- [A] Used only by `DetailsScreen`. **No.**
- [B] Carries Details-specific price/state logic — domain-shaped. **No.**
- [C] Used by other Details screens? No.
- [D] Single-Screen, ≥15 lines, has its own state (banner visibility). **Destination: `feature/details/.../component/PriceChangeBanner.kt`.**

### Example 5 — a 6-line label/value row inside a section

Walk:

- [A] Used by one Screen only. **No.**
- [B] Signature: `(label: String, value: String, isBold: Boolean)` — generic. **YES.**
- Already in `:shared:ui/.../component/`? Check before extracting.
- Reused project-wide? `grep -rn "LabelValueRow"` → only here. **No.**
- → continue to [C]/[D]
- [D] <15 lines, single-use, stateless. **LEAVE INLINE.**

This is the article's "over-extraction" guard in action: a 6-line label/value row used only by one section does not earn its own file. It stays inline inside the extracted section file as a `private fun`.

### Example 6 — `Spacer(modifier = Modifier.height(8.dp))`

Not even a named composable — direct stdlib call. Never extract a `Spacer` wrapper into a file like `OrderSpacer.kt`. The article's anti-pattern §7.2 explicitly flags this kind of over-extraction. If a 5-line file would only ever wrap a `Spacer` — refuse and tell the user why.

## Edge case — "leave separators local for now"

Sometimes the user explicitly opts out of the cross-feature dedup move (e.g., wants to ship a Screen split today and defer the `:shared:ui` consolidation to a separate PR). When that happens with a separator/heading composable currently `private` in the Screen file:

- **Promote visibility from `private` to `internal`** so the extracted `component/*.kt` files in the same module can import it.
- **Keep the composable inside `XxxScreen.kt`** (don't move to `component/`); it stays a single-file primitive.
- **Add a TODO comment** with the migration target: `// TODO(:shared:ui) — consolidate cross-feature; see refactor-recipes.md "Cross-feature dedup task"`.
- **Track the dedup work** as a separate item (a TODO in the splitter's audit report or a tracked task) — do not lose the signal.

This isn't a deviation from the decision tree; it's a phased application of branch [B]. The composable's _destination_ is still `:shared:ui`, but the _timing_ is deferred. The skill flags the deferral in its before/after report so the user can come back to it.

## Tie-breaker rules

When two branches match (rare), use:

1. **Smaller blast radius wins.** `:shared:ui` (no module wiring) > `:core:<name>` (needs module + dep) > `feature/.../component/`.
2. **Project-precedent wins.** If a similar composable already lives in `:shared:ui`, place the new one there too (consistency > theoretical purity).
3. **Avoid bootstrap blockers.** If a destination requires creating a new `:core:*` module and the user hasn't approved that, fall back to `feature/<x>/.../component/` with a `// TODO(:core:<name>)` comment.
4. **`internal` over `private`** for any composable extracted to its own file (it must be visible to the file that imports it). Only top-level `@Preview` private helpers stay `private`.

## When the tree is wrong

This tree codifies project conventions that change over time. If walking the tree produces a result that contradicts:

- [`architecture.md`](../../../rules/architecture.md) module rules (especially Rule 12 about `:core:*`)
- [`conventions.md`](../../../rules/conventions.md) Route↔Screen contract (no DI in extracted components)
- [`preview.md`](../../../rules/preview.md) Preview discipline (every component gets a Preview)
- [`models.md`](../../../rules/models.md) Mapper rules (UI types stay where they belong)

…then the **rules win**. Update this tree to match. Don't refactor against the rules just because the tree says so.
