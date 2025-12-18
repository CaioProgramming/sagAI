package com.ilustris.sagai.core.ai

import android.util.Base64
import android.util.Log
import com.ilustris.sagai.core.ai.model.AudioConfig
import com.ilustris.sagai.core.ai.model.createAudioGenerationRequest
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
            Log.i(TAG, "Generating audio with ➡ $modelName")
            Log.i(TAG, "Audio Config: ${audioConfig.toJsonFormat()}")

            return billingService.runPremiumRequest {
                val apiKey = apiKey()
                val request =
                    createAudioGenerationRequest(text = audioConfig.prompt, voice = audioConfig.voice)

                Log.d(
                    TAG,
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
                    Log.e(TAG, "Gemini API error: ${error.code} - ${error.message}")
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
                    Log.e(TAG, "No audio data in response")
                    throw Exception("No audio data returned from Gemini API")
                }

                Log.d(TAG, "Received audio data with mimeType: ${inlineData.mimeType}")

                // Log usage metadata
                response.usageMetadata?.let { usage ->
                    Log.d(
                        TAG,
                        "Token usage - Prompt: ${usage.promptTokenCount}, " +
                            "Candidates: ${usage.candidatesTokenCount}, " +
                            "Total: ${usage.totalTokenCount}",
                    )
                }

                // Decode base64 to ByteArray
                Base64.decode(inlineData.data, Base64.DEFAULT)
            }
        }
    }
