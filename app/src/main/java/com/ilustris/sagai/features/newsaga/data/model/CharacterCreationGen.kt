package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.features.characters.data.model.CharacterInfo

data class CharacterCreationGen(
    val message: String,
    val inputHint: String,
    val suggestions: List<CreationSuggestion>,
    val callback: CharacterCallbackContent?,
)

data class CharacterCallbackContent(
    val action: CallBackAction,
    val data: CharacterInfo?,
)
