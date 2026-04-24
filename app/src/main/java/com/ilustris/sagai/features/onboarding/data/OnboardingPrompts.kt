package com.ilustris.sagai.features.onboarding.data

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.onboarding.data.model.OnboardingConfig

data class OnboardingArgs(
    val config: String,
    val persona: String,
)

object OnboardingPrompts {
    const val ONBOARDING_BLUEPRINT = "onboarding_blueprint"
    const val DEFAULT_ROLE_BLUEPRINT = "onboarding_default_role"

    suspend fun getOnboardingPrompt(
        promptService: PromptService,
        config: OnboardingConfig,
        persona: String,
    ): String {
        val args =
            OnboardingArgs(
                config = config.toAINormalize(),
                persona = persona,
            )
        return promptService.buildRemotePrompt(ONBOARDING_BLUEPRINT, args)
    }
}
