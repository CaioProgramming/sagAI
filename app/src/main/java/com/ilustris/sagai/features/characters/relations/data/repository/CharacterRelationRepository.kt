package com.ilustris.sagai.features.characters.relations.data.repository

import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent

interface CharacterRelationRepository {
    suspend fun insertRelation(characterRelation: CharacterRelation): CharacterRelation

    suspend fun insertRelations(characterRelations: List<CharacterRelation>)

    suspend fun insertRelationAndEvent(
        relation: CharacterRelation,
        timelineId: Int,
    ): CharacterRelation

    suspend fun addEventToRelation(
        relationId: Int,
        timelineId: Int,
        title: String,
        description: String,
        emoji: String,
        timestamp: Long,
    )

    suspend fun addEventToRelation(event: RelationshipUpdateEvent): RelationshipUpdateEvent
}
