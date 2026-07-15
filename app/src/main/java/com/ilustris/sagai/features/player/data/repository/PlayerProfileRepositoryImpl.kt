package com.ilustris.sagai.features.player.data.repository

import com.google.gson.Gson
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.features.player.data.model.PlayerProfileData
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerProfileRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStorePreferences,
    ) : PlayerProfileRepository {
    override fun observeProfile(): Flow<PlayerProfileData?> =
        dataStore.getString("player_profile_json", "").map { jsonString ->
            if (jsonString.isEmpty()) {
                null
            } else {
                try {
                    Gson().fromJson(jsonString, PlayerProfileData::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }

    override suspend fun getProfile(): PlayerProfileData? {
        val jsonString = dataStore.getStringNow("player_profile_json", "")
        return if (jsonString.isEmpty()) {
            null
        } else {
            try {
                Gson().fromJson(jsonString, PlayerProfileData::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun saveProfile(data: PlayerProfileData) {
        val jsonString = Gson().toJson(data)
        dataStore.setString("player_profile_json", jsonString)
    }
}

