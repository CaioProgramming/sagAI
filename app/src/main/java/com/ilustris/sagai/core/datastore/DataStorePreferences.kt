package com.ilustris.sagai.core.datastore

import kotlinx.coroutines.flow.Flow

abstract class DataStorePreferences {
    abstract fun getString(key: String, default: String = ""): Flow<String>
    abstract suspend fun setString(key: String, value: String)

    abstract fun getBoolean(key: String, default: Boolean = false): Flow<Boolean>
    abstract suspend fun setBoolean(key: String, value: Boolean)

    abstract fun getInt(key: String, default: Int = 0): Flow<Int>
    abstract suspend fun setInt(key: String, value: Int)

    abstract fun getLong(key: String, default: Long = 0L): Flow<Long>
    abstract suspend fun setLong(key: String, value: Long)
}

