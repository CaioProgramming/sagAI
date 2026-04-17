package com.ilustris.sagai.features.onboarding.data.model

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
