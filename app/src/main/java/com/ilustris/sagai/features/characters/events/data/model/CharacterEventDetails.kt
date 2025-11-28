package com.ilustris.sagai.features.characters.events.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.timeline.data.model.Timeline

data class CharacterEventDetails(
    @Embedded
    val event: CharacterEvent,
    @Relation(
        parentColumn = "characterId",
        entityColumn = "id",
        entity = Character::class,
    )
    val character: Character,
    @Relation(
        parentColumn = "gameTimelineId",
        entityColumn = "id",
        entity = Timeline::class,
    )
    val timeline: Timeline?,
)
