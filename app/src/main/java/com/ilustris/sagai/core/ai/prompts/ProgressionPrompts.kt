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

data class HonorsArgs(
    val honorKind: String,
    val scope: String,
    val feats: String,
    val medalsOfTheAct: String,
    val previousLegacies: String = "",
)

data class StatusArgs(
    val characters: String,
    val events: String,
)

object ProgressionPrompts {
    const val STATUS_BLUEPRINT = "character_status_blueprint"

    const val HONORS_BLUEPRINT = "character_honors_blueprint"

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

    suspend fun honors(
        promptService: PromptService,
        args: HonorsArgs,
    ): SplitPrompt = promptService.buildSplitBlueprint(HONORS_BLUEPRINT, args)

    suspend fun status(
        promptService: PromptService,
        args: StatusArgs,
    ): SplitPrompt = promptService.buildSplitBlueprint(STATUS_BLUEPRINT, args)
}
