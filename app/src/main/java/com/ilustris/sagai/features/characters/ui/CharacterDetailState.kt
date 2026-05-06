package com.ilustris.sagai.features.characters.ui

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
