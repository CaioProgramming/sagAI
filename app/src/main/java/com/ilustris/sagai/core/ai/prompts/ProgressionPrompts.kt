package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService

data class FeatEvaluationArgs(
    val event: String,
    val participants: String,
    val relationshipChanges: String,
)

data class PlacementArgs(
    val characters: String,
    val levelBands: String,
    val protagonistMaxLevel: Int,
)

object ProgressionPrompts {
    const val PLACEMENT_BLUEPRINT = "character_placement_blueprint"

    const val FEAT_EVALUATION_BLUEPRINT = "character_feat_evaluation_blueprint"

    suspend fun featEvaluation(
        promptService: PromptService,
        args: FeatEvaluationArgs,
    ): SplitPrompt = promptService.buildSplitBlueprint(FEAT_EVALUATION_BLUEPRINT, args)

    suspend fun placement(
        promptService: PromptService,
        args: PlacementArgs,
    ): SplitPrompt = promptService.buildSplitBlueprint(PLACEMENT_BLUEPRINT, args)
}
