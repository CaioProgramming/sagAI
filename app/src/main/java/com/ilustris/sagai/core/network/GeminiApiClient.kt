package com.ilustris.sagai.core.network

import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.GeminiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

/** Direct OkHttp client for Gemini REST (R8-safe; no Retrofit suspend adapters). */
@Singleton
class GeminiApiClient
    @Inject
    constructor(
        private val okHttpClient: OkHttpClient,
    ) {
        suspend fun generateContent(
            model: String,
            apiKey: String,
            request: GeminiRequest,
        ): GeminiResponse =
            withContext(Dispatchers.IO) {
                val httpRequest =
                    Request
                        .Builder()
                        .url("$BASE_URL/models/$model:generateContent")
                        .header("x-goog-api-key", apiKey)
                        .post(GeminiApiCodec.encodeRequest(request))
                        .build()

                okHttpClient.newCall(httpRequest).execute().use { response ->
                    val bodyString = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw GeminiHttpException(response.code, bodyString)
                    }
                    GeminiApiCodec.decodeResponse(bodyString)
                }
            }

        /** Caller must close the returned body (e.g. with [use]). */
        suspend fun streamGenerateContent(
            model: String,
            apiKey: String,
            request: GeminiRequest,
        ): ResponseBody =
            withContext(Dispatchers.IO) {
                val httpRequest =
                    Request
                        .Builder()
                        .url("$BASE_URL/models/$model:streamGenerateContent?alt=sse")
                        .header("x-goog-api-key", apiKey)
                        .post(GeminiApiCodec.encodeRequest(request))
                        .build()

                val response = okHttpClient.newCall(httpRequest).execute()
                val body = response.body
                if (!response.isSuccessful || body == null) {
                    val errBody = body?.string().orEmpty()
                    response.close()
                    throw GeminiHttpException(response.code, errBody)
                }
                body
            }

        companion object {
            private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        }
    }
