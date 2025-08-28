package com.ilustris.sagai.features.characters.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class CharacterContent(
    @Embedded
    val data: Character,
    @Relation(
        parentColumn = "id",
        entityColumn = "characterId",
        entity = CharacterEvent::class,
    )
    val events: List<CharacterEvent> = emptyList(),
)
