package com.ilustris.sagai.core.services

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.gsonTypeOf
import com.ilustris.sagai.core.narrative.NarrativeRules
import timber.log.Timber

class RemoteConfigService {
    private val firebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    suspend fun getString(
        key: String?,
        logEnabled: Boolean = true,
    ): String? =
        fetchFlag("getString", key, logEnabled) {
            if (key == null) return@fetchFlag "Key: $key not found"
            firebaseRemoteConfig.getString(key)
        }

    suspend fun getBoolean(key: String): Boolean? = fetchFlag("getBoolean", key) { firebaseRemoteConfig.getBoolean(key) }

    suspend fun getLong(key: String): Long? = fetchFlag("getLong", key) { firebaseRemoteConfig.getLong(key) }

    suspend fun getDouble(key: String): Double? = fetchFlag("getDouble", key) { firebaseRemoteConfig.getDouble(key) }

    suspend inline fun <reified T> getJson(
        key: String?,
        logEnabled: Boolean = true,
    ): T? {
        val jsonString = getString(key, logEnabled)
        return if (jsonString?.isNotEmpty() == true) {
            try {
                Gson().fromJson<T>(jsonString, gsonTypeOf<T>())
            } catch (e: Exception) {
                Timber.tag("RemoteConfigService").e("Error parsing json for $key: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    private suspend fun <T> fetchFlag(
        method: String,
        key: String?,
        logEnabled: Boolean = true,
        block: suspend () -> T,
    ): T? =
        try {
            block().also {
                if (logEnabled) {
                    Timber.tag("RemoteConfigService").d("\n$method($key) -> $it\n")
                }
            }
        } catch (e: Exception) {
            Timber.tag("RemoteConfigService").e("Error on $method($key): ${e.message}")
            e.printStackTrace()
            null
        }
}

suspend fun RemoteConfigService.getNarrativeRules() = getJson<NarrativeRules>("narrative_rules", false)!!

suspend fun RemoteConfigService.getGenderPlaceholders() =
    getJson<Map<String, com.ilustris.sagai.features.newsaga.data.model.GenderPlaceholders>>(
        "gender_placeholders",
        false,
    ) ?: emptyMap()
