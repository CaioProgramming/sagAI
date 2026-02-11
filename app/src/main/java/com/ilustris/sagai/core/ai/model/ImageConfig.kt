package com.ilustris.sagai.core.ai.model

import com.google.gson.annotations.SerializedName

/**
 * Dynamic configuration for the image generation pipeline, fetched from Remote Config.
 * This allows for real-time updates to generation rules, type descriptions, and reviewer scopes.
 */
data class ImageConfig(
    @SerializedName("criticalRules")
    val criticalRules: String = "",
    @SerializedName("typeDescriptions")
    val typeDescriptions: Map<String, String> = emptyMap(),
    @SerializedName("directorialVisionRules")
    val directorialVisionRules: String = "",
    @SerializedName("artistPromptRules")
    val artistPromptRules: String = "",
    @SerializedName("reviewerScopes")
    val reviewerScopes: String = "",
)
