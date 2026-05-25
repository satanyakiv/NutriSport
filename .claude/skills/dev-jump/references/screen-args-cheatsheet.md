# dev-jump Deep-Link Path Cheatsheet

`nutrisport://` deep-link path for every `Screen` destination. Source of truth for the route set: [`Screen.kt`](../../../shared/utils/src/commonMain/kotlin/com/nutrisport/shared/navigation/Screen.kt).

> **Scope note.** The `nutrisport://` scheme and these path mappings are established by the deep-links track. The table below is the intended path form; when the track lands, keep it in lockstep with the route mapper. Until then, `dev-jump` degrades to manual navigation (see the SKILL.md prerequisite note).

## Command template

```bash
# Public route:
adb shell am start -a android.intent.action.VIEW \
  -d "nutrisport://<path>" \
  com.portfolio.nutrisport.debug
```

Always target the debug package `com.portfolio.nutrisport.debug`.

## Gating legend

- **public** — reachable with no session (the entry surface).
- **auth-gated** — requires a signed-in session; without one the link lands on `Auth`.
- **admin-gated** — requires an admin session; non-admins are redirected.

## Path table

| Screen route                                | `nutrisport://` path                                                      | Args                                         | Gating      |
| ------------------------------------------- | ------------------------------------------------------------------------- | -------------------------------------------- | ----------- |
| `Auth`                                      | `nutrisport://auth`                                                       | none                                         | public      |
| `HomeGraph`                                 | `nutrisport://home`                                                       | none                                         | auth-gated  |
| `ProductsOverview`                          | `nutrisport://products`                                                   | none                                         | auth-gated  |
| `Details(id)`                               | `nutrisport://products/{id}`                                              | `id: String?` (path segment)                 | auth-gated  |
| `Categories`                                | `nutrisport://categories`                                                 | none                                         | auth-gated  |
| `CategorySearch(category)`                  | `nutrisport://categories/{category}`                                      | `category: String` (path segment)            | auth-gated  |
| `Cart`                                      | `nutrisport://cart`                                                       | none                                         | auth-gated  |
| `Checkout(totalAmount)`                     | `nutrisport://checkout?totalAmount={amount}`                              | `totalAmount: Double` (query)                | auth-gated  |
| `PaymentCompleted(isSuccess, error, token)` | `nutrisport://payment-completed?isSuccess={bool}&error={msg}&token={tok}` | all optional query params                    | auth-gated  |
| `Profile`                                   | `nutrisport://profile`                                                    | none                                         | auth-gated  |
| `AdminPanel`                                | `nutrisport://admin`                                                      | none                                         | admin-gated |
| `ManageProduct(id)`                         | `nutrisport://admin/products/{id}`                                        | `id: String?` (path segment, omit to create) | admin-gated |

## Examples

```bash
# Product details for a specific product:
adb shell am start -a android.intent.action.VIEW \
  -d "nutrisport://products/prod-42" com.portfolio.nutrisport.debug

# Category search:
adb shell am start -a android.intent.action.VIEW \
  -d "nutrisport://categories/PROTEIN" com.portfolio.nutrisport.debug

# Checkout with a total amount:
adb shell am start -a android.intent.action.VIEW \
  -d "nutrisport://checkout?totalAmount=49.99" com.portfolio.nutrisport.debug

# Terminal success screen:
adb shell am start -a android.intent.action.VIEW \
  -d "nutrisport://payment-completed?isSuccess=true" com.portfolio.nutrisport.debug

# Admin product editor (create mode — no id):
adb shell am start -a android.intent.action.VIEW \
  -d "nutrisport://admin/products" com.portfolio.nutrisport.debug
```

## Maintenance

When adding a new route to `Screen.kt`, add a row to this table and a corresponding mapping in the deep-links track's route mapper.
