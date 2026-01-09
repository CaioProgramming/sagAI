package com.ilustris.sagai.features.characters.data.model

data class KnowledgeUpdateResult(
    val updates: List<CharacterKnowledgeUpdate> = emptyList(),
)

data class CharacterKnowledgeUpdate(
    val characterName: String,
    val learnedFacts: List<String>,
)
