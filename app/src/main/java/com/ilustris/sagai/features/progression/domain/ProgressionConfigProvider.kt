package com.ilustris.sagai.features.progression.domain

import com.ilustris.sagai.core.services.RemoteConfigService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one place that reads the `character_progression` Remote Config key. Remote-only: a missing
 * or disabled config means the whole system is off, with no compiled fallback.
 */
@Singleton
class ProgressionConfigProvider
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
    ) {
        /** The config when the system is on, null otherwise (kill switch included). */
        suspend fun activeConfig(): ProgressionConfig? =
            remoteConfigService.getJson<ProgressionConfig>(KEY)?.takeIf { it.enabled }

        private companion object {
            const val KEY = "character_progression"
        }
    }
