package com.ilustris.sagai.features.progression.data.model

/** Model output of the feat evaluation: one entry per participant of the event. */
data class FeatEvaluation(
    val characters: List<CharacterFeatsOutput> = emptyList(),
)

data class CharacterFeatsOutput(
    val name: String = "",
    val feats: List<GeneratedFeat> = emptyList(),
    val newSkills: List<String> = emptyList(),
)

data class GeneratedFeat(
    val kind: String = "",
    val title: String = "",
    val changed: String? = null,
    val cost: String? = null,
    val tier: Int = 0,
    /** Id of the relationship update of this event that this feat matches, if any. */
    val relationshipEvidence: Int? = null,
)
