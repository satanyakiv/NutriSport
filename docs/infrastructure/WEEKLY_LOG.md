# Weekly log

Chronological journal of the Sunday infrastructure reviews. Each section is one week: what commits were scanned, which entries were added, and three reflection answers. Append-only.

## 2026-05-25 — Week 1 (bootstrap)

**Commits scanned.** This week's portfolio-tooling branch: rules expansion, fail-safe hooks, Sentry dual setup (Android), and a 9-skill port — `c73feb4`, `59c6f7d`, `bb609d9`, `5997274`, `b29e285`, `6e659f1`. Full repo history: 143 commits since 2025-11-20.

**Modified infra files.** `.claude/skills/` (+9 ported), `.claude/hooks/` (+figma-budget-guard, wired), `.claude/rules/` (14 total), `.claude/settings.json`, `.claude/figma/SCREEN_FRAME_MAP.md` (new stub).

**Entries added.**

- [wall-clock-economy](wall-clock-economy.md): Robolectric JVM suite (~30s, 0 emulator), Baseline Profiles harness.
- [ai-workforce](ai-workforce.md): 18 skills / 5 hooks / 14 rules surface.
- [token-economy](token-economy.md): cacheable rules surface, rtk allowlist.
- [reliability-and-safety](reliability-and-safety.md): fail-safe hooks, deterministic fake-data path.
- [process-and-workflow](process-and-workflow.md): 3 convention plugins → 25 modules, plan-mode continuity.
- [lessons-learned](lessons-learned.md): disabled figma-budget-guard; caught ComponentRegistrar R8 gap.

**Entries deferred.**

- Baseline Profile before→after startup numbers — needs a device run; re-evaluate once captured.
- rtk per-repo compression benchmark — not yet run on this repo.

**Reflection.**

1. _What worked well?_ Mirroring a proven infra-docs structure and seeding it with already-measured facts (test runtime, coverage, module/plugin counts) rather than inventing numbers.
2. _What did we try and drop? Why?_ Carrying over benchmark numbers from elsewhere — dropped, because portfolio entries must use this repo's own measurements; un-measured fields are marked `qualitative only`.
3. _What to try next week?_ Capture a real Baseline-Profile device run and a per-repo rtk benchmark to replace the two `qualitative only` placeholders.

## 2026-05-26 — Week 1 (continued): Router + cross-platform deep links

**Commits scanned.** Router abstraction — `604d6c9`, `642f771`, `b7aa3b8`, `96ffc79`; cross-platform deep links — `f48fee9`, `c9e2ee2`, `781838a`, `a49844f`, `b158cf2`. Local feature branch; not pushed.

**Modified infra files.** `.claude/rules/navigation.md` (new, B1), `.claude/skills/claude-in-mobile/SKILL.md` (iOS deep-link section updated to the landed registry), `docs/DEEPLINKS.md` (new), `docs/adr/0007-deeplink-architecture.md` (new). Surface now: 18 skills / 8 hook commands / 15 rules.

**Entries added.**

- [process-and-workflow](process-and-workflow.md): plan-mode exploration caught a cross-platform deep-link bug before code.

**Entries deferred.**

- Push-tap → deep-link wiring — deferred until the KMPNotifier tap-payload contract is settled.
- App Links / Universal Links before→after — needs a production domain; re-evaluate when one exists.
- iOS simulator deep-link E2E — only Android was live-verified this session; re-run once the iOS framework is built.

**Reflection.**

1. _What worked well?_ Plan-mode with three parallel Explore agents surfaced the resolver host-folding mismatch and two navigation-topology conflicts (Cart inner-tab, Checkout amount) before any code — the registry was right on the first pass, zero rework commits.
2. _What did we try and drop? Why?_ A verbatim resolver port — dropped once exploration showed the source's throwaway-host convention (`scheme://app/...`) would not match NutriSport's natural `nutrisport://products/123` URLs; replaced with scheme-aware host folding. Also dropped `/cart` and `/checkout` from the registry (inner tab / needs a cart-derived amount).
3. _What to try next week?_ Wire push-tap deep links once the KMPNotifier tap-payload contract is settled, and run the iOS simulator E2E to match the Android live verification.
