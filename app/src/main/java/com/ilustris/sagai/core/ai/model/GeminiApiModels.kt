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
    val text: String? = null,
    @SerializedName("inline_data")
    val inlineData: GeminiInlineData? = null,
)

data class GeminiGenerationConfig(
    @SerializedName("response_modalities")
    val responseModalities: List<String>? = null,
    @SerializedName("speech_config")
    val speechConfig: GeminiSpeechConfig? = null,
    @SerializedName("temperature")
    val temperature: Float? = null,
    @SerializedName("response_mime_type")
    val responseMimeType: String? = null,
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

data class GeminiErrorResponse(
    val error: GeminiError?,
)

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
    val details: List<GeminiErrorDetail>? = null,
)

data class GeminiErrorDetail(
    @SerializedName("@type")
    val type: String? = null,
    val retryDelay: String? = null,
    val violations: List<GeminiQuotaViolation>? = null,
)

data class GeminiQuotaViolation(
    val quotaMetric: String? = null,
    val quotaId: String? = null,
    val quotaDimensions: Map<String, String>? = null,
    val quotaValue: String? = null,
)

// endregion

// region Builder Extensions

/**
 * Creates a GeminiRequest for audio generation with the specified text, voice, and performance instruction.
 */
fun createAudioGenerationRequest(
    text: String,
    voice: Voice,
    instruction: String? = null,
): GeminiRequest {
    val fullPrompt =
        if (instruction != null) {
            "Instruction: $instruction\n\nScript: $text"
        } else {
            text
        }
    return GeminiRequest(
        contents =
            listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = fullPrompt)),
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
}

// endregion
