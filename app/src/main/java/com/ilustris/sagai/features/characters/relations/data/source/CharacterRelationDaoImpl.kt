package com.ilustris.sagai.features.characters.relations.data.source

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import javax.inject.Inject

class CharacterRelationDaoImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : CharacterRelationDao {
        val dao by lazy { database.characterRelationDao() }

        override suspend fun insertRelation(characterRelation: CharacterRelation) = dao.insertRelation(characterRelation)

        override suspend fun insertRelations(characterRelations: List<CharacterRelation>) = dao.insertRelations(characterRelations)
    }
