---
name: claude-in-mobile
description: Use when capturing an Android emulator screenshot or iOS simulator screenshot, dumping a UI tree, or installing a build to the device — e.g. "screenshot the emulator", "capture Details on the emulator", "зроби скрін емулятора", "які пристрої підключені", "ui dump", or any step in the `feature` / `dev-jump` pipelines that needs an emulator/simulator render. Router for two CLIs — Google's first-party `android-cli` for Android (since 2026-05-21), AlexGladkov's native `claude-in-mobile` binary for iOS. Local-dev only; cloud Claude Code containers and CI have no binary and no emulator/simulator, so capture / device steps are no-ops.
---

# claude-in-mobile — Android (via `android-cli`) + iOS (via `claude-in-mobile` binary)

Since 2026-05-21 this skill is a **router** for two CLIs:

| Platform | Tool                                                                                                 | Why                                                                                                                                          |
| -------- | ---------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| Android  | Google's first-party [`android-cli`](https://developer.android.com/tools/agents/android-cli) (v1.0+) | `screen capture --annotate` + `screen resolve` give agent-friendly label-based addressing; `layout --pretty` returns Compose semantics JSON. |
| iOS      | [`AlexGladkov/claude-in-mobile`](https://github.com/AlexGladkov/claude-in-mobile) native Rust binary | Only path for iOS Simulator capture. `android-cli` is Android-only.                                                                          |

Both tools are **local-dev only**. Cloud Claude Code containers and CI have no binary and no emulator/simulator. Capture / device steps skip silently there — same practical outcome as before either tool existed.

## Installation (local dev)

```bash
# Android
brew tap android/tap
brew install android-cli
android --version           # → 1.0.x

# iOS
brew tap AlexGladkov/claude-in-mobile
brew install claude-in-mobile
claude-in-mobile doctor     # ADB + simulator self-check
```

Homebrew owns both binary lifecycles; no in-repo install script, no version pin. Cloud/CI no-op contract: [`references/install.md`](references/install.md).

## When to use

- `feature` pipeline Steps 10 / 12 / 13 (assemble + install, emulator capture, end-to-end fidelity check).
- `dev-jump` screenshot / "right screen loaded?" confirmation step.
- Ad-hoc "screenshot the emulator" / "which devices are connected" / "ui dump".

Do NOT use for: launching the app on a specific Screen — that is `dev-jump`'s job and it uses a `nutrisport://` deep-link launch (`adb shell am start -a android.intent.action.VIEW -d "nutrisport://..."`). Neither `android run` nor `claude-in-mobile install` routes to a specific screen.

## Android command surface (`android-cli`)

| Command                                                               | Purpose                                                                                                                                                                     |
| --------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `adb devices`                                                         | Assert one device/emulator is reachable before capture.                                                                                                                     |
| `android emulator list`                                               | List configured AVDs.                                                                                                                                                       |
| `android info`                                                        | Print environment (SDK location, CLI version) — replaces `claude-in-mobile doctor` for Android.                                                                             |
| `android screen capture -o <file>`                                    | Emulator screenshot → PNG.                                                                                                                                                  |
| `android screen capture --annotate -o <file>`                         | Same + labeled bounding boxes (`#1`, `#2`, …) around every UI element.                                                                                                      |
| `android screen resolve --screenshot=<file> --string="tap #N"`        | Substitute every `#N` placeholder in the string with center coords of label `N` from the annotated PNG. Output e.g. `"tap 1189 245"`.                                       |
| `android layout --pretty -o <file>.json`                              | Compose semantics tree as JSON (`text`, `center`, `interactions: [clickable, focusable]`, composition `key`). Cheaper than capturing a PNG when only the structure matters. |
| `android run --apks=<apk> [--device=<serial>] [--activity=… --debug]` | Install (and optionally launch) a debug APK.                                                                                                                                |

### Image budget recipe (mandatory for Android screenshots)

`android screen capture` writes raw PNG (~1.3 MB for a 1280×2856 device). Eight raw captures blow through the 10-image / 30-MB per-thread cap. Always post-process to WebP:

```bash
android screen capture -o /tmp/cap.png
cwebp -q 75 /tmp/cap.png -o screenshots/<frame>__emulator__light.webp
rm /tmp/cap.png
# 1.3 MB PNG → ~95 KB WebP (≈13× smaller, ~92% saving — measured 2026-05-21)
```

`cwebp` is the project-canonical compression tool, already used for committed drawables ([`conventions.md`](../../rules/conventions.md)).

### Label-driven taps (new pattern enabled by `android-cli`)

```bash
android screen capture --annotate -o /tmp/annotated.png
android screen resolve --screenshot=/tmp/annotated.png --string="tap #5"
# → "tap 1189 245"
adb shell input tap 1189 245
```

Useful when the agent must interact with a control identified visually (e.g. "the third bottom-nav tab") without inventing pixel coordinates. The annotated PNG itself still costs one image slot if you load it via `Read`; in scripted flows you can chain capture → resolve → `adb input tap` without ever passing the PNG to the model.

## iOS command surface (`claude-in-mobile` binary)

| Command                                                                            | Purpose                                                                                                                                                                                    |
| ---------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `claude-in-mobile doctor`                                                          | ADB + simulator dependency self-check.                                                                                                                                                     |
| `claude-in-mobile devices ios`                                                     | List booted iOS simulators.                                                                                                                                                                |
| `claude-in-mobile screenshot ios -o <path> [--compress --max-width N --quality N]` | Simulator capture (binary handles compression flags itself).                                                                                                                               |
| `claude-in-mobile ui-dump ios`                                                     | Accessibility-tree dump (XML).                                                                                                                                                             |
| `claude-in-mobile install <app>`                                                   | Install `.app` to simulator.                                                                                                                                                               |
| `xcrun simctl openurl booted <url>`                                                | Open a URL / custom-scheme deep link on the **booted** simulator (routes through `onOpenURL`). Unambiguous — preferred.                                                                    |
| `claude-in-mobile open-url ios <url> --simulator <name>`                           | Binary alternative. Resolves `<name>` by name only, so it picks a _shutdown_ duplicate when device names collide (common after Xcode upgrades) and rejects UDIDs. Prefer the `simctl` row. |
| `xcrun simctl location <udid> set <lat>,<lon>`                                     | Set simulator GPS. Apple-supplied, outside the binary — mirrors `xcrun simctl boot`.                                                                                                       |

Full iOS reference: [`references/cli.md`](references/cli.md).

## iOS deep-link / location

> The `nutrisport` custom scheme, the `:core:deeplink` registry, and the `onOpenURL` routing in `iosApp/iosApp/iOSApp.swift` are established by the deep-links track. Until that lands, only the `simctl` _mechanism_ below is live; the specific paths describe the contract the deep-links track fulfils.

The iOS host registers the `nutrisport` custom scheme (`iosApp/iosApp/Info.plist` → `CFBundleURLSchemes`) and routes incoming URLs through `onOpenURL` in `iosApp/iosApp/iOSApp.swift`. Deliver a URL to the booted simulator and that path runs, so deep-link routing can be exercised without a manual Safari / Notes tap.

Prefer `xcrun simctl openurl booted` over the binary's `open-url ios --simulator <name>`: the binary resolves the simulator by **name** and, when several devices share a name (e.g. four "iPhone 16e" after an Xcode upgrade), targets a _shutdown_ duplicate and fails with `Unable to lookup in current state: Shutdown` — and it does not accept a UDID. `booted` is unambiguous and needs no binary.

URL form is `nutrisport://<host>/<path>` — the `<host>` authority is discarded; the resolver matches on `<path>`. Valid paths are the single source of truth in the `:core:deeplink` registry (public: `/products/{id}`, `/categories/{category}`; auth-gated: `/cart`, `/checkout`, `/profile`; admin-gated: `/admin`). Auth-gated paths with no active session land on the auth screen and park the target for post-login replay.

```bash
# Public route — non-destructive, no session needed
xcrun simctl openurl booted "nutrisport://app/products/123"

# Sentry crash smoke-test (host == "crash" → fatalError in iOSApp.swift).
# Previously triggerable only by a manual tap on a physical device.
xcrun simctl openurl booted "nutrisport://crash"

# Simulator GPS — no geo feature ships yet, this is the ready mechanism for when one does
xcrun simctl location booted set 37.7749,-122.4194
```

> The v3.9.0 "Deep Device Introspection" modules (`sandbox`, `intent`, `sensor`, `network`, `performance framestats`) live in the upstream **TypeScript MCP server**, not the Rust CLI this skill drives, and most are Android-only (`adb run-as` / GPU framestats). They are NOT reachable through the iOS CLI transport — do not look for them in the binary. iOS deep-link is `xcrun simctl openurl booted` (binary `open-url ios` is a fragile alternative); iOS location is `xcrun simctl location`.

## Emulator / simulator boot

**Android** — `android emulator start <avd-name>` boots an AVD and blocks until it is fully ready (the CLI returns only when `sys.boot_completed=1`). List available AVDs with `android emulator list`. Stop with `android emulator stop <serial>`. Use this in preference to launching the SDK `emulator` binary by hand.

**iOS** — the `claude-in-mobile` binary does not boot simulators; `xcrun simctl boot` is the Apple-supplied path.

```bash
# Android — start + assert ready
android emulator list                                                # see AVDs
android emulator start Pixel_5_API_34_account_settings               # blocks until ready
adb devices                                                          # emulator-5554 should appear

# Android — stop
android emulator stop emulator-5554

# iOS — start
xcrun simctl boot "<simulator-name>"
```

`feature` Step 10 calls `adb devices` (Android) to **assert** a device is present, not to boot one. If the dev forgot to boot, the soft-skip path applies (cloud / CI also lands here); booting is one `android emulator start` call away on local dev.

## Image budget (unchanged by tool switch)

Every screenshot the agent loads still consumes one slot of the 20-image / 30-MB per-thread cap. The limit is on images **presented to the model in a thread**, independent of which CLI wrote the file. The discipline in [`media-budget.md`](../../rules/media-budget.md) applies unchanged: ≤10 captures per session, one per screen, light mode by default, reuse cached WebPs for fidelity checks. Prefer `android layout` over `android screen capture` when you only need to confirm a screen loaded.

## Anti-patterns

- Re-capturing the same screen to "re-verify" — once per screen per session, then trust the diff ([`media-budget.md`](../../rules/media-budget.md)).
- Skipping `cwebp` post-process on Android — raw PNG is ~1.3 MB; eight captures saturate the 30 MB cap.
- Assuming the binaries exist in cloud / CI — guard with `command -v android` (Android) or `command -v claude-in-mobile` (iOS) before any capture; absent → skip with a logged note, not an error.
- Trying to boot an emulator/simulator via either CLI — neither has a boot subcommand; boot manually.
- Using `android run` or `claude-in-mobile install` for a specific Screen — neither routes to a screen; `dev-jump`'s `nutrisport://` deep-link launch is the supported path for that.
- Loading the annotated PNG into the conversation just to read `#N` labels visually — `screen resolve` substitutes coordinates without burning an image slot.

## Related

- [`.claude/rules/media-budget.md`](../../rules/media-budget.md) — per-thread image cap (applies to both tools).
- [`.claude/rules/conventions.md`](../../rules/conventions.md) — `cwebp -q 75` is the canonical WebP-conversion command project-wide.
- [`.claude/skills/feature/SKILL.md`](../feature/SKILL.md) — Steps 10 / 12 / 13 consumer.
- [`.claude/skills/dev-jump/SKILL.md`](../dev-jump/SKILL.md) — screenshot / visual-confirm consumer; raw-ADB launch path.
- [`references/cli.md`](references/cli.md) (iOS binary subcommands) · [`references/install.md`](references/install.md) (install + cloud/CI no-op contract)
- Upstreams: [`android-cli`](https://developer.android.com/tools/agents/android-cli) (Android), [`AlexGladkov/claude-in-mobile`](https://github.com/AlexGladkov/claude-in-mobile) (iOS).
