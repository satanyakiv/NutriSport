package com.nutrisport.core.deeplink.resolver

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.nutrisport.core.deeplink.DeeplinkGate
import com.nutrisport.core.deeplink.DeeplinkSource
import com.nutrisport.core.deeplink.registry.DeeplinkRegistry
import com.nutrisport.shared.navigation.Screen
import kotlin.test.Test

class DefaultDeeplinkResolverTest {

  private val resolver = DefaultDeeplinkResolver(routes = DeeplinkRegistry.routes)

  @Test
  fun `custom scheme to product details folds host into path and captures id`() {
    val resolved = resolver.resolveUri("nutrisport://products/123", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNotNull()
    assertThat(resolved!!.screen).isEqualTo(Screen.Details(id = "123"))
    assertThat(resolved.gate).isEqualTo(DeeplinkGate.Public)
  }

  @Test
  fun `universal link to product details ignores host and captures id`() {
    val resolved = resolver.resolveUri("https://nutrisport.com/products/123", DeeplinkSource.UniversalLink)
    assertThat(resolved).isNotNull()
    assertThat(resolved!!.screen).isEqualTo(Screen.Details(id = "123"))
  }

  @Test
  fun `category is matched case-insensitively and stored as canonical enum name`() {
    val resolved = resolver.resolveUri("nutrisport://categories/protein", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNotNull()
    assertThat(resolved!!.screen).isEqualTo(Screen.CategorySearch(category = "Protein"))
    assertThat(resolved.gate).isEqualTo(DeeplinkGate.Public)
  }

  @Test
  fun `multi-word category resolves to its enum name`() {
    val resolved = resolver.resolveUri("nutrisport://categories/PreWorkout", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNotNull()
    assertThat(resolved!!.screen).isEqualTo(Screen.CategorySearch(category = "PreWorkout"))
  }

  @Test
  fun `hyphenated category slug is normalized to its enum name`() {
    val resolved = resolver.resolveUri("nutrisport://categories/pre-workout", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNotNull()
    assertThat(resolved!!.screen).isEqualTo(Screen.CategorySearch(category = "PreWorkout"))
  }

  @Test
  fun `unknown category is rejected`() {
    val resolved = resolver.resolveUri("nutrisport://categories/bogus", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNull()
  }

  @Test
  fun `profile is signed-in gated`() {
    val resolved = resolver.resolveUri("nutrisport://profile", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNotNull()
    assertThat(resolved!!.screen).isEqualTo(Screen.Profile)
    assertThat(resolved.gate).isEqualTo(DeeplinkGate.SignedIn)
  }

  @Test
  fun `admin is admin gated`() {
    val resolved = resolver.resolveUri("nutrisport://admin", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNotNull()
    assertThat(resolved!!.screen).isEqualTo(Screen.AdminPanel)
    assertThat(resolved.gate).isEqualTo(DeeplinkGate.Admin)
  }

  @Test
  fun `product without id does not match`() {
    val resolved = resolver.resolveUri("nutrisport://products", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNull()
  }

  @Test
  fun `trailing slash is normalized`() {
    val resolved = resolver.resolveUri("nutrisport://profile/", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNotNull()
    assertThat(resolved!!.screen).isEqualTo(Screen.Profile)
  }

  @Test
  fun `fragment is ignored`() {
    val resolved = resolver.resolveUri("nutrisport://products/123#section", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNotNull()
    assertThat(resolved!!.screen).isEqualTo(Screen.Details(id = "123"))
  }

  @Test
  fun `unknown path returns null`() {
    val resolved = resolver.resolveUri("nutrisport://something/random", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNull()
  }

  @Test
  fun `malformed uri returns null`() {
    val resolved = resolver.resolveUri("not a url", DeeplinkSource.CustomScheme)
    assertThat(resolved).isNull()
  }

  @Test
  fun `push payload with deeplink delegates to uri parsing`() {
    val resolved = resolver.resolvePush(mapOf("deeplink" to "nutrisport://products/xyz"))
    assertThat(resolved).isNotNull()
    assertThat(resolved!!.screen).isEqualTo(Screen.Details(id = "xyz"))
  }

  @Test
  fun `push payload without deeplink key returns null`() {
    assertThat(resolver.resolvePush(mapOf("foo" to "bar"))).isNull()
  }

  @Test
  fun `push payload with blank deeplink returns null`() {
    assertThat(resolver.resolvePush(mapOf("deeplink" to ""))).isNull()
  }
}
