package com.ilustris.sagai.features.characters.relations.data.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent
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

        private val relationshipUpdateEventDao by lazy {
            database.relationshipUpdateEventDao()
        }

        override suspend fun insertRelation(characterRelation: CharacterRelation): CharacterRelation {
            val insertedId = characterRelationDao.insertRelation(characterRelation)
            return characterRelation.copy(
                id = insertedId.toInt(),
                lastUpdated = Calendar.getInstance().timeInMillis,
            )
        }

        override suspend fun insertRelations(characterRelations: List<CharacterRelation>) =
            characterRelationDao.insertRelations(characterRelations)

        override suspend fun insertRelationAndEvent(
            relation: CharacterRelation,
            timelineId: Int,
        ): CharacterRelation {
            val savedRelation = insertRelation(relation)

            val event =
                RelationshipUpdateEvent(
                    relationId = savedRelation.id,
                    timelineId = timelineId,
                    title = savedRelation.title,
                    description = savedRelation.description,
                    emoji = savedRelation.emoji,
                    timestamp = savedRelation.lastUpdated,
                )
            relationshipUpdateEventDao.insertEvent(event)

            return savedRelation
        }

        override suspend fun addEventToRelation(
            relationId: Int,
            timelineId: Int,
            title: String,
            description: String,
            emoji: String,
            timestamp: Long,
        ) {
            val event =
                RelationshipUpdateEvent(
                    relationId = relationId,
                    timelineId = timelineId,
                    title = title,
                    description = description,
                    emoji = emoji,
                    timestamp = timestamp,
                )
            relationshipUpdateEventDao.insertEvent(event)
        }
    }
