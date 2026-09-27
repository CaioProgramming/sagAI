package com.ilustris.sagai.core.ai.rag

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ilustris.sagai.core.ai.MediaModelResolver
import com.ilustris.sagai.core.ai.MediaRequirement
import com.ilustris.sagai.core.ai.MissingApiKeyException
import com.ilustris.sagai.core.ai.key.ApiUsageTracker
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.core.network.GeminiHttpException
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
        private val mediaModelResolver: MediaModelResolver,
        private val apiUsageTracker: ApiUsageTracker,
    ) {
        private val client =
            okHttpClient
                .newBuilder()
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()

        /** Whether EMBEDDING has any model configured — `model_configs.EMBEDDING` — and a BYOK key is set. */
        suspend fun isAvailable(): Boolean =
            mediaModelResolver.candidates(MediaRequirement.EMBEDDING).isNotEmpty() &&
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

                texts.chunked(BATCH_LIMIT).flatMap { chunk ->
                    // Rotates across EMBEDDING's candidates on a 503 or a spent daily quota.
                    runCatching {
                        mediaModelResolver.withRotation(MediaRequirement.EMBEDDING) { model ->
                            requestBatch(model, apiKey, chunk)
                        }
                    }.onFailure { Timber.tag(TAG).w(it, "Embedding batch of ${chunk.size} failed") }
                        .getOrDefault(chunk.map { null })
                }
            }

        private suspend fun requestBatch(
            model: String,
            apiKey: String,
            texts: List<String>,
        ): List<FloatArray?> {
            // `model` is bare (e.g. "text-embedding-004"); the request body needs the full
            // resource name, while the URL path below takes the bare form after BASE_URL's `/models`.
            val modelResourceName = "models/$model"
            val requests =
                JsonArray().apply {
                    texts.forEach { text ->
                        add(
                            JsonObject().apply {
                                addProperty("model", modelResourceName)
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
        }
    }
