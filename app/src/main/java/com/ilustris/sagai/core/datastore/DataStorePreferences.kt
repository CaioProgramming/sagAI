package com.ilustris.sagai.core.datastore

import kotlinx.coroutines.flow.Flow

interface DataStorePreferences {
    fun getString(
        key: String,
        default: String = "",
    ): Flow<String>

    suspend fun getStringNow(
        key: String,
        default: String = "",
    ): String

    suspend fun setString(
        key: String,
        value: String,
    )

    fun getBoolean(
        key: String,
        default: Boolean = false,
    ): Flow<Boolean>

    suspend fun getBooleanNow(
        key: String,
        default: Boolean = false,
    ): Boolean

    suspend fun setBoolean(
        key: String,
        value: Boolean,
    )

    fun getInt(
        key: String,
        default: Int = 0,
    ): Flow<Int>

    suspend fun getIntNow(
        key: String,
        default: Int = 0,
    ): Int

    suspend fun setInt(
        key: String,
        value: Int,
    )

    fun getLong(
        key: String,
        default: Long = 0L,
    ): Flow<Long>

    suspend fun getLongNow(
        key: String,
        default: Long = 0L,
    ): Long

    suspend fun setLong(
        key: String,
        value: Long,
    )

    suspend fun removeKey(key: String)
}
