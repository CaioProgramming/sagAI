package com.ilustris.sagai.features.player.data.model

data class PlayerProfileData(
    val confirmedTraits: List<ConfirmedTrait> = emptyList(),
    val candidateTraits: List<CandidateTrait> = emptyList(),
)

data class ConfirmedTrait(
    val trait: String = "",
    val evidenceSummary: String = "",
    val genreScope: String = "",
    val lastReinforcedAt: Long = 0L,
)

data class CandidateTrait(
    val trait: String = "",
    val firstSeenAt: Long = 0L,
    val sagaId: Int = 0,
    val actId: Int = 0,
)

