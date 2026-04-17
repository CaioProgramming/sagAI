package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import timber.log.Timber

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
        Timber.tag("GenreVisualConfigService").d("Requesting config for $genre")
        if (cache.containsKey(genre) && cache[genre] != null) {
            Timber.tag("GenreVisualConfigService").d("Returning cached config for $genre")
            return cache[genre]
        }

        val key = "${genre.name.lowercase()}_visual_config"
        Timber.tag("GenreVisualConfigService").d("Fetching from remote config with key: $key")
        val config =
            try {
                remoteConfigService.getJson<GenreVisualConfig>(key)
            } catch (e: Exception) {
                Timber.tag("GenreVisualConfigService").e("Failed to fetch visual config for $genre: ${e.message}")
                null
            }
        if (config != null) {
            cache[genre] = config
        } else {
            Timber.tag("GenreVisualConfigService").w("Config for $genre is null/empty in Remote Config")
        }
        return config
    }
}
