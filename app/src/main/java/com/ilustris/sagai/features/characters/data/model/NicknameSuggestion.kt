package com.ilustris.sagai.features.characters.data.model

data class NicknameSuggestion(
    val characterName: String,
    val newNicknames: List<String>,
)

data class NickNameGen(
    val suggestions: List<NicknameSuggestion>,
)
