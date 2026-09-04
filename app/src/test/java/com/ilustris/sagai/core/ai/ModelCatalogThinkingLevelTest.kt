package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.network.GeminiApiClient
import okhttp3.OkHttpClient
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `supportsThinkingLevel`/`markThinkingLevelUnsupported` used to be a single boolean per model,
 * covering only `minimal` — the one level a Gemini model had ever been observed to reject. A model
 * swap on a 503 fallback can now hand a request to a model that has never been asked for whichever
 * level it needed (`high`, say), and the old boolean had no way to record a rejection of anything
 * but `minimal` — so a rejected `high` would just be resent, identically, until the retry budget ran
 * out. This covers the generalized per-model-per-level tracking that replaced it.
 */
class ModelCatalogThinkingLevelTest {
    private val catalog = ModelCatalog(GeminiApiClient(OkHttpClient()))

    @Test
    fun `every level is supported until proven otherwise`() {
        assertTrue(catalog.supportsThinkingLevel("gemma-4-31b-it", "high"))
        assertTrue(catalog.supportsThinkingLevel("gemma-4-31b-it", "minimal"))
    }

    @Test
    fun `a rejected level stays rejected for that model, and only that level`() {
        catalog.markThinkingLevelUnsupported("gemma-4-31b-it", "high")

        assertFalse(catalog.supportsThinkingLevel("gemma-4-31b-it", "high"))
        assertTrue(catalog.supportsThinkingLevel("gemma-4-31b-it", "medium"))
        assertTrue(catalog.supportsThinkingLevel("gemma-4-31b-it", "minimal"))
    }

    @Test
    fun `a rejection on one model does not leak onto another`() {
        catalog.markThinkingLevelUnsupported("gemma-4-31b-it", "high")

        assertTrue(catalog.supportsThinkingLevel("gemini-3.5-flash-lite", "high"))
    }

    @Test
    fun `a "models slash" prefix does not create a second identity for the same model`() {
        catalog.markThinkingLevelUnsupported("models/gemma-4-31b-it", "high")

        assertFalse(catalog.supportsThinkingLevel("gemma-4-31b-it", "high"))
    }
}
