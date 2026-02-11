package com.ilustris.sagai.core.services

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RemoteConfigService {
    private val firebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    suspend fun getString(key: String): String? = fetchFlag("getString", key) { firebaseRemoteConfig.getString(key) }

    suspend fun getBoolean(key: String): Boolean? = fetchFlag("getBoolean", key) { firebaseRemoteConfig.getBoolean(key) }

    suspend fun getLong(key: String): Long? = fetchFlag("getLong", key) { firebaseRemoteConfig.getLong(key) }

    suspend fun getDouble(key: String): Double? = fetchFlag("getDouble", key) { firebaseRemoteConfig.getDouble(key) }

    suspend inline fun <reified T> getJson(key: String): T? {
        val jsonString = getString(key)
        return if (jsonString?.isNotEmpty() == true) {
            val typeToken = object : TypeToken<T>() {}

            try {
                Gson().fromJson<T>(jsonString, typeToken.type).also {
                    Log.d("RemoteConfigService", "getJson($key) -> $it")
                }
            } catch (e: Exception) {
                Log.e("RemoteConfigService", "Error parsing json for $key: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    private suspend fun <T> fetchFlag(
        method: String,
        key: String,
        block: suspend () -> T,
    ): T? =
        try {
            block().also {
                Log.d("RemoteConfigService", "$method($key) -> $it")
            }
        } catch (e: Exception) {
            Log.e("RemoteConfigService", "Error on $method($key): ${e.message}")
            e.printStackTrace()
            null
        }
}
