package com.nutrisport.core.deeplink.registry

import com.nutrisport.core.deeplink.DeeplinkGate
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.navigation.Screen

/**
 * Single source of truth for deeplink path → Screen mapping. Order matters for
 * path matching: more specific templates MUST come before less specific ones so
 * the resolver picks the right row on a first-match basis.
 *
 * Custom-scheme links omit a host (`nutrisport://products/123`); the resolver
 * folds the leading segment into the path before matching.
 *
 * Intentionally excluded:
 *  - Screen.Checkout — needs a cart-derived totalAmount a link cannot supply;
 *  - Screen.Cart / ProductsOverview / Categories — inner HomeNavHost tabs, not
 *    top-level Router destinations (the Router contract has no tab switching);
 *  - Screen.PaymentCompleted / ManageProduct — carry server-issued or
 *    admin-write state and have no meaningful entry point via a link.
 */
internal object DeeplinkRegistry {
  val routes: List<DeeplinkRoute> = listOf(
    DeeplinkRoute("/products/{id}", DeeplinkGate.Public) { params, _ ->
      params["id"]?.takeIf { it.isNotBlank() }?.let { Screen.Details(id = it) }
    },
    DeeplinkRoute("/categories/{category}", DeeplinkGate.Public) { params, _ ->
      // Normalize like the domain's valueOfProductCategory(): lowercase + letters
      // only, so "pre-workout", "Pre-Workout" and "PreWorkout" all resolve.
      params["category"]
        ?.lowercase()
        ?.filter { it.isLetter() }
        ?.let { slug -> ProductCategory.entries.firstOrNull { it.name.lowercase() == slug } }
        ?.let { Screen.CategorySearch(category = it.name) }
    },
    DeeplinkRoute("/profile", DeeplinkGate.SignedIn) { _, _ -> Screen.Profile },
    DeeplinkRoute("/admin", DeeplinkGate.Admin) { _, _ -> Screen.AdminPanel },
  )
}
