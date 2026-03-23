package com.nutrisport.analytics.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NutriSportAnalyticsImplTest {

    @Test
    fun `should dispatch event to all registered processors`() = runTest {
        // Arrange
        val processor1 = FakeAnalyticsProcessor(key = "fake1")
        val processor2 = FakeAnalyticsProcessor(key = "fake2")
        val analytics = NutriSportAnalyticsImpl()
        analytics.addProcessor(processor1)
        analytics.addProcessor(processor2)

        // Act
        analytics.logEvent(NavigationEvent.ScreenView("Home"))

        // Assert
        assertEquals(1, processor1.events.size)
        assertEquals(1, processor2.events.size)
        assertEquals("screen_view", processor1.events.first().name)
    }

    @Test
    fun `should not fail when no processors registered`() = runTest {
        // Arrange
        val analytics = NutriSportAnalyticsImpl()

        // Act & Assert — no exception
        analytics.logEvent(AuthEvent.SignIn())
    }

    @Test
    fun `should replace processor with same key`() = runTest {
        // Arrange
        val processor1 = FakeAnalyticsProcessor(key = "same")
        val processor2 = FakeAnalyticsProcessor(key = "same")
        val analytics = NutriSportAnalyticsImpl()
        analytics.addProcessor(processor1)
        analytics.addProcessor(processor2)

        // Act
        analytics.logEvent(AuthEvent.SignOut)

        // Assert
        assertEquals(0, processor1.events.size)
        assertEquals(1, processor2.events.size)
    }
}
