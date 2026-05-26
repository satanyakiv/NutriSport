package com.nutrisport.core.deeplink.registry

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import com.nutrisport.shared.navigation.Screen
import kotlin.test.Test

/**
 * Guards the linkable-Screen contract. Asserts both directions — every linkable
 * Screen is reachable, and every intentionally excluded Screen is absent.
 */
class DeeplinkRegistryTest {

  private val resolvedScreens: Set<Screen> = DeeplinkRegistry.routes
    .mapNotNull { route ->
      // Synthetic captures so routes with {id}/{category} resolve to a value.
      route.resolve(mapOf("id" to "x", "category" to "Protein"), emptyMap())
    }
    .toSet()

  @Test
  fun `linkable screens are present`() {
    val mustBeReachable: Set<Screen> = setOf(
      Screen.Details(id = "x"),
      Screen.CategorySearch(category = "Protein"),
      Screen.Profile,
      Screen.AdminPanel,
    )
    for (screen in mustBeReachable) {
      assertThat(resolvedScreens, name = "Registry should expose $screen").contains(screen)
    }
  }

  @Test
  fun `excluded screens are intentionally absent`() {
    val mustBeExcluded: Set<Screen> = setOf(
      Screen.Cart,
      Screen.ProductsOverview,
      Screen.Categories,
      Screen.Checkout(totalAmount = 0.0),
      Screen.PaymentCompleted(),
      Screen.ManageProduct(),
    )
    for (screen in mustBeExcluded) {
      assertThat(resolvedScreens, name = "Registry must NOT expose $screen").doesNotContain(screen)
    }
  }
}
