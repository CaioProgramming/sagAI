package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

data class EmotionalToneArgs(
    val availableTones: String,
    val userText: String,
)

data class EmotionalReviewArgs(
    val sagaMainContext: String,
    val analysisData: String,
    val conversationDirective: String,
)

data class EmotionalConclusionArgs(
    val sagaMainContext: String,
    val emotionalJourneySummary: String,
    val conversationDirective: String,
)

data class EmotionalProfileArgs(
    val behaviorNotes: String,
    val conversationDirective: String,
)

object EmotionalPrompt {
    suspend fun emotionalToneExtraction(
        promptService: PromptService,
        userText: String,
    ): String {
        val args =
            EmotionalToneArgs(
                availableTones = EmotionalTone.entries.joinToString { it.name },
                userText = userText,
            )
        return promptService.buildRemotePrompt("emotional_tone_extraction_prompt", args)
    }

    suspend fun generateEmotionalReview(
        promptService: PromptService,
        saga: SagaContent,
        context: String,
        config: GenreConfig,
    ): String {
        val args =
            EmotionalReviewArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                analysisData = context,
                conversationDirective = config.conversationDirective,
            )
        return promptService.buildRemotePrompt("emotional_review_prompt", args)
    }

    suspend fun generateEmotionalConclusion(
        promptService: PromptService,
        saga: SagaContent,
        config: GenreConfig,
    ): String {
        val args =
            EmotionalConclusionArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                emotionalJourneySummary = saga.emotionalSummary(),
                conversationDirective = config.conversationDirective,
            )
        return promptService.buildRemotePrompt("emotional_conclusion_prompt", args)
    }

    suspend fun generateEmotionalProfile(
        promptService: PromptService,
        summary: String,
        config: GenreConfig,
    ): String {
        val args =
            EmotionalProfileArgs(
                behaviorNotes = summary,
                conversationDirective = config.conversationDirective,
            )
        return promptService.buildRemotePrompt("emotional_profile_prompt", args)
    }
}
