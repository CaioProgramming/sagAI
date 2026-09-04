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

    /**
     * Explains why the app runs on the user's own Gemini key, and collects it.
     *
     * Content comes from `onboarding_fallbacks` like every other type, so the copy can be
     * reworded without a release — which matters more here than anywhere else, since this is
     * the screen standing between a new user and the app.
     */
    API_KEY_SETUP("onboarding_api_key_seen", "onboarding_api_key"),
}
