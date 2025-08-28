package com.ilustris.sagai.features.characters.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class CharacterEventDetails(
    @Embedded
    val event: CharacterEvent,

    @Relation(
        parentColumn = "characterId", // CharacterEvent.characterId
        entityColumn = "id",          // Character.id
        entity = Character::class
    )
    val character: Character
)
