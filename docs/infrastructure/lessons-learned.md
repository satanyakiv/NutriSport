# Lessons learned

Anti-patterns, disabled experiments, and risks caught but not yet closed. A portfolio without a "what didn't work" section reads as fiction. Append-only; correct a past entry with a new note, don't rewrite history.

## 2026-05-25 — figma-budget-guard hook disabled (env-var bypass doesn't propagate)

**What it was.** A PreToolUse hook meant to hard-block Figma MCP calls until the developer acknowledged the rate-limit budget by setting `FIGMA_BUDGET_ACK=1`.

**Why it failed.** The env var does not propagate into the Claude Code subprocess that the hook reads, so the acknowledgment can never be registered — the gate would block every Figma call with no way through.

**Resolution.** The script `exit 0`s immediately (disabled, with the reason in a comment). The behavioral discipline lives in the `figma-mcp-budget` skill instead. The hook + its settings wiring stay in place so the hard gate can be restored by removing two lines once the env-var path is solved.

**Lesson.** Hooks cannot rely on shell-session env vars reaching the agent subprocess. A guardrail that can't be acknowledged is worse than none — prefer a behavioral skill over an un-bypassable block.

**References.** [figma-budget-guard.sh](../../.claude/hooks/figma-budget-guard.sh) · [figma-mcp-budget skill](../../.claude/skills/figma-mcp-budget/SKILL.md)

## 2026-05-25 — Caught risk: Firebase Crashlytics under R8 full-mode lacks a keep rule

**What it is.** `androidApp` ships Google `firebase-crashlytics` (via BoM) and runs R8 full-mode (AGP 9.1.0, `release` minified), but `proguard-rules.pro` has no `-keep class * implements com.google.firebase.components.ComponentRegistrar { <init>(); }`.

**Why it matters.** R8 full-mode can strip the no-arg constructors of Firebase `ComponentRegistrar` implementations (instantiated reflectively via the Java SPI loader at startup). Without the keep rule, Crashlytics may fail to initialize in release and production crashes get silently dropped — invisible because debug works fine. Firebase BoM does not ship this rule via consumer ProGuard files.

**Status.** In progress — flagged during the `r8-analyzer` skill port; the keep rule has since been added to `proguard-rules.pro`. Verification still pending: a signed release APK + controlled crash → confirm the issue lands in the Crashlytics Console with a symbolicated trace.

**Lesson.** Google's consumer ProGuard rules do not cover every R8-full-mode interaction; audit reflectively-loaded SDK entry points before shipping a minified release. The `r8-analyzer` skill now carries this as an explicit audit checkpoint.

**References.** [r8-analyzer skill](../../.claude/skills/r8-analyzer/SKILL.md) · [proguard-rules.pro](../../androidApp/proguard-rules.pro) · [Crashlytics](../CRASHLYTICS.md)
