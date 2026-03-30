package com.ilustris.sagai.features.onboarding.data

data class OnboardingConfig(
    val context: String = "",
    val keyPoints: List<String> = emptyList(),
    val tone: String = "casual",
)

data class OnboardingContent(
    val pages: List<OnboardingPage> = emptyList(),
)

data class OnboardingPage(
    val title: String = "",
    val description: String = "",
    val emoji: String = "",
)

data class OnboardingAsset(
    val image: String,
)

enum class OnboardingType(
    val preferenceKey: String,
    val configKey: String,
) {
    APP_INTRO("onboarding_app_intro_seen", "onboarding_app_intro"),
    CREATION_GUIDE("onboarding_creation_guide_seen", "onboarding_creation_guide"),
    GAMEPLAY_GUIDE("onboarding_gameplay_guide_seen", "onboarding_gameplay_guide"),
    PREMIUM_GUIDE("onboarding_premium_guide_seen", "onboarding_premium_guide"),
}
