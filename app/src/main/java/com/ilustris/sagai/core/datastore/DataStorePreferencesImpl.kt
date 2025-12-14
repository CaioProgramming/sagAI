package com.ilustris.sagai.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

private const val DATASTORE_NAME = "settings_datastore"
val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class DataStorePreferencesImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : DataStorePreferences {
        override fun getString(
            key: String,
            default: String,
        ): Flow<String> = preferencesFlow.map { it[stringPreferencesKey(key)] ?: default }

        override suspend fun getStringNow(
            key: String,
            default: String,
        ) = getString(key, default).first()

        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        private val preferencesFlow: Flow<Preferences> =
            context.dataStore.data
                .shareIn(
                    scope = scope,
                    started = SharingStarted.Eagerly, // Start immediately
                    replay = 1, // Cache and replay the last emitted value to new subscribers
                )

        override suspend fun setString(
            key: String,
            value: String,
        ) {
            context.dataStore.edit { it[stringPreferencesKey(key)] = value }
        }

        override fun getBoolean(
            key: String,
            default: Boolean,
        ): Flow<Boolean> = preferencesFlow.map { it[booleanPreferencesKey(key)] ?: default }

        override suspend fun getBooleanNow(
            key: String,
            default: Boolean,
        ): Boolean = getBoolean(key, default).first()

        override suspend fun setBoolean(
            key: String,
            value: Boolean,
        ) {
            context.dataStore.edit {
                it[booleanPreferencesKey(key)] = value
            }
        }

        override fun getInt(
            key: String,
            default: Int,
        ): Flow<Int> = preferencesFlow.map { it[intPreferencesKey(key)] ?: default }

        override suspend fun getIntNow(
            key: String,
            default: Int,
        ): Int = getInt(key, default).first()

        override suspend fun setInt(
            key: String,
            value: Int,
        ) {
            context.dataStore.edit { it[intPreferencesKey(key)] = value }
        }

        override fun getLong(
            key: String,
            default: Long,
        ): Flow<Long> = preferencesFlow.map { it[longPreferencesKey(key)] ?: default }

        override suspend fun getLongNow(
            key: String,
            default: Long,
        ): Long = getLong(key, default).first()

        override suspend fun setLong(
            key: String,
            value: Long,
        ) {
            context.dataStore.edit { it[longPreferencesKey(key)] = value }
        }

        override suspend fun removeKey(key: String) {
            context.dataStore.edit { preferences ->
                // Remove all possible key types for the given key
                preferences.remove(stringPreferencesKey(key))
                preferences.remove(booleanPreferencesKey(key))
                preferences.remove(intPreferencesKey(key))
                preferences.remove(longPreferencesKey(key))
        }
    }
    }
