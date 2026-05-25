# claude-in-mobile (iOS binary) — CLI reference

Canonical subcommand reference for the **iOS transport** of the `claude-in-mobile` skill — the AlexGladkov native binary. Android transport runs on Google's `android-cli`; its commands live inline in [`SKILL.md`](../SKILL.md) §"Android command surface".

Upstream ships ~38 subcommands across Android / iOS / desktop / browser; only the iOS-relevant subset is documented here. Run `claude-in-mobile --help` for the exhaustive list.

## Self-check

```bash
claude-in-mobile doctor            # checks ADB / xcrun discovery + reachable simulator
command -v claude-in-mobile        # presence guard (cloud / CI returns nothing)
```

Always run `doctor` (or the `command -v` guard) before any capture. Absent binary / no simulator → skip the step with a logged note, never error.

## Devices

```bash
claude-in-mobile devices ios       # list booted iOS simulators
```

## Screenshot

```bash
claude-in-mobile screenshot ios -o screenshots/<frame>__simulator__light.png
claude-in-mobile screenshot ios -o <path> --compress
claude-in-mobile screenshot ios -o <path> --compress --max-width 800 --quality 60
```

- `-o <path>` — write target. Convention: `screenshots/<frame>__simulator__light.png` (light mode), paired with the Figma side `screenshots/<frame>__figma.png`.
- `--compress` — first-pass visual checks. Binary handles WebP/quality internally; no external post-process needed (contrast: Android transport requires `cwebp` post-process — see [`SKILL.md`](../SKILL.md) §"Image budget recipe").
- `--max-width N` / `--quality N` — finer control.

## UI-tree dump

```bash
claude-in-mobile ui-dump ios       # accessibility tree (XML)
```

Use to confirm "the right screen loaded" without spending an image slot.

## App install

```bash
claude-in-mobile install <path-to-app-bundle>
```

iOS Simulator install (.app bundle). Simulator must already be booted (manual local-dev prerequisite — there is no CLI boot subcommand).

## Deep link / open URL

```bash
xcrun simctl openurl booted "nutrisport://app/products/123"   # public route → product details
xcrun simctl openurl booted "nutrisport://crash"              # Sentry smoke-test → fatalError
```

Drives the iOS host's `onOpenURL` handler (`iosApp/iosApp/iOSApp.swift`). URL form is `nutrisport://<host>/<path>` — `<host>` is discarded, the resolver matches `<path>`. Valid paths come from the `:core:deeplink` registry (established by the deep-links track). Auth-gated paths without a session land on the auth screen. Use this instead of a manual Safari / Notes tap to exercise deep-link routing on the simulator.

The binary's `claude-in-mobile open-url ios <url> --simulator <name>` does the same thing, but resolves the simulator by **name**: with duplicate device names (e.g. several "iPhone 16e") it targets a _shutdown_ duplicate and fails (`Unable to lookup in current state: Shutdown`), and it rejects UDIDs. Prefer the `booted` form above.

## Location

```bash
xcrun simctl location <udid> set 37.7749,-122.4194
```

Apple-supplied, outside the binary (mirrors `xcrun simctl boot`). The app ships no geo feature yet — this is the ready mechanism for when one arrives, not a current dependency.

## Other subcommands (available, not part of the core repo flow)

`launch` (basic, no extras flag — `dev-jump`'s raw `xcrun simctl launch` path would be used if iOS Screen-targeted launch is ever added), `stop`, `uninstall`, `shell`, `logs`, `tap`, `swipe`, `input`, `key`, `find`, `apps`, `annotate`, `setup`.

## Android-side cheat-sheet

For Android use the equivalent `android-cli` commands documented inline in [`SKILL.md`](../SKILL.md) §"Android command surface". Mapping:

| iOS (`claude-in-mobile`)                             | Android (`android-cli`)                                                                               |
| ---------------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| `claude-in-mobile devices ios`                       | `adb devices` + `android emulator list`                                                               |
| `claude-in-mobile screenshot ios -o file`            | `android screen capture -o file` (+ mandatory `cwebp -q 75` post-process)                             |
| `claude-in-mobile screenshot ios -o file --compress` | same `android screen capture` + `cwebp -q 75` — the `--compress` flag has no `android-cli` equivalent |
| `claude-in-mobile ui-dump ios`                       | `android layout --pretty -o file.json`                                                                |
| `claude-in-mobile install <app>`                     | `android run --apks=<apk>`                                                                            |
| `claude-in-mobile open-url ios <url>`                | `adb shell am start -a android.intent.action.VIEW -d <uri>`                                           |

## Related

- [`SKILL.md`](../SKILL.md) — router, when to use, emulator-boot gap, image budget.
- [`install.md`](install.md) — Homebrew install + cloud/CI no-op contract for both transports.
