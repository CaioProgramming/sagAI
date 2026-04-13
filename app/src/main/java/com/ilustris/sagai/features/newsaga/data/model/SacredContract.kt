package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga

data class SacredContract(
    val saga: Saga,
    val character: Character,
    val narrativeSeal: String? = null,
)
