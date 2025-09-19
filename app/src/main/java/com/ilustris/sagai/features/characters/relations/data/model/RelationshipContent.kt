package com.ilustris.sagai.features.characters.relations.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.characters.data.model.Character

data class RelationshipContent(
    @Embedded
    val data: CharacterRelation,
    @Relation(
        parentColumn = "characterOneId",
        entityColumn = "id",
    )
    val characterOne: Character,
    @Relation(
        parentColumn = "characterTwoId",
        entityColumn = "id",
    )
    val characterTwo: Character,
    @Relation(
        parentColumn = "id", // This refers to CharacterRelation.id (via the @Embedded data field)
        entityColumn = "relationId",
        entity = RelationshipUpdateEvent::class,
    )
    val relationshipEvents: List<RelationshipUpdateEvent> = emptyList(),
)
