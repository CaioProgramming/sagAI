package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.GeminiThinkingConfig
import com.ilustris.sagai.core.network.GeminiApiClient
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Each family takes its reasoning control differently, and the expectations below are what a live
 * probe of the API returned, not what the docs imply: Gemini 2.5 answers `thinkingLevel` with a
 * 400, Gemma 4 accepts `minimal` but answers `low` with one, and Gemini 3 takes the level.
 */
class ModelCatalogThinkingConfigTest {
    private val catalog = ModelCatalog(GeminiApiClient(OkHttpClient()))

    @Test
    fun `no requested level means no thinkingConfig, whatever the family`() {
        assertNull(catalog.thinkingConfig("gemini-3.1-flash-lite", null))
        assertNull(catalog.thinkingConfig("gemini-2.5-flash-lite", null))
        assertNull(catalog.thinkingConfig("gemma-4-31b-it", null))
    }

    @Test
    fun `gemini 2_5 never receives a thinkingLevel, minimal becomes a zero budget`() {
        assertEquals(
            GeminiThinkingConfig(thinkingBudget = 0),
            catalog.thinkingConfig("gemini-2.5-flash-lite", "minimal"),
        )
        assertEquals(
            GeminiThinkingConfig(thinkingBudget = 0),
            catalog.thinkingConfig("models/gemini-2.5-flash", "minimal"),
        )
    }

    @Test
    fun `gemini 2_5 keeps its own default for any level above minimal`() {
        listOf("low", "medium", "high").forEach { level ->
            assertNull(catalog.thinkingConfig("gemini-2.5-flash", level))
        }
    }

    @Test
    fun `gemma clamps every level to minimal and asks for no thought summaries`() {
        val expected = GeminiThinkingConfig(includeThoughts = false, thinkingLevel = "minimal")

        listOf("minimal", "low", "medium", "high").forEach { level ->
            assertEquals(expected, catalog.thinkingConfig("gemma-4-31b-it", level))
            assertEquals(expected, catalog.thinkingConfig("models/gemma-4-26b-a4b-it", level))
        }
    }

    @Test
    fun `gemini 3 sends the level as requested`() {
        assertEquals(
            GeminiThinkingConfig(includeThoughts = false, thinkingLevel = "minimal"),
            catalog.thinkingConfig("gemini-3.1-flash-lite", "minimal"),
        )
        assertEquals(
            GeminiThinkingConfig(includeThoughts = true, thinkingLevel = "low"),
            catalog.thinkingConfig("gemini-3.5-flash-lite", "low"),
        )
        assertEquals(
            GeminiThinkingConfig(includeThoughts = true, thinkingLevel = "high"),
            catalog.thinkingConfig("gemini-3.5-flash-lite", "high"),
        )
    }

    @Test
    fun `gemini 3 steps down past a level that model has rejected, only for that model`() {
        catalog.markThinkingLevelUnsupported("gemini-3.7-flash", "minimal")
        catalog.markThinkingLevelUnsupported("gemini-3.7-flash", "high")

        assertEquals(
            GeminiThinkingConfig(includeThoughts = true, thinkingLevel = "low"),
            catalog.thinkingConfig("gemini-3.7-flash", "minimal"),
        )
        assertEquals(
            GeminiThinkingConfig(includeThoughts = true, thinkingLevel = "medium"),
            catalog.thinkingConfig("gemini-3.7-flash", "high"),
        )
        assertEquals(
            GeminiThinkingConfig(includeThoughts = false, thinkingLevel = "minimal"),
            catalog.thinkingConfig("gemini-3.1-flash-lite", "minimal"),
        )
    }
}
