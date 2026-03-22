package com.nutrisport.analytics.core

import kotlin.test.Test
import kotlin.test.assertIs

class DebugAnalyticsProcessorTest {

    @Test
    fun `should have correct key`() {
        val processor = DebugAnalyticsProcessor()
        assertIs<String>(processor.key)
        kotlin.test.assertEquals("debug", processor.key)
    }

    @Test
    fun `should not throw when logging event`() {
        // Arrange
        val processor = DebugAnalyticsProcessor()

        // Act & Assert — no exception
        processor.logEvent(NavigationEvent.ScreenView("Test"))
    }

    @Test
    fun `should return self from setEnabled`() {
        val processor = DebugAnalyticsProcessor()
        val result = processor.setEnabled(false)
        assertIs<DebugAnalyticsProcessor>(result)
    }

    @Test
    fun `should return self from setLoggingEnabled`() {
        val processor = DebugAnalyticsProcessor()
        val result = processor.setLoggingEnabled(false)
        assertIs<DebugAnalyticsProcessor>(result)
    }
}
