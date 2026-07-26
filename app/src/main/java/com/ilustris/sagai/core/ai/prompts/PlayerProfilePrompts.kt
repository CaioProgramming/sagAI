package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.player.data.model.PlayerProfileData

data class ActProfileUpdateArgs(
    val currentPlayerProfile: String,
    val actInsight: String,
    val sagaGenre: String,
    val sagaId: String,
    val actId: String,
    val userName: String,
)

data class SagaProfileUpdateArgs(
    val currentPlayerProfile: String,
    val sagaConclusion: String,
    val emotionalReview: String,
    val sagaGenre: String,
    val sagaId: String,
    val userName: String,
)

object PlayerProfilePrompts {
    const val ACT_PROFILE_UPDATE_BLUEPRINT = "act_profile_update_blueprint"
    const val SAGA_PROFILE_UPDATE_BLUEPRINT = "saga_profile_update_blueprint"

    val SAGA_PROFILE_EXCLUSIONS =
        listOf(
            "id",
            "icon",
            "createdAt",
            "mainCharacterId",
            "isDebug",
            "currentActId",
            "endedAt",
            "review",
            "emotionalReview",
            "isEnded",
            "playTimeMs",
        )

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

    suspend fun generateSagaProfileUpdate(
        promptService: PromptService,
        currentPlayerProfile: PlayerProfileData,
        saga: Saga,
        userName: String,
    ): SplitPrompt {
        val args =
            buildMap {
                put(
                    "currentPlayerProfile",
                    buildMap {
                        put("userName", userName)
                        putAll(currentPlayerProfile.asMap())
                    }.toAINormalize(),
                )
                put(
                    "sagaContext",
                    saga.toAINormalize(
                        SAGA_PROFILE_EXCLUSIONS,
                    ),
                )
            }

        return promptService.buildSplitBlueprint(ACT_PROFILE_UPDATE_BLUEPRINT, args)
    }
}
