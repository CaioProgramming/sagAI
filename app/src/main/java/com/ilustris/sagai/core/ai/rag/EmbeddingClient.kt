package com.ilustris.sagai.core.ai.rag

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ilustris.sagai.core.ai.MissingApiKeyException
import com.ilustris.sagai.core.ai.key.ApiUsageTracker
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.core.network.GeminiHttpException
import com.ilustris.sagai.core.services.RemoteConfigService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Turns text into a vector via the Gemini embedding endpoint, on the user's own BYOK key.
 *
 * This is the retrieval half of the saga's RAG index: it never generates narrative content, it
 * only produces the numbers [SemanticRetrievalService] ranks against. Kept deliberately separate
 * from [com.ilustris.sagai.core.ai.AIClient] — that base class exists to assemble prompt
 * blueprints for generation, which an embedding call has none of.
 */
@Singleton
class EmbeddingClient
    @Inject
    constructor(
        okHttpClient: OkHttpClient,
        private val userApiKeyStore: UserApiKeyStore,
        private val remoteConfigService: RemoteConfigService,
        private val apiUsageTracker: ApiUsageTracker,
    ) {
        private val client =
            okHttpClient
                .newBuilder()
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()

        suspend fun isAvailable(): Boolean =
            remoteConfigService.getBoolean(RAG_ENABLED_FLAG) == true &&
                userApiKeyStore.getKeyNow()?.isNotBlank() == true

        /** Embeds one string. Returns null rather than throwing — a failed embed must never break chat. */
        suspend fun embed(text: String): FloatArray? = embedBatch(listOf(text)).firstOrNull()

        /**
         * Embeds up to [BATCH_LIMIT] strings per call (the API's own cap on `batchEmbedContents`).
         * Larger lists are chunked automatically. Returns one vector per input, in order; a chunk
         * that fails yields nulls for its slice rather than aborting the whole batch.
         */
        suspend fun embedBatch(texts: List<String>): List<FloatArray?> =
            withContext(Dispatchers.IO) {
                if (texts.isEmpty()) return@withContext emptyList()
                val apiKey =
                    userApiKeyStore.getKeyNow()?.takeIf { it.isNotBlank() }
                        ?: throw MissingApiKeyException()
                val model = embeddingModel()

                texts.chunked(BATCH_LIMIT).flatMap { chunk ->
                    runCatching { requestBatch(model, apiKey, chunk) }
                        .onFailure { Timber.tag(TAG).w(it, "Embedding batch of ${chunk.size} failed") }
                        .getOrDefault(chunk.map { null })
                }
            }

        private suspend fun embeddingModel(): String =
            remoteConfigService.getString(EMBEDDING_MODEL_FLAG, logEnabled = false)
                ?.takeIf { it.isNotBlank() }
                ?.let { if (it.startsWith("models/")) it else "models/$it" }
                ?: DEFAULT_MODEL

        private suspend fun requestBatch(
            model: String,
            apiKey: String,
            texts: List<String>,
        ): List<FloatArray?> {
            val requests =
                JsonArray().apply {
                    texts.forEach { text ->
                        add(
                            JsonObject().apply {
                                addProperty("model", model)
                                add(
                                    "content",
                                    JsonObject().apply {
                                        add(
                                            "parts",
                                            JsonArray().apply {
                                                add(JsonObject().apply { addProperty("text", text) })
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    }
                }
            val body = JsonObject().apply { add("requests", requests) }.toString()

            val request =
                Request
                    .Builder()
                    .url("$BASE_URL/$model:batchEmbedContents")
                    .header("x-goog-api-key", apiKey)
                    .post(body.toRequestBody(JSON_MEDIA))
                    .build()

            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw GeminiHttpException(response.code, raw)
                // batchEmbedContents carries no token usage in its response; recorded as a bare
                // request against RPD, same as TranscribeClient does for the Interactions API.
                apiUsageTracker.record(model, null)
                return decodeBatchResponse(raw).map { it?.normalized() }
            }
        }

        private fun decodeBatchResponse(raw: String): List<FloatArray?> {
            val root = JsonParser.parseString(raw).asJsonObject
            val embeddings = root.getAsJsonArray("embeddings") ?: return emptyList()
            return embeddings.map { entry ->
                entry.asJsonObject
                    .getAsJsonArray("values")
                    ?.map { it.asFloat }
                    ?.toFloatArray()
            }
        }

        private fun FloatArray.normalized(): FloatArray {
            val magnitude = sqrt(sumOf { (it * it).toDouble() }).toFloat()
            if (magnitude == 0f) return this
            return FloatArray(size) { this[it] / magnitude }
        }

        companion object {
            private const val TAG = "🧭 Embedding"
            private const val TIMEOUT_SECONDS = 30L
            private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
            private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
            private const val BATCH_LIMIT = 100

            /** Remote Config flag gating the whole RAG feature — off means the old behavior stands. */
            const val RAG_ENABLED_FLAG = "rag_enabled"
            private const val EMBEDDING_MODEL_FLAG = "rag_embedding_model"
            private const val DEFAULT_MODEL = "models/text-embedding-004"
        }
    }
