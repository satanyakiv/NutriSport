---
name: dev-jump
description: Use when verifying the visual/design fidelity of a specific Android screen via the `claude-in-mobile` skill (Android transport: `android-cli`) — bypasses full user flows by launching the debug app directly on the target screen via the `nutrisport://` deep-link scheme. Activates on requests like "verify the PaymentCompleted design", "check Details screen візуально", "screenshot the Cart", "відкрий цей екран на емуляторі", "як виглядає Checkout на емуляторі", "перевір верстку Profile", or any visual / design-system / Figma-fidelity check on a screen that lives behind a multi-step flow. Saves time when the screen is static or terminal (PaymentCompleted, Details) and the agent would otherwise have to walk through several prerequisite screens with form data. Android debug build only — iOS visual verification stays manual.
---

# dev-jump — direct-screen launcher for visual verification

`dev-jump` makes the Android debug app land on a target [`Screen`](../../shared/utils/src/commonMain/kotlin/com/nutrisport/shared/navigation/Screen.kt) destination on launch via the `nutrisport://` deep-link scheme, instead of walking the full UI flow to reach it.

> **Prerequisite — the deep-links track.** The `nutrisport://` scheme and the screen-jump routing it triggers are established by the deep-links track (intent filter in the Android manifest + a route mapper). **Until that lands**, dev-jump degrades to: navigate manually in the running app to the target screen, then capture a screenshot. The when-to-use logic, the decision protocol, and the `claude-in-mobile` capture integration below all still apply in the degraded mode — only the "launch directly" mechanism is gated on the deep-links track.

## When to use vs walk the flow

| Use `dev-jump`                                                                        | Walk the full flow                                               |
| ------------------------------------------------------------------------------------- | ---------------------------------------------------------------- |
| Visual / design-system parity check ("does it match Figma?")                          | Form validation behavior ("does the email field reject `foo@`?") |
| Terminal/success screens (`PaymentCompleted`, `Details`)                              | Multi-step state propagation (does cart data reach Checkout?)    |
| Static content rendering (typography, spacing, dark drift)                            | Side-effect testing (toasts, backend dispatch, analytics)        |
| Hard-to-reach states behind a multi-step checkout flow                                | Navigation transitions and back-stack behavior                   |
| Screenshot capture for Figma fidelity check on one screen                             | E2E happy paths (those belong in `androidInstrumentedTest`)      |
| Inspecting one screen across multiple route args (e.g. `Details(id="a")` vs `id="b"`) | First-time onboarding UX                                         |

## Decision protocol

When the user asks to look at or verify a specific screen, ask yourself:

1. **Does the user say "verify / check / на скрін / як виглядає / verify visually / verify the design"?** → `dev-jump`. They care about how it looks, not how it behaves.
2. **Does the user say "test that X works / поведінка / якщо натиснути / form validation"?** → walk the flow. The behavior IS the point.
3. **Ambiguous or unstated?** → ask once: "перевіряємо тільки рендер чи інтерактивний flow?" Don't guess wrong — the cost of a clarifying question is one round-trip; the cost of a wrong choice is several wasted minutes through the flow.

This matters because a deep-link jump skips ViewModel state accumulation. A `Checkout` reached via `dev-jump` has zero state from the cart flow — perfect for visual QA, useless for behavioral QA.

## Canonical command

Launch the debug app on a target screen via its `nutrisport://` deep link:

```bash
adb shell am start -a android.intent.action.VIEW \
  -d "nutrisport://products/123" \
  com.portfolio.nutrisport.debug
```

Two things matter here:

- **Target the debug package** (`com.portfolio.nutrisport.debug`). The release package id is `com.portfolio.nutrisport` (no `.debug` suffix).
- **Auth/admin gating.** Auth-gated deep links without a session land on the `Auth` screen; admin-gated links require an admin session. See the path table in [`references/screen-args-cheatsheet.md`](references/screen-args-cheatsheet.md) for which paths are public, auth-gated, or admin-gated.

## Per-screen reference

