package com.nutrisport.analytics.firebase

import com.nutrisport.analytics.core.AuthEvent
import com.nutrisport.analytics.core.NavigationEvent
import com.nutrisport.analytics.firebase.mapper.NavigationFirebaseMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NavigationFirebaseMapperTest {

    private val mapper = NavigationFirebaseMapper()

    @Test
    fun `should map ScreenView event`() {
        val event = NavigationEvent.ScreenView(screenName = "Cart")
        val result = mapper.map(event)

        assertNotNull(result)
        assertEquals("screen_view", result.eventName)
        assertEquals("Cart", result.params["screen_name"])
    }

    @Test
    fun `should return null for non-navigation event`() {
        val event = AuthEvent.SignOut
        assertNull(mapper.map(event))
    }
}
