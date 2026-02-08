package com.ilustris.sagai.core.network

import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit service interface for Gemini REST API.
 * Used for audio generation since Firebase SDK doesn't support it on Android.
 */
interface GeminiApiService {
    /**
     * Generate content using Gemini API.
     * For audio generation, use responseModalities: ["AUDIO"] in generationConfig.
     *
     * @param model The model name (e.g., "gemini-2.5-flash")
     * @param apiKey The Gemini API key passed via header
     * @param request The generation request body
     * @return GeminiResponse containing audio data in base64 format
     */
    @POST("models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest,
    ): GeminiResponse
}
