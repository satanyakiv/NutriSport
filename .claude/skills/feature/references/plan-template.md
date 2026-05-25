# Plan template — `.claude/plans/feature-<name>.md`

Frontmatter spec for a feature pipeline plan. The interactive phase of [`feature`](../SKILL.md) writes this file; the autonomous phase reads it.

## Frontmatter

```yaml
---
name: checkout # → :feature:home:cart:checkout (path follows the module nesting)
status: planning # planning | approved | in_progress | done | blocked

# One entry per Figma frame. nodeId format "X:Y" (URL `?node-id=X-Y` → dash to colon).
figmaFrames:
  - { name: CheckoutForm, nodeId: "201:55" }
  - { name: PaymentCompleted, nodeId: "201:88" }

routes: # append-only to :shared:utils/.../Screen.kt; omit entries that already exist
  - { screen: "Screen.Checkout(val totalAmount: Double)", parent: "Screen" }
  - {
      screen: "Screen.PaymentCompleted(val isSuccess: Boolean?, val error: String?, val token: String?)",
      parent: "Screen",
    }

flow:
  positive:
    - "Cart with items → Checkout → pay → navigate to Screen.PaymentCompleted(isSuccess=true)"
    - "PaymentCompleted success → Back → navigate to Screen.HomeGraph"
  negative:
    - "Checkout with payment failure → navigate to Screen.PaymentCompleted(isSuccess=false, error=...)"
    - "Network 500 during pay → ErrorCard with retry (no navigation)"
  neutral:
    - "Slow network (3 s) → LoadingCard → Content"
    - "Back from Checkout → Cart preserved (state survived)"

repository: OrderRepository # if missing in :network, pipeline scaffolds Repository interface + Impl + Fake/Remote DataSource + Mapper — flag the expansion in `## Context`

useCases: # if any are missing in :domain, pipeline scaffolds them — flag the expansion in `## Context`
  - PlaceOrderUseCase
  - CalculateCartTotalUseCase

parallelGroup: B # see .claude/features/00-orchestrator.md
---
```

## Required body sections

After the frontmatter, the plan body has these sections in order:

```markdown
## Context

Why this feature exists, current state in :network and :domain, what gates this lands behind.

## Decisions answered (from interactive A.5)

| Question                 | Answer                              |
| ------------------------ | ----------------------------------- |
| Post-success destination | navigate to Screen.PaymentCompleted |
| Social login this round  | No — flagged in Not Covered         |
| Empty state for Checkout | Generic ErrorCard for v1            |

## Files to create

Per frame, lists the 5-piece skeleton paths (UiState, Action, ViewModel, Route, Screen) plus sibling PreviewData. Previews are inline at the bottom of `<Frame>Screen.kt` per [`.claude/rules/preview.md`](../../../rules/preview.md) — never a separate `<Frame>ScreenPreview.kt`.

## Files to modify

- `:shared:utils/.../navigation/Screen.kt` — append routes
- `:navigation/.../NavGraph.kt` — append composable<Screen.X>
- `:di/.../KoinModule.kt` — aggregate checkoutFeatureModule
- `settings.gradle.kts` — include(":feature:home:cart:checkout")

## Implementation notes

Anything specific to the feature: validation rules, payment-token handling,
state handoff. Pipeline reads this when writing ViewModel logic.

## Verification

Lists the gradle commands the pipeline will run; matches §"Quality gates"
in SKILL.md.

## Not Covered (and why)

**Social login** — out of scope for v1. Apple/Google sign-in deferred.

## Related

- `.claude/rules/conventions.md`
- `.claude/skills/feature/SKILL.md`
```

## Field rules

| Field                  | Required | Notes                                                                                                                                                                          |
| ---------------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `name`                 | yes      | lowercase, kebab-case → `feature/<name>` and the matching `:feature:...` Gradle path                                                                                           |
| `status`               | yes      | `planning` while drafting; flip to `approved` after `ExitPlanMode`                                                                                                             |
| `figmaFrames[].nodeId` | yes      | one per screen; format `"X:Y"` (URL `?node-id=X-Y` → dash to colon)                                                                                                            |
| `routes[].screen`      | yes      | full Kotlin signature, e.g. `Screen.Details(val id: String?)` or `Screen.Checkout(val totalAmount: Double)`. Omit entries that already exist in `:shared:utils/.../Screen.kt`. |
| `routes[].parent`      | yes      | `Screen` for top-level entries in the sealed class.                                                                                                                            |
| `flow.positive[]`      | yes      | appended to [`docs/E2E_BACKLOG.md`](../../../../docs/E2E_BACKLOG.md) (created by the testing/CI track) at step 11 (parking lot until activation gate met); soft gate           |
| `flow.negative[]`      | yes      | appended to `docs/E2E_BACKLOG.md` at step 11; also drives composition error-state tests at step 9                                                                              |
| `flow.neutral[]`       | optional | rotation, back-button, slow network — covered by composition tests                                                                                                             |
| `repository`           | yes      | resolves to `:network`; pipeline scaffolds Repository interface + Impl + Fake/Remote DataSource + Mapper if missing                                                            |
| `useCases[]`           | yes      | resolves to `:domain/.../usecase/`; pipeline scaffolds any missing entries. Scope expansion MUST be flagged in `## Context`                                                    |
| `parallelGroup`        | yes      | per [`00-orchestrator.md`](../../../features/00-orchestrator.md)                                                                                                               |

## How the pipeline consumes this

- `figmaFrames[]` → step 3 (one Figma handoff per entry, per-screen mini-loop)
- `routes[]` → step 2 (append `@Serializable` to `Screen.kt`) and step 4 (`composable<Screen.X>` per entry)
- `flow.positive[]` → step 11 (one bullet per scenario appended to `docs/E2E_BACKLOG.md`, soft gate; tests are NOT scaffolded as Kotlin until the activation gate is met)
- `flow.negative[]` → step 11 (appended to `docs/E2E_BACKLOG.md`) and step 9 (composition error-state tests run today)
- `flow.neutral[]` → step 9 (composition tests for slow network, rotation, back-button)
- `useCases[]` → step 8 (constructor-injected per ViewModel)
- `repository` → resolves the use case → repository chain when writing the ViewModel

## Status transitions

```
planning ─(ExitPlanMode)─▶ approved ─(Phase B starts)─▶ in_progress ─(final step done)─▶ done
                                                                  └──(hard fail)──▶ blocked
```

`done` plans stay in `.claude/plans/` for audit; `blocked` plans surface in the chat-console report.
