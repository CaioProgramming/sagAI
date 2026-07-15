package com.ilustris.sagai.features.player.domain

import kotlinx.coroutines.flow.Flow

interface UserIdentityUseCase {
    fun observeName(): Flow<String>

    suspend fun getNameNow(): String

    suspend fun setName(name: String)

    suspend fun shouldPromptName(): Boolean
}


