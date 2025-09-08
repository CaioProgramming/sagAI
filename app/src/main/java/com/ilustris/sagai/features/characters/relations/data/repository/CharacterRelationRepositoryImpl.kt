package com.ilustris.sagai.features.characters.relations.data.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.source.CharacterRelationDao
import java.util.Calendar
import javax.inject.Inject

class CharacterRelationRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : CharacterRelationRepository {
        private val characterRelationDao by lazy {
            database.characterRelationDao()
        }

        override suspend fun insertRelation(characterRelation: CharacterRelation) =
            characterRelation.copy(
                id = characterRelationDao.insertRelation(characterRelation).toInt(),
                lastUpdated = Calendar.getInstance().timeInMillis,
            )

        override suspend fun insertRelations(characterRelations: List<CharacterRelation>) =
            characterRelationDao.insertRelations(characterRelations)
    }
