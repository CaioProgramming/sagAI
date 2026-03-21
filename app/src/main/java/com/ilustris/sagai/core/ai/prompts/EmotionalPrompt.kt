package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

data class EmotionalToneArgs(
    val roleDefinition: String,
    val availableTones: String,
    val userText: String,
)

data class EmotionalReviewArgs(
    val roleDefinition: String,
    val sagaMainContext: String,
    val analysisData: String,
    val conversationDirective: String,
)

data class EmotionalConclusionArgs(
    val roleDefinition: String,
    val sagaMainContext: String,
    val emotionalJourneySummary: String,
    val conversationDirective: String,
)

data class EmotionalProfileArgs(
    val roleDefinition: String,
    val behaviorNotes: String,
    val conversationDirective: String,
)

object EmotionalPrompt {
    suspend fun emotionalToneExtraction(
        promptService: PromptService,
        promptDirectives: PromptDirectives,
        userText: String,
    ): String {
        val roleDefinition =
            promptService.buildPrompt(
                promptDirectives.roleEmotionalReviewer,
                emptyMap(),
            )
        val args =
            EmotionalToneArgs(
                roleDefinition = roleDefinition,
                availableTones = EmotionalTone.entries.joinToString { it.name },
                userText = userText,
            )
        return promptService.buildRemotePrompt("emotional_tone_extraction_blueprint", args)
    }

    suspend fun generateEmotionalReview(
        promptService: PromptService,
        promptDirectives: PromptDirectives,
        saga: SagaContent,
        context: String,
        conversationDirective: String,
    ): String {
        val roleDefinition =
            promptService.buildPrompt(
                promptDirectives.roleEmotionalReviewer,
                mapOf(
                    "sagaTitle" to saga.data.title,
                    "conversationDirective" to conversationDirective,
                ),
            )
        val args =
            EmotionalReviewArgs(
                roleDefinition = roleDefinition,
                sagaMainContext = SagaPrompts.mainContext(saga),
                analysisData = context,
                conversationDirective = conversationDirective,
            )
        return promptService.buildRemotePrompt("emotional_review_blueprint", args)
    }

    suspend fun generateEmotionalConclusion(
        promptService: PromptService,
        promptDirectives: PromptDirectives,
        saga: SagaContent,
        conversationDirective: String,
    ): String {
        val roleDefinition =
            promptService.buildPrompt(
                promptDirectives.roleEmotionalCounselor,
                mapOf(
                    "sagaTitle" to saga.data.title,
                    "conversationDirective" to conversationDirective,
                ),
            )
        val args =
            EmotionalConclusionArgs(
                roleDefinition = roleDefinition,
                sagaMainContext = SagaPrompts.mainContext(saga),
                emotionalJourneySummary = saga.emotionalSummary(),
                conversationDirective = conversationDirective,
            )
        return promptService.buildRemotePrompt("emotional_conclusion_blueprint", args)
    }

    suspend fun generateEmotionalProfile(
        promptService: PromptService,
        promptDirectives: PromptDirectives,
        saga: SagaContent,
        summary: String,
        conversationDirective: String,
    ): String {
        val roleDefinition =
            promptService.buildPrompt(
                promptDirectives.roleEmotionalReviewer,
                mapOf(
                    "sagaTitle" to saga.data.title,
                    "conversationDirective" to conversationDirective,
                ),
            )
        val args =
            EmotionalReviewArgs(
                roleDefinition = roleDefinition,
                sagaMainContext = SagaPrompts.mainContext(saga),
                analysisData = summary,
                conversationDirective = conversationDirective,
            )
        return promptService.buildRemotePrompt("emotional_review_blueprint", args)
    }
}
