package com.ilustris.sagai.core.data

import com.ilustris.sagai.core.ai.model.SafeGuard

sealed class SideEffect {
    object ShowPremiumOnboarding : SideEffect()

    data class GuardrailBlock(
        val status: SafeGuard,
    ) : SideEffect()

    data class ShowImage(
        val bitmap: android.graphics.Bitmap,
    ) : SideEffect()

    data class ShowAILoader(
        val message: String,
    ) : SideEffect()
}
