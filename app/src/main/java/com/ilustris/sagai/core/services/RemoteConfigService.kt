package com.ilustris.sagai.core.services

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await

class RemoteConfigService {
    private val firebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    suspend fun getString(key: String): String? = fetchFlag { firebaseRemoteConfig.getString(key) }

    suspend fun getBoolean(key: String): Boolean? = fetchFlag { firebaseRemoteConfig.getBoolean(key) }

    suspend fun getLong(key: String): Long? = fetchFlag { firebaseRemoteConfig.getLong(key) }

    suspend fun getDouble(key: String): Double? = fetchFlag { firebaseRemoteConfig.getDouble(key) }

    suspend inline fun <reified T> getJson(key: String): T? {
        val jsonString = getString(key)
        return if (jsonString?.isNotEmpty() == true) {
            val typeToken = object : TypeToken<T>() {}

            Gson().fromJson(jsonString, typeToken)
        } else {
            null
        }
    }

    private suspend fun <T> fetchFlag(block: suspend () -> T): T? =
        try {
            val fetchTask =
                firebaseRemoteConfig.fetchAndActivate().await()
            if (fetchTask != null) {
                block()
            } else {
                error("Couldn't sync with remote config.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
}
