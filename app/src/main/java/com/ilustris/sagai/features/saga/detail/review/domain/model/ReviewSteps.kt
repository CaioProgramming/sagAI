package com.ilustris.sagai.features.saga.detail.review.domain.model

import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.prompts.ActPrompts
import com.ilustris.sagai.core.ai.prompts.ReviewPrompts
import com.ilustris.sagai.core.utils.formatDuration
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.historySummary
import com.ilustris.sagai.features.home.data.model.rankByHour
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage

enum class ReviewSteps(
    val blueprintKey: String,
    val requirement: ModelRequirement,
) {
    INTRO(
        ReviewPrompts.REVIEW_INTRODUCTION_BLUEPRINT,
        ModelRequirement.MEDIUM,
    ),

    EXPRESSIVENESS(
        ReviewPrompts.REVIEW_EXPRESSIVENESS_BLUEPRINT,
        ModelRequirement.MEDIUM,
    ),
    PLAYSTYLE(
        ReviewPrompts.REVIEW_PLAYSTYLE_BLUEPRINT,
        ModelRequirement.LOW,
    ),
    CHARACTERS_STEP(
        ReviewPrompts.REVIEW_CONNECTIONS_BLUEPRINT,
        ModelRequirement.MEDIUM,
    ),
    ACTS_INSIGHT(
        ReviewPrompts.REVIEW_ACTS_INSIGHT_BLUEPRINT,
        ModelRequirement.MEDIUM,
    ),
    CONCLUSION(
        ReviewPrompts.REVIEW_CONCLUSION_BLUEPRINT,
        ModelRequirement.MEDIUM,
    ),

    /**
     * The Send-Off — a farewell message per top character. Unlike every other step,
     * its AI response is a [com.ilustris.sagai.features.saga.detail.data.model.FarewellSet]
     * (a list), not a single [ReviewStage], so [SagaReviewUseCaseImpl.generateStep]
     * special-cases it before reaching the generic `ReviewStage` path — this step's
     * branches below ([isPresentIn] aside) are never actually exercised, but must exist
     * for these `when` blocks to stay exhaustive.
     */
    FAREWELLS(
        ReviewPrompts.REVIEW_FAREWELLS_BLUEPRINT,
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
        ReviewSteps.FAREWELLS -> review?.farewells != null
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
        // Farewells persists its own FarewellSet directly in SagaReviewUseCaseImpl.generateStep;
        // this branch is unreachable.
        ReviewSteps.FAREWELLS -> base
    }
}

suspend fun ReviewSteps.buildArgs(saga: SagaContent): Map<String, String> {
    val topCharacters =
        saga
            .flatMessages()
            .rankTopCharacters(
                saga.characters
                    .filter { it != saga.mainCharacter }
                    .map { it.data },
            ).take(3)
            .joinToString {
                "${it.first.fullName()} - ${it.second} messages."
            }
    return when (this) {
        ReviewSteps.INTRO -> {
            buildMap {
                put("emotionalProfile", saga.data.emotionalReview.orEmpty())
                put("playTime", saga.data.playTimeMs.formatDuration())
                put("endMessage", saga.data.endMessage)
            }
        }

        ReviewSteps.EXPRESSIVENESS -> {
            buildMap {
                put("emotionalJourney", saga.emotionalSummary())
            }
        }

        ReviewSteps.PLAYSTYLE -> {
            buildMap {
                put(
                    "mostTimePlaying",
                    "${saga.rankByHour().maxByOrNull { it.value.size }?.key ?: 0}h",
                )
                put(
                    "MainCharacterArcs",
                    saga.mainCharacter!!.arcs.normalizetoAIItems(
                        listOf("id", "characterId", "sourceId"),
                    ),
                )
            }
        }

        ReviewSteps.CHARACTERS_STEP -> {
            buildMap {
                put("CharacterRanking", topCharacters)
                put("mainCharacterRelationships", saga.mainCharacter!!.summarizeRelationships(6))
            }
        }

        ReviewSteps.ACTS_INSIGHT -> {
            buildMap {
                put(
                    "FullStory",
                    saga.acts.map { it.data }.normalizetoAIItems(ActPrompts.ACT_EXCLUSIONS),
                )
            }
        }

        ReviewSteps.CONCLUSION -> {
            buildMap {
                put("FullStory", saga.historySummary())
                put("endMessage", saga.data.endMessage)
            }
        }

        // Farewells builds its own args (top character context) directly in
        // SagaReviewUseCaseImpl.generateStep; this branch is unreachable.
        ReviewSteps.FAREWELLS -> emptyMap()
    }
}
