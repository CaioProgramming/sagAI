package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

data class ReviewIntroArgs(
    val characterName: String,
    val conversationDirective: String,
)

data class ReviewPlaystyleArgs(
    val characterName: String,
    val playTime: String,
    val peakHour: String,
    val interactionCount: String,
    val conversationDirective: String,
)

data class ReviewExpressivenessArgs(
    val characterName: String,
    val emotionalRank: String,
    val emotionalSummary: String,
    val conversationDirective: String,
)

data class ReviewConnectionsArgs(
    val characterName: String,
    val topBonds: String,
    val conversationDirective: String,
)

data class ReviewActsInsightArgs(
    val characterName: String,
    val worldHistory: String,
    val conversationDirective: String,
)

data class ReviewConclusionArgs(
    val characterName: String,
    val conversationDirective: String,
)

object ReviewPrompts {
    const val REVIEW_ACTS_INSIGHT_BLUEPRINT = "review_acts_insight_blueprint"
    const val REVIEW_CONCLUSION_BLUEPRINT = "review_conclusion_blueprint"
    const val REVIEW_CONNECTIONS_BLUEPRINT = "review_connections_blueprint"
    const val REVIEW_EXPRESSIVENESS_BLUEPRINT = "review_expressiveness_blueprint"
    const val REVIEW_INTRODUCTION_BLUEPRINT = "review_introduction_blueprint"
    const val REVIEW_PLAYSTYLE_BLUEPRINT = "review_playstyle_blueprint"

    suspend fun introductionPrompt(
        promptService: PromptService,
        saga: SagaContent,
        config: GenreConfig?,
    ): String {
        val args =
            ReviewIntroArgs(
                characterName = saga.mainCharacter?.data?.name ?: "buddy",
                conversationDirective = "",
            )
        return promptService.buildRemotePrompt(REVIEW_INTRODUCTION_BLUEPRINT, args)
    }

    suspend fun playstylePrompt(
        promptService: PromptService,
        saga: SagaContent,
        config: GenreConfig?,
        playTime: String,
        mostActiveHour: Int,
        totalExpressive: Int,
    ): String {
        val args =
            ReviewPlaystyleArgs(
                characterName = saga.mainCharacter?.data?.name ?: "buddy",
                playTime = playTime,
                peakHour = "${mostActiveHour}h",
                interactionCount = totalExpressive.toString(),
                conversationDirective = "",
            )
        return promptService.buildRemotePrompt(REVIEW_PLAYSTYLE_BLUEPRINT, args)
    }

    suspend fun expressivenessPrompt(
        promptService: PromptService,
        saga: SagaContent,
        config: GenreConfig?,
        emotionalRank: List<Pair<EmotionalTone, Int>>,
    ): String {
        val args =
            ReviewExpressivenessArgs(
                characterName = saga.mainCharacter?.data?.name ?: "buddy",
                emotionalRank = emotionalRank.joinToString { it.first.name },
                emotionalSummary = saga.emotionalSummary(),
                conversationDirective = "",
            )
        return promptService.buildRemotePrompt(REVIEW_EXPRESSIVENESS_BLUEPRINT, args)
    }

    suspend fun connectionsPrompt(
        promptService: PromptService,
        saga: SagaContent,
        config: GenreConfig?,
        topCharacters: List<Pair<String, Int>>,
    ): String {
        val args =
            ReviewConnectionsArgs(
                characterName = saga.mainCharacter?.data?.name ?: "buddy",
                topBonds = topCharacters.joinToString { it.first },
                conversationDirective = "",
            )
        return promptService.buildRemotePrompt(REVIEW_CONNECTIONS_BLUEPRINT, args)
    }

    suspend fun actsInsightPrompt(
        promptService: PromptService,
        saga: SagaContent,
        config: GenreConfig?,
    ): String {
        val args =
            ReviewActsInsightArgs(
                characterName = saga.mainCharacter?.data?.name ?: "buddy",
                worldHistory = saga.acts.joinToString { it.data.title },
                conversationDirective = "",
            )
        return promptService.buildRemotePrompt(REVIEW_ACTS_INSIGHT_BLUEPRINT, args)
    }

    suspend fun conclusionPrompt(
        promptService: PromptService,
        saga: SagaContent,
        config: GenreConfig?,
    ): String {
        val args =
            ReviewConclusionArgs(
                characterName = saga.mainCharacter?.data?.name ?: "buddy",
                conversationDirective = "",
            )
        return promptService.buildRemotePrompt(REVIEW_CONCLUSION_BLUEPRINT, args)
    }
}
