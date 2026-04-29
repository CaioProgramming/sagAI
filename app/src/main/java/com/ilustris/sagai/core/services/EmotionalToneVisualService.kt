package com.ilustris.sagai.core.services

import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmotionalToneVisualService
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
    ) {
        suspend fun getVisualUrl(tone: EmotionalTone): String? {
            val map = remoteConfigService.getJson<Map<String, String>>("tone_visuals")
            return map?.get(tone.name)
        }
    }
