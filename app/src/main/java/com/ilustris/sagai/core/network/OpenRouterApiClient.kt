package com.ilustris.sagai.core.network

import com.ilustris.sagai.core.ai.model.OpenRouterRequest
import com.ilustris.sagai.core.ai.model.OpenRouterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

/** Direct OkHttp client for OpenRouter REST (R8-safe; no Retrofit suspend adapters). */
@Singleton
class OpenRouterApiClient
    @Inject
    constructor(
        private val okHttpClient: OkHttpClient,
    ) {
        suspend fun generateContent(
            apiKey: String,
            request: OpenRouterRequest,
        ): OpenRouterResponse =
            withContext(Dispatchers.IO) {
                val httpRequest =
                    Request
                        .Builder()
                        .url("$BASE_URL/chat/completions")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer $apiKey")
                        .post(OpenRouterApiCodec.encodeRequest(request))
                        .build()

                okHttpClient.newCall(httpRequest).execute().use { response ->
                    val bodyString = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw OpenRouterHttpException(response.code, bodyString)
                    }
                    OpenRouterApiCodec.decodeResponse(bodyString)
                }
            }

        /** Caller must close the returned body (e.g. with [use]). */
        suspend fun streamGenerateContent(
            apiKey: String,
            request: OpenRouterRequest,
        ): ResponseBody =
            withContext(Dispatchers.IO) {
                val httpRequest =
                    Request
                        .Builder()
                        .url("$BASE_URL/chat/completions")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer $apiKey")
                        .post(OpenRouterApiCodec.encodeRequest(request))
                        .build()

                val response = okHttpClient.newCall(httpRequest).execute()
                val body = response.body
                if (!response.isSuccessful || body == null) {
                    val errBody = body?.string().orEmpty()
                    response.close()
                    throw OpenRouterHttpException(response.code, errBody)
                }
                body
            }

        companion object {
            private const val BASE_URL = "https://openrouter.ai/api/v1"
        }
    }
