package com.nutrisport.analytics.firebase

import com.nutrisport.analytics.core.AuthEvent
import com.nutrisport.analytics.core.EcommerceEvent
import com.nutrisport.analytics.firebase.mapper.AuthFirebaseMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthFirebaseMapperTest {

    private val mapper = AuthFirebaseMapper()

    @Test
    fun `should map SignIn event`() {
        val event = AuthEvent.SignIn(method = "google")
        val result = mapper.map(event)

        assertNotNull(result)
        assertEquals("sign_in", result.eventName)
        assertEquals("google", result.params["method"])
    }

    @Test
    fun `should map SignOut event`() {
        val result = mapper.map(AuthEvent.SignOut)

        assertNotNull(result)
        assertEquals("sign_out", result.eventName)
        assertEquals(emptyMap(), result.params)
    }

    @Test
    fun `should return null for non-auth event`() {
        val event = EcommerceEvent.Search(query = "test")
        assertNull(mapper.map(event))
    }
}
