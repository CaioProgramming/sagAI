package com.ilustris.sagai.core.ai.model

import com.google.gson.annotations.SerializedName

/**
 * Unified configuration for a genre, fetched from Remote Config.
 * [aesthetic] is the single source of truth for universe context — visual DNA,
 * tone, narrative expectations, and character identity for AI agents.
 * Rendering technique lives in `${genre}_rendering_blueprint`; conversation tone
 * in `${genre}_conversation_blueprint`.
 */
data class GenreConfig(
    @SerializedName("ambientMusicUrl")
    val ambientMusicUrl: String = "",
    @SerializedName("aesthetic")
    val aesthetic: String = "",
    @SerializedName("variations")
    val variations: Map<String, VariationConfig>? = null,
    @SerializedName("companion")
    val companion: CompanionConfig? = null,
    @SerializedName("iconAspectRatio")
    val iconAspectRatio: String? = null,
    @SerializedName("coverAspectRatio")
    val coverAspectRatio: String? = null,
    val imageUrl: String,
) {
    data class CompanionConfig(
        @SerializedName("tone") val tone: String = "",
        @SerializedName("persona") val persona: String = "",
        @SerializedName("conversationalStyle") val conversationalStyle: String = "",
        @SerializedName("interludeStyle") val interludeStyle: String = "",
    )

    data class VariationConfig(
        val name: String = "",
        val description: String = "",
        val aesthetic: String? = null,
        val conversationDirective: String? = null,
    )
}
