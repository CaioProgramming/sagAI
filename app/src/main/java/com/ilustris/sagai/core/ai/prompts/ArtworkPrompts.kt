package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.ImageType

data class ArtworkConcept(
    val artwork: String = "",
)

object ArtworkPrompts {
    /**
     * Cover concepts come from a dedicated curator that returns a full [com.ilustris.sagai.core.ai.model.Artwork].
     * A key of its own, so app versions that still read the older text-only `artwork_cover_concept_blueprint`
     * keep working while it stays in Remote Config.
     */
    const val COVER_CURATOR_BLUEPRINT = "artwork_cover_curator_blueprint"

    /**
     * One concept blueprint per [ImageType] (icon/portrait vs cover), so each has its own tailored
     * rules instead of sharing a single generic one. This is the single source of artwork rules:
     * used both for standalone concept generation ([ArtworkConceptService.ensureArtwork]) and merged
     * as instructions into any generation that outputs an `artwork` field
     * ([ArtworkConceptService.artworkInstructions]). Covers are now curated by [COVER_CURATOR_BLUEPRINT].
     */
    fun conceptBlueprintKey(imageType: ImageType): String =
        when (imageType) {
            ImageType.ICON -> "artwork_portrait_concept_blueprint"
            ImageType.COVER -> COVER_CURATOR_BLUEPRINT
        }
}
