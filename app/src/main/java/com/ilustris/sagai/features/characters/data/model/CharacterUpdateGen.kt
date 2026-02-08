package com.ilustris.sagai.features.characters.data.model

data class CharacterUpdate(
    val characterName: String,
    val title: String,
    val description: String,
)

data class CharacterUpdateGen(
    val updates: List<CharacterUpdate>,
)
