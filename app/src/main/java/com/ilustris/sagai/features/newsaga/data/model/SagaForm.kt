package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.features.characters.data.model.CharacterInfo

data class SagaForm(
    val saga: SagaDraft = SagaDraft(),
    val character: CharacterInfo = CharacterInfo(),
)
