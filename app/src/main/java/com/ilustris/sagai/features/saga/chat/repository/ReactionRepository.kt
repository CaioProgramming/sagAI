package com.ilustris.sagai.features.saga.chat.repository

import com.ilustris.sagai.features.saga.chat.data.model.Reaction

interface ReactionRepository {
    suspend fun saveReaction(reaction: Reaction)
}
