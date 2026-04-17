package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.home.data.model.Saga
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
    val sagaContext: String,
)

object ReviewPrompts {
    const val REVIEW_ACTS_INSIGHT_BLUEPRINT = "review_acts_insight_blueprint"
    const val REVIEW_CONCLUSION_BLUEPRINT = "review_conclusion_blueprint"
    const val REVIEW_CONNECTIONS_BLUEPRINT = "review_connections_blueprint"
    const val REVIEW_EXPRESSIVENESS_BLUEPRINT = "review_expressiveness_blueprint"
    const val REVIEW_INTRODUCTION_BLUEPRINT = "review_introduction_blueprint"
    const val REVIEW_PLAYSTYLE_BLUEPRINT = "review_playstyle_blueprint"
}
