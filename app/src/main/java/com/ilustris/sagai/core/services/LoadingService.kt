package com.ilustris.sagai.core.services

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.LoadingPrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.onboarding.data.OnboardingPrompts

class LoadingService(
    private val gemmaClient: GemmaClient,
    private val promptService: PromptService,
) {
    suspend fun generateLoadingMessage(
        loadingType: LoadingType,
        conversationStyle: String? = null,
    ): String? {
        val conversation =
            conversationStyle
                ?: promptService.buildRemotePrompt(OnboardingPrompts.DEFAULT_ROLE_BLUEPRINT)
        val loadingBlueprint =
            promptService.buildRemotePrompt(
                LOADING_BLUEPRINT,
                LoadingPrompts.LoadingArgs(
                    loadingType.task,
                    conversation,
                ),
            )

        return gemmaClient.generate<String>(loadingBlueprint, temperatureRandomness = 1f)
    }
}

class LoadingType(
    val task: String,
)

private const val LOADING_BLUEPRINT = "loading_blueprint"
