package com.ilustris.sagai.features.player.data.repository

import com.ilustris.sagai.features.player.data.model.PlayerProfileData
import kotlinx.coroutines.flow.Flow

interface PlayerProfileRepository {
    fun observeProfile(): Flow<PlayerProfileData?>

    suspend fun getProfile(): PlayerProfileData?

    suspend fun saveProfile(data: PlayerProfileData)
}

