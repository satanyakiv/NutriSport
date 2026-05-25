# claude-in-mobile router — install & environment contract

Since 2026-05-21 the skill routes to two CLIs. Both are installed via Homebrew, both are local-dev only.

## Local dev (the only environment where capture is real)

```bash
# Android transport
brew tap android/tap
brew install android-cli
android --version           # → 1.0.x

# iOS transport
brew tap AlexGladkov/claude-in-mobile
brew install claude-in-mobile
claude-in-mobile doctor     # ADB + simulator self-check
```

Homebrew owns both binary lifecycles. There is **no in-repo install script and no version pin** for either — unlike the `rtk` tooling (`.claude/rules/bash-output.md`), which is pinned because it filters Bash output on every session. The capture CLIs are local-dev-only visual-verification helpers, so a pinned/scripted install would be maintenance cost with no payoff. Upgrade with `brew upgrade android-cli` / `brew upgrade claude-in-mobile` when you choose to.

## Cloud Claude Code containers & CI — no-op contract

Cloud Claude Code containers and CI runners:

- have **no Homebrew**, neither `android` nor `claude-in-mobile` binary, and
- have **no Android emulator / iOS simulator**.

Therefore every capture / device step is a guarded no-op there:

```bash
command -v android >/dev/null || { echo "android-cli absent — skipping Android capture"; }
command -v claude-in-mobile >/dev/null || { echo "claude-in-mobile absent — skipping iOS capture"; }
```

This is the **same practical outcome as before**: cloud sessions also had no emulator to talk to via the retired MCP / older `claude-in-mobile` binary. No new failure mode. `feature` Steps 10 / 12 are already classified soft / soft-skip; `dev-jump`'s capture step degrades to "describe from code, no screenshot".

Never treat an absent binary as an error or a pipeline hard-fail. Log the skip and continue.

## Verification after install

```bash
# Android transport
android --version                                            # 1.0.x
adb devices                                                  # at least one device, e.g. emulator-5554
android emulator list                                        # AVDs visible
android screen capture -o /tmp/verify.png && test -s /tmp/verify.png && echo "Android capture OK"
android layout --pretty | head                                # non-empty JSON

# iOS transport
claude-in-mobile doctor                                      # green / no errors
command -v claude-in-mobile                                   # prints a path
claude-in-mobile devices ios                                  # lists booted iOS simulators
```

(Boot the emulator / simulator first — Android: `android emulator start <avd-name>`; iOS: `xcrun simctl boot "<simulator-name>"`. See [`SKILL.md`](../SKILL.md) "Emulator / simulator boot".)

## Out-of-repo follow-up (one-time, manual)

Disabling the retired harness-level `claude-in-mobile` MCP server is a global Claude Code config action **outside this repository** — it is not in `.mcp.json` (Figma only) nor in `.claude/settings*.json`. This repo no longer references any `mcp__claude-in-mobile__*` tool; the manual unregistration just stops the harness from advertising an unused server.

## Related

- [`SKILL.md`](../SKILL.md) · [`cli.md`](cli.md) — iOS command reference (Android commands live inline in SKILL.md)
- [`.claude/rules/bash-output.md`](../../../rules/bash-output.md) — the `rtk` pinned-CLI pattern this skill deliberately does NOT replicate.
- Upstream: [`android-cli`](https://developer.android.com/tools/agents/android-cli) · [`AlexGladkov/claude-in-mobile`](https://github.com/AlexGladkov/claude-in-mobile)
