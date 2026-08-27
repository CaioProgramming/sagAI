package com.ilustris.sagai.core.ai.prompts

data class ArtworkConcept(
    val artwork: String = "",
)

object ArtworkPrompts {
    const val ARTWORK_CONCEPT_BLUEPRINT = "artwork_concept_blueprint"

    /**
     * Instruction-only blueprint merged into any generation that outputs an `artwork` field,
     * keeping the artwork rules in a single place instead of duplicated per blueprint.
     */
    const val ARTWORK_DIRECTIVES_BLUEPRINT = "artwork_directives_blueprint"
}
