package com.ilustris.sagai.features.onboarding.data

data class OnboardingAsset(
    val image: String,
)

enum class OnboardingType(
    val preferenceKey: String,
    val configKey: String,
) {
    APP_INTRO("onboarding_app_intro_seen", "onboarding_app_intro"),
    CREATION_GUIDE("onboarding_creation_guide_seen", "onboarding_creation_guide"),
    GAMEPLAY_GUIDE("tutorials_enabled", "onboarding_gameplay_guide"),
    PREMIUM_GUIDE("onboarding_premium_guide_seen", "onboarding_premium_guide"),
}
