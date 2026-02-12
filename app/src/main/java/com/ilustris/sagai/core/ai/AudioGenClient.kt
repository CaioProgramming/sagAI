package com.ilustris.sagai.core.ai

import android.util.Base64
import com.ilustris.sagai.core.ai.model.AudioConfig
import com.ilustris.sagai.core.ai.model.createAudioGenerationRequest
import timber.log.Timber
import com.ilustris.sagai.core.network.GeminiApiService
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.toJsonFormat
import javax.inject.Inject

interface AudioGenClient {
    /**
     * Generates audio from the given AudioConfig.
     * @param audioConfig Contains the voice selection and crafted prompt
     * @return ByteArray of the generated audio or null if failed
     */
    suspend fun generateAudio(audioConfig: AudioConfig): ByteArray?
}

class AudioGenClientImpl
    @Inject
    constructor(
        private val billingService: BillingService,
        private val remoteConfigService: RemoteConfigService,
        private val geminiApiService: GeminiApiService,
    ) : AudioGenClient {
        companion object {
            const val AUDIO_GEN_MODEL_FLAG = "audioGenModel"
            private const val TAG = "🎙️ Audio Generation"
        }

        private suspend fun modelName() =
            remoteConfigService.getString(AUDIO_GEN_MODEL_FLAG)
                ?: error("Couldn't find model for Audio generation")

        private suspend fun apiKey() =
            remoteConfigService.getString(KEY_FLAG)
                ?: error("Couldn't fetch API key for Audio generation")

        override suspend fun generateAudio(audioConfig: AudioConfig): ByteArray? {
            val modelName = modelName()
            Timber.tag(TAG).i("Generating audio with ➡ $modelName")
            Timber.tag(TAG).i("Audio Config: ${audioConfig.toJsonFormat()}")

            return billingService.runPremiumRequest {
                val apiKey = apiKey()
                val cleanPrompt = stripExpressiveTags(audioConfig.prompt)
                val request =
                    createAudioGenerationRequest(
                        text = cleanPrompt,
                        voice = audioConfig.voice,
                        instruction = audioConfig.instruction,
                    )

                Timber.tag(TAG).d(
                    "Sending audio generation request to model: $modelName with voice: ${audioConfig.voice.id}",
                )

                val response =
                    geminiApiService.generateContent(
                        model = modelName,
                        apiKey = apiKey,
                        request = request,
                    )

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
                com.ilustris.sagai.core.utils.AudioUtils
                    .wrapPcmInWav(pcmData)
            }
        }

        private fun stripExpressiveTags(text: String): String =
            text
                .replace(Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL), "")
                .replace(Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL), "")
                .replace(Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL), "")
                .trim()
                .replace("\\s+".toRegex(), " ")
    }
