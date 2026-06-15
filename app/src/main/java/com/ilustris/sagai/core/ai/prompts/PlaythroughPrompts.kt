package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.playthrough.data.model.SagaPlaythrough

data class PlaythroughReviewArgs(
    val emotionalSummary: String,
)

object PlaythroughPrompts {
    const val PLAYTHROUGH_REVIEW_BLUEPRINT = "extract_playthrough_review_blueprint"

    suspend fun playthroughReviewPrompt(
        promptService: PromptService,
        sagas: List<SagaPlaythrough>,
    ): SplitPrompt {
        val args = PlaythroughReviewArgs(emotionalSummary = extractEmotionalSummary(sagas))

        return promptService.buildSplitBlueprint(
            PLAYTHROUGH_REVIEW_BLUEPRINT,
            args.asMap(),
        )
    }

    private fun extractEmotionalSummary(sagas: List<SagaPlaythrough>): String =
        sagas.joinToString { playthrough ->
            val saga = playthrough.data
            buildString {
                appendLine("Saga: ${saga.title} (${saga.genre})")
                appendLine("Playtime: ${saga.playTimeMs}ms")

                saga.emotionalProfile?.let { profile ->
                    appendLine(profile.toAINormalize())
                } ?: run {
                    val validActs =
                        playthrough.acts.filter { it.data.emotionalReview != null }
                    if (validActs.isEmpty()) {
                        appendLine("No valid emotional reviews")
                    } else {
                        validActs.joinToString {
                            appendLine("Act: ${it.data.title}")
                            appendLine(it.data.emotionalReview)
                        }
                    }
                }
            }
        }
}
