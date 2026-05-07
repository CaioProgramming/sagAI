package com.ilustris.sagai.features.characters.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.home.data.model.Saga

/**
 * A dedicated data model for the character details page to avoid loading the full [SagaContent].
 * This structure is flat and avoids recursive relations that could lead to memory issues.
 */
data class CharacterDetailData(
    @Embedded
    val character: Character,
    @Relation(
        parentColumn = "sagaId",
        entityColumn = "id",
        entity = Saga::class,
    )
    val saga: Saga,
    @Relation(
        parentColumn = "id",
        entityColumn = "characterId",
        entity = CharacterEvent::class,
    )
    val events: List<CharacterEventDetails> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "characterOneId",
        entity = CharacterRelation::class,
    )
    val relationshipsAsFirst: List<RelationshipContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "characterTwoId",
        entity = CharacterRelation::class,
    )
    val relationshipsAsSecond: List<RelationshipContent> = emptyList(),
    val messageCount: Int = 0,
) {
    /**
     * Combines all relationships where this character is either the first or second participant.
     */
    val relationships: List<RelationshipContent>
        get() = (relationshipsAsFirst + relationshipsAsSecond).distinctBy { it.data.id }
}
