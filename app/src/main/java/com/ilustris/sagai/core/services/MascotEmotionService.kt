package com.ilustris.sagai.core.services

import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MascotEmotionService
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
    ) {
        // Cache: Genre -> (EmotionalTone -> URL)
        private val cache = MutableStateFlow<Map<Genre, Map<EmotionalTone, String>>>(emptyMap())

        // Default (genre-agnostic) dragon emotes
        private var defaultEmotions: Map<EmotionalTone, String>? = null

        suspend fun getEmotionUrl(
            genre: Genre,
            tone: EmotionalTone,
        ): String? {
            // 1. Try genre-specific
            val genreMap = fetchForGenre(genre)
            genreMap[tone]?.let { return it }

            // 2. Try default
            val defaults = fetchDefaults()
            return defaults[tone]
        }

        private suspend fun fetchForGenre(genre: Genre): Map<EmotionalTone, String> {
            cache.value[genre]?.let { return it }
            val key = "${genre.name.lowercase()}_mascot_emotions"
            val raw =
                remoteConfigService.getJson<Map<String, String>>(key)
                    ?: emptyMap()

            val mapped =
                raw
                    .mapNotNull { (k, v) ->
                        val tone = EmotionalTone.getTone(k)
                        // Verify if the tone name matches exactly or if it's the fallback NEUTRAL
                        if (tone.name == k.uppercase()) {
                            tone to v
                        } else {
                            null
                        }
                    }.toMap()

            cache.value = cache.value + (genre to mapped)
            return mapped
        }

        private suspend fun fetchDefaults(): Map<EmotionalTone, String> {
            defaultEmotions?.let { return it }
            val raw =
                remoteConfigService.getJson<Map<String, String>>("default_mascot_emotions")
                    ?: emptyMap()

            val mapped =
                raw
                    .mapNotNull { (k, v) ->
                        val tone = EmotionalTone.getTone(k)
                        if (tone.name == k.uppercase()) {
                            tone to v
                        } else {
                            null
                        }
                    }.toMap()

            defaultEmotions = mapped
            return mapped
        }
    }