The full `nutrisport://` path reference for every Screen route lives in [`references/screen-args-cheatsheet.md`](references/screen-args-cheatsheet.md). Read it once per session and copy-paste the relevant path. Don't try to construct paths from memory.

## Anti-patterns

- **Running against the release package.** Use `com.portfolio.nutrisport.debug`, not `com.portfolio.nutrisport`. The release build does not carry debug-only conveniences.
- **Running on iOS.** The Android capture path (`android-cli`) is, well, Android-only. iOS deep links use the same scheme but visual verification on iOS stays manual unless a simulator deep-link path is wired (`xcrun simctl openurl booted "nutrisport://..."`).
- **Using `dev-jump` instead of fixing a flow bug.** If the agent is using `dev-jump` to skip _because of a bug_ in the flow (e.g. the checkout form crashes on submit), say so explicitly. Don't paper over flow bugs with `dev-jump` — file the bug first, then use `dev-jump` for the visual check.
- **Re-fetching screenshots between dev-jumps in the same session.** Every screen you load eats from the 20-image-per-thread budget (see Image budget below). Capture once per screen per session.

## Activation gate

Before sending the ADB command, verify the debug package is installed:

```bash
adb shell pm list packages | grep -q "com.portfolio.nutrisport.debug" || echo "Debug build NOT installed — run ./gradlew :androidApp:installDebug first"
```

If the activation gate fails, install first. Don't try to run `dev-jump` against `com.portfolio.nutrisport` (release id, no `.debug` suffix).

## Image budget integration

`dev-jump` is a budget-saver, not a budget-spender, when used correctly. The relevant rule is [`.claude/rules/media-budget.md`](../../rules/media-budget.md): 20 images / 30 MB total per Claude thread, no way to clear mid-session. Walking through several screens to reach a confirmation screen costs at minimum one capture per intermediate step you need to verify worked; jumping straight to the target costs one.

Practical rules:

- **One capture per screen per session.** Once you've seen `PaymentCompleted`, don't re-capture it — read the cached PNG via `Read` if you need to compare again.
- **Prefer `android layout --pretty` over screenshots when you only need to confirm "the right screen loaded".** A JSON Compose-semantics dump (text, center coords, interactions) is far cheaper than a screenshot — costs zero image-budget slots and is plenty for verifying that `dev-jump` landed on the intended Screen.
- **When you do capture, use `android screen capture -o /tmp/cap.png && cwebp -q 75 /tmp/cap.png -o screenshots/<screen>__emulator__light.webp && rm /tmp/cap.png`** for first-pass visual checks. Raw PNG is ~1.3 MB; WebP @ q=75 lands around 95 KB and keeps the image budget healthy. Skip the `cwebp` step only when you genuinely need lossless detail (rare).

## Related

- [`figma-handoff` skill](../figma-handoff/SKILL.md) Step 13 (visual fidelity check) — `dev-jump` is the recommended way to reach the target screen for the comparison capture.
- [`feature` skill](../feature/SKILL.md) per-screen mini-loop — when a feature plan adds a new terminal/success screen, the per-screen verification step uses `dev-jump`.
- [`claude-in-mobile` skill](../claude-in-mobile/SKILL.md) — the capture transport (`android-cli`) `dev-jump` builds on.
- [`.claude/rules/media-budget.md`](../../rules/media-budget.md) — the image limit rule this skill helps respect.
- [`.claude/rules/conventions.md`](../../rules/conventions.md) — Route/Screen separation that defines what `dev-jump` can target.
- [`.claude/rules/fake-data.md`](../../rules/fake-data.md) — debug builds use `Fake*DataSource`, so any ViewModel a `dev-jump`-loaded screen instantiates resolves against deterministic fakes.

## Maintenance: when adding a new Screen route

When [`Screen.kt`](../../shared/utils/src/commonMain/kotlin/com/nutrisport/shared/navigation/Screen.kt) gains a new `data object` or `data class`, add the corresponding `nutrisport://` path mapping in the deep-links track's route mapper and add a row to [`references/screen-args-cheatsheet.md`](references/screen-args-cheatsheet.md).
