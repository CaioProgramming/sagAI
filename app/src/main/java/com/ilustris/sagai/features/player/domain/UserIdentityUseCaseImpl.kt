package com.ilustris.sagai.features.player.domain

import com.ilustris.sagai.core.datastore.DataStorePreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class UserIdentityUseCaseImpl
    @Inject
    constructor(
        private val dataStore: DataStorePreferences,
    ) : UserIdentityUseCase {
    override fun observeName(): Flow<String> = dataStore.getString("user_display_name", "")

    override suspend fun getNameNow(): String = dataStore.getStringNow("user_display_name", "")

    override suspend fun setName(name: String) {
        dataStore.setString("user_display_name", name)
        dataStore.setBoolean("user_name_prompt_seen", true)
    }

    override suspend fun shouldPromptName(): Boolean =
        !dataStore.getBooleanNow("user_name_prompt_seen", false)
}


