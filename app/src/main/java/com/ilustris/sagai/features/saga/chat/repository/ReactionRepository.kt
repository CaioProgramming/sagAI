package com.ilustris.sagai.features.saga.chat.repository

import ReactionContent
import com.ilustris.sagai.features.saga.chat.data.model.Reaction
import kotlinx.coroutines.flow.Flow

interface ReactionRepository {
    suspend fun saveReaction(reaction: Reaction): Reaction

    fun observeReactions(
        messageIds: List<Int>,
        since: Long,
    ): Flow<List<ReactionContent>>
}
