package com.ilustris.sagai.core.ai.services

import android.util.Log
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * Fetches and caches [GenreVisualConfig] from Firebase Remote Config.
 *
 * Uses a per-genre in-memory cache so each config is fetched at most once per app session.
 * The config key follows the pattern: `<genre_name>_visual_config` (e.g. `fantasy_visual_config`).
 *
 * Returns null if the config could not be fetched — callers should skip applying
 * visual effects when the config is absent (remote-only, no compiled fallbacks).
 */
class GenreVisualConfigService(
    private val remoteConfigService: RemoteConfigService,
) {
    private val cache = mutableMapOf<Genre, GenreVisualConfig?>()

    suspend fun getVisualConfig(genre: Genre): GenreVisualConfig? {
        Log.d("GenreVisualConfigService", "Requesting config for $genre")
        if (cache.containsKey(genre) && cache[genre] != null) {
            Log.d("GenreVisualConfigService", "Returning cached config for $genre")
            return cache[genre]
        }

        val key = "${genre.name.lowercase()}_visual_config"
        Log.d("GenreVisualConfigService", "Fetching from remote config with key: $key")
        val config =
            try {
                remoteConfigService.getJson<GenreVisualConfig>(key)
            } catch (e: Exception) {
                Log.e(
                    "GenreVisualConfigService",
                    "Failed to fetch visual config for $genre: ${e.message}",
                )
                null
            }
        if (config != null) {
            cache[genre] = config
        } else {
            Log.w("GenreVisualConfigService", "Config for $genre is null/empty in Remote Config")
        }
        return config
    }

    /**
     * Clears the in-memory cache, forcing the next call to re-fetch from Remote Config.
     * Useful after a Remote Config force-refresh.
     */
    fun invalidateCache() {
        cache.clear()
    }
}
