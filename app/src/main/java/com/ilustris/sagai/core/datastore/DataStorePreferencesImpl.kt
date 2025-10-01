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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val DATASTORE_NAME = "settings_datastore"
val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class DataStorePreferencesImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : DataStorePreferences() {
        override fun getString(
            key: String,
            default: String,
        ): Flow<String> = context.dataStore.data.map { it[stringPreferencesKey(key)] ?: default }

        override suspend fun setString(
            key: String,
            value: String,
        ) {
            context.dataStore.edit { it[stringPreferencesKey(key)] = value }
        }

        override fun getBoolean(
            key: String,
            default: Boolean,
        ): Flow<Boolean> = context.dataStore.data.map { it[booleanPreferencesKey(key)] ?: default }

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
        ): Flow<Int> = context.dataStore.data.map { it[intPreferencesKey(key)] ?: default }

        override suspend fun setInt(
            key: String,
            value: Int,
        ) {
            context.dataStore.edit { it[intPreferencesKey(key)] = value }
        }

        override fun getLong(
            key: String,
            default: Long,
        ): Flow<Long> = context.dataStore.data.map { it[longPreferencesKey(key)] ?: default }

        override suspend fun setLong(
            key: String,
            value: Long,
        ) {
            context.dataStore.edit { it[longPreferencesKey(key)] = value }
        }
    }
