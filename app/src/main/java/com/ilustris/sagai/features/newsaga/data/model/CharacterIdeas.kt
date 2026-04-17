package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.features.characters.data.model.CharacterInfo

data class CharacterIdeas(
    val ideas: List<CharacterInfo>,
    val message: String = "",
)
