package com.ilustris.sagai.features.saga.detail.review.domain.model

import com.ilustris.sagai.core.ai.prompts.ReviewActsInsightArgs
import com.ilustris.sagai.core.ai.prompts.ReviewConclusionArgs
import com.ilustris.sagai.core.ai.prompts.ReviewConnectionsArgs
import com.ilustris.sagai.core.ai.prompts.ReviewExpressivenessArgs
import com.ilustris.sagai.core.ai.prompts.ReviewIntroArgs
import com.ilustris.sagai.core.ai.prompts.ReviewPlaystyleArgs
import com.ilustris.sagai.core.ai.prompts.ReviewPrompts
import com.ilustris.sagai.core.utils.formatDuration
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.rankByHour
import com.ilustris.sagai.features.home.data.model.rankMainCharacterEmotionalTones
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters

enum class ReviewSteps(
    val loadingKey: String,
    val blueprintKey: String,
) {
    INTRO(
        "review_intro_loading",
        ReviewPrompts.REVIEW_INTRODUCTION_BLUEPRINT,
    ),

    EXPRESSIVENESS(
        "review_expressiveness_loading",
        ReviewPrompts.REVIEW_EXPRESSIVENESS_BLUEPRINT,
    ),
    PLAYSTYLE(
        "review_playstyle_loading",
        ReviewPrompts.REVIEW_PLAYSTYLE_BLUEPRINT,
    ),
    CHARACTERS_STEP(
        "review_connections_loading",
        ReviewPrompts.REVIEW_CONNECTIONS_BLUEPRINT,
    ),
    ACTS_INSIGHT(
        "review_acts_insight_loading",
        ReviewPrompts.REVIEW_ACTS_INSIGHT_BLUEPRINT,
    ),
    CONCLUSION(
        "review_conclusion_loading",
        ReviewPrompts.REVIEW_CONCLUSION_BLUEPRINT,
    ),
}

fun ReviewSteps.buildArgs(
    saga: SagaContent,
    conversationDirective: String,
) {
    val playerMessages = saga.flatMessages().filter { it.message.senderType == SenderType.USER }

    val actionCount = playerMessages.count { it.message.text.contains("<action>") }
    val thinkCount = playerMessages.count { it.message.text.contains("<think>") }
    val narratorCount = playerMessages.count { it.message.text.contains("<narrator>") }
    val totalExpressive = actionCount + thinkCount + narratorCount
    val topCharacters =
        saga
            .flatMessages()
            .rankTopCharacters(
                saga.characters
                    .filter { it != saga.mainCharacter }
                    .map { it.data },
            ).take(3)
            .joinToString {
                "${it.first.name} - ${it.second} messages."
            }
    when (this) {
        ReviewSteps.INTRO -> {
            ReviewIntroArgs(
                characterName = saga.mainCharacter?.data?.name!!,
                conversationDirective = conversationDirective,
            )
        }

        ReviewSteps.EXPRESSIVENESS -> {
            ReviewExpressivenessArgs(
                characterName = saga.mainCharacter?.data?.name!!,
                emotionalRank = saga.rankMainCharacterEmotionalTones().toString(),
                emotionalSummary = saga.emotionalSummary(),
                conversationDirective = conversationDirective,
            )
        }

        ReviewSteps.PLAYSTYLE -> {
            ReviewPlaystyleArgs(
                characterName = saga.mainCharacter?.data?.name!!,
                playTime = saga.data.playTimeMs.formatDuration(),
                peakHour = "${saga.rankByHour().maxByOrNull { it.value.size }?.key ?: 0}h",
                interactionCount = totalExpressive.toString(),
                conversationDirective = conversationDirective,
            )
        }

        ReviewSteps.CHARACTERS_STEP -> {
            ReviewConnectionsArgs(
                characterName = saga.mainCharacter?.data?.name!!,
                topBonds = topCharacters,
                conversationDirective = conversationDirective,
            )
        }

        ReviewSteps.ACTS_INSIGHT -> {
            ReviewActsInsightArgs(
                characterName = saga.mainCharacter?.data?.name!!,
                worldHistory =
                    saga.acts.map { it.data }.toAINormalize(
                        listOf("id", "sagaId", "chapterId"),
                    ),
                conversationDirective = conversationDirective,
            )
        }

        ReviewSteps.CONCLUSION -> {
            ReviewConclusionArgs(
                characterName = saga.mainCharacter?.data?.name!!,
                conversationDirective = conversationDirective,
                sagaContext =
                    saga.data.toAINormalize(
                        listOf("id", "mainCharacterId", "currentActId"),
                    ),
            )
        }
    }
}
