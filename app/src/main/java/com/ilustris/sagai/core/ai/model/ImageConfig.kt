package com.ilustris.sagai.core.ai.model

import com.google.gson.annotations.SerializedName
import com.ilustris.sagai.core.ai.prompts.AgentIds

/**
 * Defines the blueprint keys and aspect ratio for a specific image type (e.g., ICON, COVER).
 *
 * Each agent field now holds a Firebase Remote Config blueprint key (e.g. "icon_director_blueprint")
 * instead of a raw template string. This aligns image generation with the modular
 * PromptBlueprint architecture used across all other AI systems.
 */
data class ImageTypeConfig(
    @SerializedName("director")
    val director: String = "",
    @SerializedName("artist")
    val artist: String = "",
    @SerializedName("reviewer")
    val reviewer: String = "",
    @SerializedName("aspectRatio")
    val aspectRatio: String? = null,
) {
    /**
     * Returns the Firebase Remote Config blueprint key for a given agent role.
     * e.g. agentId "director" for ICON → "icon_director_blueprint"
     */
    fun getBlueprintKey(agentId: String): String =
        when (agentId) {
            AgentIds.DIRECTOR -> director
            AgentIds.ARTIST -> artist
            AgentIds.REVIEWER -> reviewer
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
