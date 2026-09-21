package com.ilustris.sagai.core.ai

import android.util.Base64
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ilustris.sagai.core.ai.key.ApiUsageTracker
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.core.network.GeminiHttpException
import com.ilustris.sagai.features.audiobook.data.usecase.SpokenWord
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

class TranscriptionException(
    message: String,
) : Exception(message)

data class Transcription(
    val text: String,
    val words: List<SpokenWord>,
    /** Raw response, kept for the debug sync comparison. */
    val rawJson: String,
)

/**
 * Speech-to-text with word-level timestamps through the Interactions API (`gemini-3.5-transcribe`).
 * Audio is sent inline, so clips must stay under the 20 MB request cap (~3.5 min of 24 kHz WAV).
 */
@Singleton
class TranscribeClient
    @Inject
    constructor(
        okHttpClient: OkHttpClient,
        private val userApiKeyStore: UserApiKeyStore,
        private val mediaModelResolver: MediaModelResolver,
        private val apiUsageTracker: ApiUsageTracker,
    ) {
        /** Inline audio makes bodies several MB: no debug body logging, and room for long clips. */
        private val client =
            okHttpClient
                .newBuilder()
                .apply { interceptors().clear() }
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()

        /**
         * Whether TRANSCRIBE has any model configured — `model_configs.TRANSCRIBE`, or [legacyModel]
         * (`book_audio_config.transcribeModel`) while that entry doesn't exist yet.
         */
        suspend fun isAvailable(legacyModel: String? = null): Boolean =
            mediaModelResolver.candidates(MediaRequirement.TRANSCRIBE, legacyModel).isNotEmpty()

        suspend fun transcribeWords(
            wav: ByteArray,
            legacyModel: String? = null,
            languageCode: String? = null,
        ): Transcription =
            withContext(Dispatchers.IO) {
                val apiKey = apiKey()
                // Rotates across TRANSCRIBE's candidates on a 503 or a spent daily quota.
                mediaModelResolver.withRotation(MediaRequirement.TRANSCRIBE, legacyModel) { model ->
                    val body = encodeRequest(model, wav, languageCode)
                    val request =
                        Request
                            .Builder()
                            .url("$BASE_URL/interactions")
                            .header("x-goog-api-key", apiKey)
                            .post(body.toRequestBody(JSON_MEDIA))
                            .build()

                    Timber.tag(TAG).d("Transcribing ${wav.size / 1024} KB with $model")
                    client.newCall(request).execute().use { response ->
                        val raw = response.body?.string().orEmpty()
                        if (!response.isSuccessful) throw GeminiHttpException(response.code, raw)
                        // The Interactions API doesn't return usageMetadata the way generateContent
                        // does — recorded with no token counts, just the request itself against RPD.
                        apiUsageTracker.record(model, null)
                        decodeResponse(raw)
                    }
                }
            }

        private suspend fun apiKey(): String =
            userApiKeyStore.getKeyNow()?.takeIf { it.isNotBlank() }
                ?: throw MissingApiKeyException()

        private fun encodeRequest(
            model: String,
            wav: ByteArray,
            languageCode: String?,
        ): String {
            val audio =
                JsonObject().apply {
                    addProperty("type", "audio")
                    addProperty("data", Base64.encodeToString(wav, Base64.NO_WRAP))
                    addProperty("mime_type", "audio/wav")
                }
            val mode =
                JsonObject().apply {
                    addProperty("type", "verbatim")
                    add("timestamp_granularities", JsonArray().apply { add("word") })
                }
            val transcriptionConfig =
                JsonObject().apply {
                    add("mode", mode)
                    add("language_codes", JsonArray().apply { languageCode?.let { add(it) } })
                }
            return JsonObject()
                .apply {
                    addProperty("model", model)
                    add("input", JsonArray().apply { add(audio) })
                    add("generation_config", JsonObject().apply { add("transcription_config", transcriptionConfig) })
                }.toString()
        }

        private fun decodeResponse(raw: String): Transcription {
            val root = JsonParser.parseString(raw).asJsonObject
            val status = root.get("status")?.asString
            if (status != null && status != "completed") {
                throw TranscriptionException("Transcription finished with status $status")
            }

            val texts = mutableListOf<String>()
            val words = mutableListOf<SpokenWord>()
            root.getAsJsonArray("steps")?.forEach { step ->
                step.asJsonObject.getAsJsonArray("content")?.forEach { content ->
                    val contentObject = content.asJsonObject
                    contentObject.get("text")?.asString?.let(texts::add)
                    contentObject.getAsJsonArray("annotations")?.forEach { annotation ->
                        val word = annotation.asJsonObject
                        if (word.get("type")?.asString != "word_info") return@forEach
                        val text = word.get("text")?.asString ?: return@forEach
                        val start = parseOffset(word.get("start_offset")?.asString) ?: return@forEach
                        val end = parseOffset(word.get("end_offset")?.asString) ?: start
                        words += SpokenWord(text, start, end)
                    }
                }
            }
            val text = texts.joinToString(" ").ifBlank { root.get("output_text")?.asString.orEmpty() }
            if (words.isEmpty()) throw TranscriptionException("Transcription returned no word timings")
            return Transcription(text, words, raw)
        }

        /** Offsets come as protobuf durations, e.g. "1.250s". */
        private fun parseOffset(value: String?): Long? =
            value
                ?.removeSuffix("s")
                ?.toDoubleOrNull()
                ?.let { (it * 1000).toLong() }

        companion object {
            private const val TAG = "📝 Transcribe"
            private const val TIMEOUT_SECONDS = 300L
            private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
            private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        }
    }
