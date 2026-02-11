package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.ImageConfig
import com.ilustris.sagai.core.services.RemoteConfigService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to manage the [ImageConfig] from Firebase Remote Config.
 */
@Singleton
class ImageConfigService
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
    ) {
        companion object {
            private const val IMAGE_CONFIG_KEY = "imageConfig"
        }

        suspend fun getImageConfig(): ImageConfig = remoteConfigService.getJson<ImageConfig>(IMAGE_CONFIG_KEY) ?: ImageConfig()
    }
