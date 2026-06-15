package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

data class EmotionalToneArgs(
    val availableTones: String,
    val userText: String,
)

object EmotionalPrompt {
    const val EMOTIONAL_TONE_EXTRACTION_BLUEPRINT = "emotional_tone_extraction_blueprint"

    suspend fun emotionalToneExtraction(
        promptService: PromptService,
        userText: String,
    ): SplitPrompt {
        val args =
            EmotionalToneArgs(
                availableTones = EmotionalTone.entries.joinToString { it.name },
                userText = userText,
            )
        return promptService
            .buildSplitBlueprint(EMOTIONAL_TONE_EXTRACTION_BLUEPRINT, args.asMap())
    }
}
