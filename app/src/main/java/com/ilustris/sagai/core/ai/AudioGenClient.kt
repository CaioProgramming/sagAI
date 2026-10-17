package com.ilustris.sagai.core.ai

import android.util.Base64
import com.ilustris.sagai.core.ai.key.ApiUsageTracker
import com.ilustris.sagai.core.ai.key.QuotaStatus
import com.ilustris.sagai.core.ai.model.AudioConfig
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.createAudioGenerationRequest
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.core.network.GeminiApiClient
import com.ilustris.sagai.core.utils.toJsonFormat
import javax.inject.Inject

interface AudioGenClient {
    /**
     * Generates audio from the given AudioConfig.
     * @param audioConfig Contains the voice selection and crafted prompt
     * @return ByteArray of the generated audio or null if failed
     */
    suspend fun generateAudio(audioConfig: AudioConfig): ByteArray?

    /**
     * Synthesizes an already-built TTS request (e.g. a two-speaker performance from
     * [com.ilustris.sagai.core.ai.model.createMultiSpeakerAudioRequest]). Same rotation, error
     * handling and WAV wrapping as [generateAudio].
     */
    suspend fun generate(request: GeminiRequest): ByteArray

    /** Whether TTS generation is currently blocked by a spent daily quota, and when it clears. */
    suspend fun quotaStatus(): Flow<QuotaStatus>
}

class AudioGenClientImpl
    @Inject
    constructor(
        private val geminiApiClient: GeminiApiClient,
        private val userApiKeyStore: UserApiKeyStore,
        private val mediaModelResolver: MediaModelResolver,
        private val apiUsageTracker: ApiUsageTracker,
    ) : AudioGenClient {
        companion object {
            private const val TAG = "🎙️ Audio Generation"

            // TTS only answers once the whole clip is synthesized; a full narration segment
            // (thousands of characters, minutes of speech) routinely outlasts the shared 120s.
            private const val AUDIO_READ_TIMEOUT_SECONDS = 300L
        }

        private suspend fun apiKey(): String =
            userApiKeyStore.getKeyNow()?.takeIf { it.isNotBlank() }
                ?: throw MissingApiKeyException()

        override suspend fun quotaStatus(): Flow<QuotaStatus> = mediaModelResolver.tierQuotaStatus(MediaRequirement.AUDIO)

        override suspend fun generateAudio(audioConfig: AudioConfig): ByteArray? {
            Timber.tag(TAG).i("Audio Config: ${audioConfig.toJsonFormat()}")

            val cleanPrompt = stripExpressiveTags(audioConfig.prompt)
            val request =
                createAudioGenerationRequest(
                    text = cleanPrompt,
                    voice = audioConfig.voice,
                    instruction = audioConfig.instruction,
                )
            return generate(request)
        }

        override suspend fun generate(request: GeminiRequest): ByteArray {
            val apiKey = apiKey()

            // Rotates across AUDIO's candidates on a 503 or a spent daily quota.
            val response =
                mediaModelResolver.withRotation(MediaRequirement.AUDIO) { model ->
                    Timber.tag(TAG).d("Generating audio with ➡ $model")
                    geminiApiClient
                        .generateContent(model, apiKey, request, readTimeoutSeconds = AUDIO_READ_TIMEOUT_SECONDS)
                        .also { apiUsageTracker.record(model, it.usageMetadata) }
                }

            // Check for API error
            response.error?.let { error ->
                Timber.tag(TAG).e("Gemini API error: ${error.code} - ${error.message}")
                throw Exception("Gemini API error: ${error.message}")
            }

            // Extract base64 audio data from response
            val inlineData =
                response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull { it.inlineData != null }
                    ?.inlineData

            if (inlineData?.data == null) {
                Timber.tag(TAG).e("No audio data in response")
                throw Exception("No audio data returned from Gemini API")
            }

            Timber.tag(TAG).d("Received audio data with mimeType: ${inlineData.mimeType}")

            // Log usage metadata
            response.usageMetadata?.let { usage ->
                Timber.tag(TAG).d(
                    "Token usage - Prompt: ${usage.promptTokenCount}, " +
                        "Candidates: ${usage.candidatesTokenCount}, " +
                        "Total: ${usage.totalTokenCount}",
                )
            }

            // Clean the base64 data (remove newlines, whitespace)
            val cleanData = inlineData.data?.replace("\\s".toRegex(), "") ?: ""

            // Decode base64 to ByteArray
            val pcmData = Base64.decode(cleanData, Base64.DEFAULT)

            // Wrap raw PCM in WAV for playback compatibility
            return com.ilustris.sagai.core.utils.AudioUtils
                .wrapPcmInWav(pcmData)
        }

        private fun stripExpressiveTags(text: String): String =
            text
                .replace(Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL), "")
                .replace(Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL), "")
                .replace(Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL), "")
                .trim()
                .replace("\\s+".toRegex(), " ")
    }
