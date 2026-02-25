package com.ilustris.sagai.core.ai.model

import com.google.gson.annotations.SerializedName

/**
 * Defines the strict framing, composition, and agent templates for a specific image type (e.g., ICON, COVER).
 */
data class ImageTypeConfig(
    @SerializedName("director")
    val director: String = "",
    @SerializedName("artist")
    val artist: String = "",
    @SerializedName("reviewer")
    val reviewer: String = "",
) {
    /**
     * Helper logic to grab an agent's string without reflection, scaling for any future agents.
     */
    fun getAgentTemplate(agentId: String): String =
        when (agentId) {
            "director" -> director
            "artist" -> artist
            "reviewer" -> reviewer
            else -> ""
        }
}

/**
 * Dynamic configuration for the image generation pipeline, fetched from Remote Config.
 */
data class ImageConfig(
    @SerializedName("criticalRules")
    val criticalRules: String = "",
    @SerializedName("typeConfigs")
    val typeConfigs: Map<String, ImageTypeConfig> = emptyMap(),
)
