package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga

data class SacredContract(
    val saga: Saga,
    val character: Character,
    val narrativeSeal: String? = null,
    /** The saga's founding place — where the story's world is anchored from the very first act. */
    val openingLocation: GeneratedLocationSeed? = null,
)

data class GeneratedLocationSeed(
    val name: String = "",
    val history: String = "",
    val emojiTag: String? = null,
)
