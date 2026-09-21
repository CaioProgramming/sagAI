package com.ilustris.sagai.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class TierModelConfigTest {
    @Test
    fun `a bare model name is a single enabled candidate`() {
        val config = "models/gemini-3.1-flash-image".toTierModelConfig()

        assertEquals(listOf("gemini-3.1-flash-image"), config?.candidates)
        assertEquals(true, config?.enabled)
    }

    @Test
    fun `availableModels wins over model and keeps its order`() {
        val config =
            mapOf(
                "model" to "gemini-3.8-flash",
                "availableModels" to listOf("gemini-3.8-flash", "models/gemini-3.7-flash", ""),
            ).toTierModelConfig()

        assertEquals(listOf("gemini-3.8-flash", "gemini-3.7-flash"), config?.candidates)
    }

    @Test
    fun `without availableModels the primary model is the only candidate`() {
        val config = mapOf("model" to "gemini-2.5-flash-preview-tts", "enabled" to false).toTierModelConfig()

        assertEquals(listOf("gemini-2.5-flash-preview-tts"), config?.candidates)
        assertFalse(config!!.enabled)
    }

    @Test
    fun `missing or malformed entries are null`() {
        assertNull(null.toTierModelConfig())
        assertNull(mapOf("enabled" to true).toTierModelConfig())
        assertNull(42.toTierModelConfig())
    }
}
