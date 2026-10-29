package com.ilustris.sagai.core.network

import com.google.gson.JsonParser
import com.ilustris.sagai.core.ai.model.GeminiContent
import com.ilustris.sagai.core.ai.model.GeminiGenerationConfig
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.GeminiThinkingConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GeminiApiCodecThinkingTest {
    private fun thinkingJson(config: GeminiThinkingConfig?) =
        GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = "hi")))),
            generationConfig = GeminiGenerationConfig(thinkingConfig = config),
        ).let { request ->
            JsonParser
                .parseString(GeminiApiCodec.encodeRequestJson(request))
                .asJsonObject
                .getAsJsonObject("generationConfig")
        }

    @Test
    fun `a zero thinkingBudget is written, not dropped as falsy`() {
        val thinking = thinkingJson(GeminiThinkingConfig(thinkingBudget = 0)).getAsJsonObject("thinkingConfig")

        assertEquals(0, thinking.get("thinkingBudget").asInt)
        assertFalse(thinking.has("thinkingLevel"))
        assertFalse(thinking.has("includeThoughts"))
    }

    @Test
    fun `a level config carries no budget`() {
        val thinking =
            thinkingJson(GeminiThinkingConfig(includeThoughts = false, thinkingLevel = "minimal"))
                .getAsJsonObject("thinkingConfig")

        assertEquals("minimal", thinking.get("thinkingLevel").asString)
        assertFalse(thinking.get("includeThoughts").asBoolean)
        assertFalse(thinking.has("thinkingBudget"))
    }

    @Test
    fun `no config omits thinkingConfig entirely`() {
        assertFalse(thinkingJson(null).has("thinkingConfig"))
    }
}
