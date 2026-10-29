package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.TtsVoicesConfig
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.core.services.RemoteConfigService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The TTS voices the app may use, read from Remote Config (`tts_voices`) with [Voice.BUNDLED] as
 * the fallback. Every id that comes from the model or from storage goes through [find], so a voice
 * removed from the list can't reach the TTS request.
 */
@Singleton
class VoiceCatalog
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
    ) {
        private val mutex = Mutex()
        private var cached: List<Voice>? = null

        suspend fun voices(): List<Voice> =
            mutex.withLock {
                cached ?: load().also { cached = it }
            }

        suspend fun find(id: String?): Voice? {
            val key = id?.trim()?.takeIf { it.isNotBlank() } ?: return null
            return voices().firstOrNull { it.id.equals(key, ignoreCase = true) }
        }

        /** The catalog as a prompt-ready list: id, gender and description per line. */
        suspend fun selectionGuide(exclude: Set<String> = emptySet()): String =
            buildString {
                appendLine("Available voices:")
                voices()
                    .filterNot { it.id in exclude }
                    .forEach { appendLine("- ${it.id}: ${it.description} (${it.gender})") }
            }

        private suspend fun load(): List<Voice> {
            val remote =
                runCatching { remoteConfigService.getJson<TtsVoicesConfig>(CONFIG_KEY) }
                    .onFailure { Timber.w(it, "tts_voices unreadable, using bundled voices") }
                    .getOrNull()
                    ?.voices
                    ?.filter { it.id.isNotBlank() }
                    ?.map { it.copy(id = it.id.trim().lowercase()) }
            return remote?.takeIf { it.isNotEmpty() } ?: Voice.BUNDLED
        }

        companion object {
            const val CONFIG_KEY = "tts_voices"
        }
    }
