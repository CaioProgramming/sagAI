package com.ilustris.sagai.features.characters.relations.data.repository

import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation

interface CharacterRelationRepository {
    suspend fun insertRelation(characterRelation: CharacterRelation): CharacterRelation

    suspend fun insertRelations(characterRelations: List<CharacterRelation>)
}
