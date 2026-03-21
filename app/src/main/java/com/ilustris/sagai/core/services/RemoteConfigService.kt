package com.ilustris.sagai.core.services

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.core.narrative.NarrativeRules

class RemoteConfigService {
    private val firebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    suspend fun getString(
        key: String,
        logEnabled: Boolean = true,
    ): String? =
        fetchFlag("getString", key, logEnabled) {
            firebaseRemoteConfig.getString(key)
        }

    suspend fun getBoolean(key: String): Boolean? = fetchFlag("getBoolean", key) { firebaseRemoteConfig.getBoolean(key) }

    suspend fun getLong(key: String): Long? = fetchFlag("getLong", key) { firebaseRemoteConfig.getLong(key) }

    suspend fun getDouble(key: String): Double? = fetchFlag("getDouble", key) { firebaseRemoteConfig.getDouble(key) }

    suspend inline fun <reified T> getJson(
        key: String,
        logEnabled: Boolean = true,
    ): T? {
        val jsonString = getString(key, logEnabled)
        return if (jsonString?.isNotEmpty() == true) {
            val typeToken = object : TypeToken<T>() {}

            try {
                Gson().fromJson<T>(jsonString, typeToken.type)
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
        logEnabled: Boolean = true,
        block: suspend () -> T,
    ): T? =
        try {
            block().also {
                if (logEnabled) {
                    Log.d("RemoteConfigService", "\n$method($key) -> $it\n")
                }
            }
        } catch (e: Exception) {
            Log.e("RemoteConfigService", "Error on $method($key): ${e.message}")
            e.printStackTrace()
            null
        }
}

suspend fun RemoteConfigService.getNarrativeRules() = getJson<NarrativeRules>("narrative_rules", false)!!
