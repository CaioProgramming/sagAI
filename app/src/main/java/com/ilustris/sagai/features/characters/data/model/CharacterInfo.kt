package com.ilustris.sagai.features.characters.data.model

import java.util.UUID

data class CharacterInfo(
    val name: String = "",
    val gender: String = "",
    val description: String = "",
    val id: String = UUID.randomUUID().toString(),
)
