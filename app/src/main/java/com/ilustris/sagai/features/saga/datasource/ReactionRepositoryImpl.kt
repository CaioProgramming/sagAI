package com.ilustris.sagai.features.saga.datasource

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.saga.chat.data.model.Reaction
import com.ilustris.sagai.features.saga.chat.repository.ReactionRepository
import javax.inject.Inject

class ReactionRepositoryImpl
    @Inject
    constructor(
        database: SagaDatabase,
    ) : ReactionRepository {
        private val reactionDao by lazy {
            database.reactionDao()
        }

        override suspend fun saveReaction(reaction: Reaction) = reaction.copy(id = reactionDao.addReaction(reaction).toInt())
    }
