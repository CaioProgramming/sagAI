package com.ilustris.sagai.core.ai.models

import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.characters.data.model.Character

data class SagaEndCreditsContext(
    val sagaTitle: String,
    val playerInfo: Character?,
    val fullSagaStructure: List<ActContent>,
)