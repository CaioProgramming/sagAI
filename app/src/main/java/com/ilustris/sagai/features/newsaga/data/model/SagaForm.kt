package com.ilustris.sagai.features.newsaga.data.model

import ai.atick.material.MaterialColor
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details

data class SagaForm(
    val title: String = "",
    val description: String = "",
    val genre: Genre = Genre.entries.first(),
    val character: Character = Character(details = Details()),
)
