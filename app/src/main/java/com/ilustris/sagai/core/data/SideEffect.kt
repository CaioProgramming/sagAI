package com.ilustris.sagai.core.data

import com.ilustris.sagai.core.ai.model.SafeGuard

sealed class SideEffect {
    object ShowPremiumOnboarding : SideEffect()

    data class GuardrailBlock(
        val status: SafeGuard,
    ) : SideEffect()

    /**
     * The user's Google Cloud project is on a tier that doesn't include this capability — image
     * generation is "Not available" on the Gemini free tier for every image model.
     *
     * A one-shot effect rather than persisted state, unlike a rejected key: this is tied to the
     * action the user just took, so they are by definition looking at the app when it fires. It
     * also must never mark the key invalid — the same key still generates text perfectly well.
     */
    data class FeatureNeedsBilling(
        val feature: BillableFeature,
    ) : SideEffect()
}

/** Capabilities the Gemini free tier excludes, so the message can name the right one. */
enum class BillableFeature {
    IMAGE_GENERATION,
}
