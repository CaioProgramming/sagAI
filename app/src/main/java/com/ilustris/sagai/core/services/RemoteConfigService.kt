package com.ilustris.sagai.core.services

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.gsonTypeOfList
import com.ilustris.sagai.core.ai.gsonTypeOfMapString
import com.ilustris.sagai.core.ai.gsonTypeOfMapStringString
import com.ilustris.sagai.core.ai.gsonTypeOfStringAnyMap
import com.ilustris.sagai.core.narrative.NarrativeRules
import timber.log.Timber
import java.lang.reflect.Type

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

    /** Plain JSON objects (data classes). Do not use for Map/List — use typed helpers below. */
    @OptIn(ExperimentalStdlibApi::class)
    suspend inline fun <reified T : Any> getJson(
        key: String?,
        logEnabled: Boolean = true,
    ): T? = getJsonWithType(key, com.ilustris.sagai.core.ai.getJavaType<T>(), logEnabled)

    suspend fun getJsonMapStringAny(
        key: String?,
        logEnabled: Boolean = true,
    ): Map<String, Any>? = getJsonWithType(key, gsonTypeOfStringAnyMap(), logEnabled)

    suspend fun getJsonMapStringString(
        key: String?,
        logEnabled: Boolean = true,
    ): Map<String, String>? = getJsonWithType(key, gsonTypeOfMapStringString(), logEnabled)

    suspend fun <V : Any> getJsonMapString(
        key: String?,
        valueClass: Class<V>,
        logEnabled: Boolean = true,
    ): Map<String, V>? = getJsonWithType(key, gsonTypeOfMapString(valueClass), logEnabled)

    suspend fun <T : Any> getJsonList(
        key: String?,
        itemClass: Class<T>,
        logEnabled: Boolean = true,
    ): List<T>? = getJsonWithType(key, gsonTypeOfList(itemClass), logEnabled)

    suspend fun <T> getJsonWithType(
        key: String?,
        type: Type,
        logEnabled: Boolean,
    ): T? {
        val jsonString = getString(key, logEnabled)
        return if (jsonString?.isNotEmpty() == true) {
            try {
                Gson().fromJson(jsonString, type)
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
    getJsonMapString(
        "gender_placeholders",
        com.ilustris.sagai.features.newsaga.data.model.GenderPlaceholders::class.java,
        false,
    ) ?: emptyMap()
