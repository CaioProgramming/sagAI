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

/** Model output of the placement of an existing cast (the backfill of a saga that predates the system). */
data class CharacterPlacementOutput(
    val placements: List<PlacementEntry> = emptyList(),
)

data class PlacementEntry(
    val name: String = "",
    val baseLevel: Int = 1,
    val levelRationale: String = "",
    val skills: List<String> = emptyList(),
)

/** Model output of the end-of-chapter honors. */
data class MedalsOutput(
    val medals: List<MedalDraft> = emptyList(),
)

data class MedalDraft(
    val characterName: String = "",
    val name: String = "",
    val reason: String = "",
    val emoji: String? = null,
    val featRefs: List<Int> = emptyList(),
)

/** Model output of the end-of-act legacies. */
data class LegaciesOutput(
    val legacies: List<LegacyDraft> = emptyList(),
)

data class LegacyDraft(
    val characterName: String = "",
    val title: String = "",
    val description: String = "",
    val featRefs: List<Int> = emptyList(),
)

/** Model output of the status catch-up: only the characters who are not simply around. */
data class StatusOutput(
    val statuses: List<StatusEntry> = emptyList(),
)

data class StatusEntry(
    val name: String = "",
    val status: com.ilustris.sagai.features.characters.data.model.CharacterStatus? = null,
    val statusNote: String? = null,
    val statusReason: String? = null,
)
