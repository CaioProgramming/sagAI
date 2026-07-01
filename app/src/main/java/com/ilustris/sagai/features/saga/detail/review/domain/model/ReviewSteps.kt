package com.ilustris.sagai.features.saga.detail.review.domain.model

import com.ilustris.sagai.core.ai.ModelRequirement
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
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage

enum class ReviewSteps(
    val loadingKey: String,
    val blueprintKey: String,
    val requirement: ModelRequirement,
) {
    INTRO(
        "review_intro_loading",
        ReviewPrompts.REVIEW_INTRODUCTION_BLUEPRINT,
        ModelRequirement.MEDIUM,
    ),

    EXPRESSIVENESS(
        "review_expressiveness_loading",
        ReviewPrompts.REVIEW_EXPRESSIVENESS_BLUEPRINT,
        ModelRequirement.MEDIUM,
    ),
    PLAYSTYLE(
        "review_playstyle_loading",
        ReviewPrompts.REVIEW_PLAYSTYLE_BLUEPRINT,
        ModelRequirement.LOW,
    ),
    CHARACTERS_STEP(
        "review_connections_loading",
        ReviewPrompts.REVIEW_CONNECTIONS_BLUEPRINT,
        ModelRequirement.MEDIUM,
    ),
    ACTS_INSIGHT(
        "review_acts_insight_loading",
        ReviewPrompts.REVIEW_ACTS_INSIGHT_BLUEPRINT,
        ModelRequirement.MEDIUM,
    ),
    CONCLUSION(
        "review_conclusion_loading",
        ReviewPrompts.REVIEW_CONCLUSION_BLUEPRINT,
        ModelRequirement.MEDIUM,
    ),
}

fun ReviewSteps.isPresentIn(review: Review?): Boolean =
    when (this) {
        ReviewSteps.INTRO -> review?.introduction != null
        ReviewSteps.EXPRESSIVENESS -> review?.expressiveness != null
        ReviewSteps.PLAYSTYLE -> review?.playstyle != null
        ReviewSteps.CHARACTERS_STEP -> review?.topCharacters != null
        ReviewSteps.ACTS_INSIGHT -> review?.actsInsight != null
        ReviewSteps.CONCLUSION -> review?.conclusion != null
    }

fun Review.withStep(
    step: ReviewSteps,
    stage: ReviewStage,
): Review {
    val base = this
    return when (step) {
        ReviewSteps.INTRO -> base.copy(introduction = stage)
        ReviewSteps.EXPRESSIVENESS -> base.copy(expressiveness = stage)
        ReviewSteps.PLAYSTYLE -> base.copy(playstyle = stage)
        ReviewSteps.CHARACTERS_STEP -> base.copy(topCharacters = stage)
        ReviewSteps.ACTS_INSIGHT -> base.copy(actsInsight = stage)
        ReviewSteps.CONCLUSION -> base.copy(conclusion = stage)
    }
}

fun buildReviewFromSteps(stages: Map<ReviewSteps, ReviewStage>): Review =
    Review(
        introduction = stages[ReviewSteps.INTRO],
        expressiveness = stages[ReviewSteps.EXPRESSIVENESS],
        playstyle = stages[ReviewSteps.PLAYSTYLE],
        topCharacters = stages[ReviewSteps.CHARACTERS_STEP],
        actsInsight = stages[ReviewSteps.ACTS_INSIGHT],
        conclusion = stages[ReviewSteps.CONCLUSION],
    )

fun ReviewSteps.buildArgs(
    saga: SagaContent,
    conversationDirective: String,
): Any {
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
    return when (this) {
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
