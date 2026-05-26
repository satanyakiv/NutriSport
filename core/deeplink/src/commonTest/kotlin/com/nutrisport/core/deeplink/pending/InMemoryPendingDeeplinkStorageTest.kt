package com.nutrisport.core.deeplink.pending

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.nutrisport.shared.navigation.Screen
import kotlin.test.Test

class InMemoryPendingDeeplinkStorageTest {

  @Test
  fun `peek on empty storage returns null`() {
    assertThat(InMemoryPendingDeeplinkStorage().peek()).isNull()
  }

  @Test
  fun `set then peek returns the value without consuming it`() {
    val storage = InMemoryPendingDeeplinkStorage()
    storage.set(Screen.Details(id = "abc"))
    assertThat(storage.peek()).isEqualTo(Screen.Details(id = "abc"))
    assertThat(storage.peek()).isEqualTo(Screen.Details(id = "abc"))
  }

  @Test
  fun `take returns the value and clears the cell`() {
    val storage = InMemoryPendingDeeplinkStorage()
    storage.set(Screen.Profile)
    assertThat(storage.take()).isEqualTo(Screen.Profile)
    assertThat(storage.take()).isNull()
    assertThat(storage.peek()).isNull()
  }

  @Test
  fun `set overrides the previous value`() {
    val storage = InMemoryPendingDeeplinkStorage()
    storage.set(Screen.Profile)
    storage.set(Screen.AdminPanel)
    assertThat(storage.take()).isEqualTo(Screen.AdminPanel)
  }

  @Test
  fun `clear empties the cell`() {
    val storage = InMemoryPendingDeeplinkStorage()
    storage.set(Screen.Details(id = "abc"))
    storage.clear()
    assertThat(storage.peek()).isNull()
    assertThat(storage.take()).isNull()
  }
}
