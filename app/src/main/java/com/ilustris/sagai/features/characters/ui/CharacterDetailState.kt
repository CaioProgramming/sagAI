package com.ilustris.sagai.features.characters.ui

import com.ilustris.sagai.features.characters.data.model.Character

data class CharacterDetailState(
    val stats: CharacterStats? = null,
    val relationshipSummary: String? = null,
    val roleTraits: List<String> = emptyList(),
)

data class CharacterStats(
    val strength: Int = 0,
    val agility: Int = 0,
    val intelligence: Int = 0,
    val charisma: Int = 0,
    val constitution: Int = 0,
    val wisdom: Int = 0,
)

/** Where the player-requested design review of a character stands. */
sealed interface DesignReviewState {
    data object Idle : DesignReviewState

    data object Reviewing : DesignReviewState

    /** The audit found fields to rewrite; [reviewed] is only saved when the player applies it. */
    data class Proposal(
        val reviewed: Character,
        val changes: List<Triple<String, String, String>>,
    ) : DesignReviewState

    /** The sheet passed the audit untouched. */
    data object Passed : DesignReviewState

    data object Failed : DesignReviewState
}
