package com.ilustris.sagai.features.characters.data.model

import java.util.UUID

data class CharacterInfo(
    val name: String = "",
    val gender: Gender,
    val description: String = "",
    val id: String = UUID.randomUUID().toString(),
)

enum class Gender {
    MALE,
    FEMALE,
    Other,
}
