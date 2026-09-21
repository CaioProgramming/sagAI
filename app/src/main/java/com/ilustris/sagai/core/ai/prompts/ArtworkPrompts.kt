package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.ImageType

data class ArtworkConcept(
    val artwork: String = "",
)

object ArtworkPrompts {
    /**
     * One concept blueprint per [ImageType] (icon/portrait vs cover), so each has its own tailored
     * rules instead of sharing a single generic one. This is the single source of artwork rules:
     * used both for standalone concept generation ([ArtworkConceptService.ensureArtwork]) and merged
     * as instructions into any generation that outputs an `artwork` field
     * ([ArtworkConceptService.artworkInstructions]).
     */
    fun conceptBlueprintKey(imageType: ImageType): String =
        when (imageType) {
            ImageType.ICON -> "artwork_portrait_concept_blueprint"
            ImageType.COVER -> "artwork_cover_concept_blueprint"
        }
}
