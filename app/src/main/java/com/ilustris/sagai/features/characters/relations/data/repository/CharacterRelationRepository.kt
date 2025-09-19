package com.ilustris.sagai.features.characters.relations.data.repository

import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation

interface CharacterRelationRepository {
    suspend fun insertRelation(characterRelation: CharacterRelation): CharacterRelation

    suspend fun insertRelations(characterRelations: List<CharacterRelation>) // We can review this later if needed

    suspend fun insertRelationAndEvent(relation: CharacterRelation, timelineId: Int): CharacterRelation

    // Method to add an event to an existing relation
    suspend fun addEventToRelation(relationId: Int, timelineId: Int, title: String, description: String, emoji: String, timestamp: Long)
}
