package com.ilustris.sagai.core.data

sealed class SideEffect {
    object ShowPremiumOnboarding : SideEffect()

    data class ContentViolation(
        val message: String,
    ) : SideEffect()

    data class ShowImage(
        val bitmap: android.graphics.Bitmap,
    ) : SideEffect()

    data class ShowAILoader(
        val message: String,
    ) : SideEffect()
}
