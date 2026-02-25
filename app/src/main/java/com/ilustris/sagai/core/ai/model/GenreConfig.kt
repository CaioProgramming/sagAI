package com.ilustris.sagai.core.ai.model

import com.google.gson.annotations.SerializedName

/**
 * Unified configuration for a genre, fetched from Remote Config.
 * This represents the "Soul" of the genre, including its art style,
 * tone of voice, and validation rules for the AI agents.
 */
data class GenreConfig(
    @SerializedName("ambientMusicUrl")
    val ambientMusicUrl: String = "",
    @SerializedName("artStyle")
    val artStyle: String = "",
    @SerializedName("renderingInstructions")
    val renderingInstructions: String = "",
    @SerializedName("appearanceGuidelines")
    val appearanceGuidelines: String = "",
    @SerializedName("colorPalette")
    val colorPalette: String = "",
    @SerializedName("conversationDirective")
    val conversationDirective: String = "",
    @SerializedName("nameDirective")
    val nameDirective: String = "",
    @SerializedName("reviewerStrictness")
    val reviewerStrictness: ReviewerStrictness? = null,
    @SerializedName("criticalRules")
    val criticalRules: String = "",
    @SerializedName("criticalValidation")
    val criticalValidation: String = "",
    @SerializedName("variations")
    val variations: Map<String, VariationConfig>? = null,
    @SerializedName("companion")
    val companion: CompanionConfig? = null,
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
        val artStyle: String? = null,
        val renderingInstructions: String? = null,
        val appearanceGuidelines: String? = null,
        val conversationDirective: String? = null,
        val criticalRules: String? = null,
    )

    fun getValidationRules(
        genreName: String,
        palette: String,
    ): String =
        buildString {
            appendLine("**$genreName GENRE SOUL (Reviewer Mandate):**")
            appendLine("- Narrative Essence: $artStyle")
            appendLine("- Mandatory Palette: $palette")
            appendLine()
            appendLine("**RENDERING INSTRUCTIONS (The Technical Goal):**")
            appendLine(renderingInstructions)
            appendLine()
            appendLine("**CRITICAL FOCUS AREAS:**")
            appendLine(
                "1. SCENE NATURALITY: Ensure lighting and atmosphere are organic to the narrative context. Ban hard-injected digital overlays or unnatural light bars on faces.",
            )
            appendLine("2. ENVIRONMENT INTEGRITY: The setting must be vivid and match the story context. No vague or empty backgrounds.")
            appendLine("3. CHARACTER FIDELITY: Ensure all physical traits (skin, hair, eyes) match the '#### SUBJECTS DETAILS' perfectly.")
            appendLine()
            appendLine("**TECHNICAL ALIGNMENT:**")
            appendLine(
                "- The Artist must describe visuals that COMPLEMENT the Rendering Instructions. If the medium is SUMI-E, the description should favor 'void' and 'strokes'. If it is COMIC, it should favor 'spot blacks' and 'sharp ink'.",
            )
        }
}
