package com.ilustris.sagai.features.characters.relations.domain.data

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation

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
)
