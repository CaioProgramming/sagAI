package com.ilustris.sagai.core.ai.model

import com.google.gson.annotations.SerializedName

/**
 * Request/Response models for Gemini REST API audio generation.
 * Based on: https://ai.google.dev/gemini-api/docs/speech-generation
 */

// region Request Models

data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerializedName("generationConfig")
    val generationConfig: GeminiGenerationConfig,
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user",
)

data class GeminiPart(
    val text: String,
)

data class GeminiGenerationConfig(
    @SerializedName("response_modalities")
    val responseModalities: List<String>,
    @SerializedName("speech_config")
    val speechConfig: GeminiSpeechConfig? = null,
)

data class GeminiSpeechConfig(
    @SerializedName("voice_config")
    val voiceConfig: GeminiVoiceConfig,
)

data class GeminiVoiceConfig(
    @SerializedName("prebuilt_voice_config")
    val prebuiltVoiceConfig: PrebuiltVoiceConfig,
)

data class PrebuiltVoiceConfig(
    @SerializedName("voice_name")
    val voiceName: String,
)

// endregion

// region Response Models

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    val usageMetadata: GeminiUsageMetadata?,
    val error: GeminiError?,
)

data class GeminiCandidate(
    val content: GeminiResponseContent?,
    val finishReason: String?,
)

data class GeminiResponseContent(
    val parts: List<GeminiResponsePart>?,
    val role: String?,
)

data class GeminiResponsePart(
    val text: String?,
    val inlineData: GeminiInlineData?,
)

data class GeminiInlineData(
    val mimeType: String?,
    val data: String?, // Base64 encoded audio data
)

data class GeminiUsageMetadata(
    val promptTokenCount: Int?,
    val candidatesTokenCount: Int?,
    val totalTokenCount: Int?,
)

data class GeminiError(
    val code: Int?,
    val message: String?,
    val status: String?,
)

// endregion

// region Builder Extensions

/**
 * Creates a GeminiRequest for audio generation with the specified text and voice.
 */
fun createAudioGenerationRequest(
    text: String,
    voice: Voice,
): GeminiRequest =
    GeminiRequest(
        contents =
            listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = text)),
                ),
            ),
        generationConfig =
            GeminiGenerationConfig(
                responseModalities = listOf("AUDIO"),
                speechConfig =
                    GeminiSpeechConfig(
                        voiceConfig =
                            GeminiVoiceConfig(
                                prebuiltVoiceConfig =
                                    PrebuiltVoiceConfig(
                                        voiceName = voice.id,
                                    ),
                            ),
                    ),
            ),
    )

// endregion
