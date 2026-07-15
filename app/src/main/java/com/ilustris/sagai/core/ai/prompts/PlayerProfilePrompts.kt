package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService

data class ActProfileUpdateArgs(
    val currentPlayerProfile: String,
    val actInsight: String,
    val sagaGenre: String,
    val sagaId: String,
    val actId: String,
    val userName: String,
)

object PlayerProfilePrompts {
    const val ACT_PROFILE_UPDATE_BLUEPRINT = "act_profile_update_blueprint"

    suspend fun generateActProfileUpdate(
        promptService: PromptService,
        currentPlayerProfile: String,
        actInsight: String,
        sagaGenre: String,
        sagaId: Int,
        actId: Int,
        userName: String,
    ): SplitPrompt {
        val args =
            ActProfileUpdateArgs(
                currentPlayerProfile = currentPlayerProfile,
                actInsight = actInsight,
                sagaGenre = sagaGenre,
                sagaId = sagaId.toString(),
                actId = actId.toString(),
                userName = userName,
            )

        return promptService.buildSplitBlueprint(ACT_PROFILE_UPDATE_BLUEPRINT, args)
    }
}


