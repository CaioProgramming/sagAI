package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.core.ai.model.GeminiContent
import com.ilustris.sagai.core.ai.model.GeminiErrorResponse
import com.ilustris.sagai.core.ai.model.GeminiGenerationConfig
import com.ilustris.sagai.core.ai.model.GeminiInlineData
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.network.GeminiApiService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.sanitizeAndExtractJsonString
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class GemmaClient
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
        val geminiApiService: GeminiApiService,
        val promptService: PromptService,
    ) : AIClient() {
        @PublishedApi
        internal val requestMutex = Mutex()

        @PublishedApi
        @Volatile
        internal var retryDelay: Int? = null

        @Volatile
        var lastTokenCount: Int = 0

        companion object {
            const val CORE_FLAG = "SAGA_CORE"
            const val INPUT_TOKEN_LIMIT = 15000
            const val REACTIVE_DELAY_THRESHOLD = 0.7f
            const val MAX_RETRIES = 2
            const val RETRY_DELAY = 20
        }

        enum class ModelRequirement(
            val flag: String,
            val defaultModel: String,
        ) {
            LOW("gemma_low_tier", "models/gemma-3-1b-it"),
            MEDIUM("gemma_medium_tier", "models/gemma-3-12b-it"),
            HIGH("gemma_high_tier", "models/gemma-3-27b-it"),
        }

        suspend fun modelName(requirement: ModelRequirement) =
            remoteConfigService.getString(requirement.flag)?.let {
                it.ifEmpty {
                    requirement.defaultModel
                }
            } ?: requirement.defaultModel

        suspend fun coreKey() =
            remoteConfigService.getString(CORE_FLAG, false)?.let {
                it.ifEmpty {
                    error("Couldn't fetch gemma Model")
                }
            } ?: error("Couldn't get Flag value")

        suspend fun apiConfig(useCore: Boolean): String =
            if (useCore) {
                coreKey()
            } else {
                remoteConfigService.getString(KEY_FLAG, false)?.ifEmpty {
                    error("Couldn't fetch firebase key")
                } ?: error("Flag Value unavailable.")
            }

        suspend inline fun <reified T> generate(
            prompt: String,
            references: List<ImageReference?> = emptyList(),
            temperatureRandomness: Float = .5f,
            requireTranslation: Boolean = true,
            describeOutput: Boolean = true,
            filterOutputFields: List<String> = emptyList(),
            useCore: Boolean = false,
            requirement: ModelRequirement = ModelRequirement.HIGH,
        ): T? =
            withContext(Dispatchers.IO) {
                if (lastTokenCount > (INPUT_TOKEN_LIMIT * REACTIVE_DELAY_THRESHOLD) && retryDelay == null) {
                    Log.w(
                        javaClass.simpleName,
                        "Applying reactive delay due to high token count in last request.",
                    )
                    retryDelay = 5
                    delay((retryDelay ?: 5).seconds)
                }
                val model = modelName(requirement)
                if (useCore.not()) {
                    retryDelay?.let {
                        Log.e(
                            javaClass.simpleName,
                            "generate: Trying delay $retryDelay seconds to avoid rate limit.",
                        )
                        delay(it.seconds)
                    }
                } else {
                    Log.i(javaClass.simpleName, "generate: Core calls don't require delay.")
                }

                val maxAttempts = if (requirement == ModelRequirement.HIGH) MAX_RETRIES + 1 else 1

                for (currentAttempt in 1..maxAttempts) {
                    try {
                        return@withContext requestMutex.withLock {
                            val structure =
                                if (T::class != String::class &&
                                    describeOutput
                                ) {
                                    toJsonMap(T::class.java, filteredFields = filterOutputFields)
                                } else {
                                    if (T::class == String::class) {
                                        "Simple String text no JSON Object"
                                    } else {
                                        T::class.java.simpleName
                                    }
                                }

                            val formattingRule =
                                if (T::class == String::class) {
                                    "Respond using PURE NARRATIVE TEXT. No brackets, no keys, and no JSON wrapping."
                                } else {
                                    "Respond using STRICTLY VALID JSON. Maintain escaping and UTF-8 encoding."
                                }

                            val corePrompt =
                                promptService.buildRemotePrompt(
                                    remoteConfigKey = "core_blueprint",
                                    variables =
                                        mapOf(
                                            "language" to getLanguage(requireTranslation),
                                            "type" to T::class.java.simpleName,
                                            "structure" to structure,
                                            "formattingRule" to formattingRule,
                                        ),
                                    logEnabled = false,
                                )

                            val fullPrompt =
                                buildString {
                                    appendLine(prompt)
                                    appendLine()
                                    appendLine(corePrompt)
                                }

                            val promptLength =
                                fullPrompt.length +
                                    references.filterNotNull().sumOf {
                                        it.description.length
                                    }
                            Log.i(
                                this@GemmaClient::class.java.simpleName,
                                "Requesting $model\nPrompt with $promptLength chars.",
                            )

                            if (promptLength > (INPUT_TOKEN_LIMIT * 5)) {
                                throw IllegalArgumentException("Prompt is too long. verify your prompt and try again.")
                            }

                            val parts = mutableListOf<GeminiPart>()
                            parts.add(GeminiPart(text = fullPrompt))

                            references.filterNotNull().forEach { reference ->
                                parts.add(
                                    GeminiPart(
                                        inlineData =
                                            GeminiInlineData(
                                                mimeType = "image/jpeg",
                                                data = reference.bitmap.toBase64(),
                                            ),
                                    ),
                                )
                                parts.add(GeminiPart(text = reference.description))
                            }

                            val geminiRequest =
                                GeminiRequest(
                                    contents = listOf(GeminiContent(parts = parts)),
                                    generationConfig =
                                        GeminiGenerationConfig(
                                            temperature = temperatureRandomness,
                                        ),
                                )

                            val response =
                                geminiApiService.generateContent(
                                    model = model,
                                    apiKey = apiConfig(useCore),
                                    request = geminiRequest,
                                )

                            // Check for API error
                            response.error?.let { error ->
                                Log.e(
                                    javaClass.simpleName,
                                    "Gemini API error: ${error.code} - ${error.message}",
                                )
                                throw Exception("Gemini API error: ${error.message}")
                            }

                            lastTokenCount = response.usageMetadata?.promptTokenCount ?: 0
                            retryDelay = null
                            if (lastTokenCount < (INPUT_TOKEN_LIMIT * REACTIVE_DELAY_THRESHOLD)) {
                                lastTokenCount = 0
                            }

                            val responseText =
                                response.candidates
                                    ?.firstOrNull()
                                    ?.content
                                    ?.parts
                                    ?.firstOrNull()
                                    ?.text ?: ""

                            Log.d(
                                javaClass.simpleName,
                                "Input JSON: ${
                                    geminiRequest.toJsonFormatExcludingFields(
                                        AI_EXCLUDED_FIELDS,
                                    )
                                }",
                            )

                            Log.d(javaClass.simpleName, "Prompt requested:\n$fullPrompt")

                            Log.i(
                                javaClass.simpleName,
                                "Generated content: $responseText",
                            )

                            if (T::class == String::class) {
                                val cleanedText =
                                    responseText
                                        .replace(Regex("```[a-zA-Z]*"), "")
                                        .replace("```", "")
                                        .trim()
                                Log.i(
                                    javaClass.simpleName,
                                    "Prompt request result (Cleaned):\n$cleanedText",
                                )
                                return@withLock cleanedText as T
                            }

                            val cleanedJsonString =
                                responseText.sanitizeAndExtractJsonString(T::class.java)
                            val typeToken = object : TypeToken<T>() {}
                            Gson().fromJson(cleanedJsonString, typeToken.type)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            this@GemmaClient::class.java.simpleName,
                            "Error in Generation($model) Attempt $currentAttempt/$maxAttempts: ${e.javaClass.simpleName} - ${e.message}",
                        )

                        // Check if it's a parsing error (no delay needed) or network error (delay recommended)
                        val isParsingError =
                            e is JsonSyntaxException || e is JsonParseException || e is IllegalArgumentException
                        var extractedDelay: Long? = null

                        if (e is HttpException) {
                            val errorBody = e.response()?.errorBody()?.string()
                            Log.e(
                                this@GemmaClient::class.java.simpleName,
                                "HTTP Error ($model): $errorBody",
                            )

                            try {
                                val errorResponse =
                                    Gson().fromJson(errorBody, GeminiErrorResponse::class.java)
                                val retryInfo =
                                    errorResponse.error?.details?.find {
                                        it.type == "type.googleapis.com/google.rpc.RetryInfo"
                                    }
                                extractedDelay =
                                    retryInfo
                                        ?.retryDelay
                                        ?.removeSuffix("s")
                                        ?.toDoubleOrNull()
                                        ?.toLong()

                                errorResponse.error?.details?.forEach { detail ->
                                    detail.violations?.forEach { violation ->
                                        Log.w(
                                            this@GemmaClient::class.java.simpleName,
                                            "Quota Violation: ${violation.quotaId} - ${violation.quotaMetric} (Value: ${violation.quotaValue})",
                                        )
                                    }
                                }

                                if (extractedDelay != null) {
                                    Log.i(
                                        this@GemmaClient::class.java.simpleName,
                                        "Extracted precise delay from error: $extractedDelay seconds",
                                    )
                                }
                            } catch (parseEx: Exception) {
                                Log.e(
                                    this@GemmaClient::class.java.simpleName,
                                    "Failed to parse error body: ${parseEx.message}",
                                )
                            }
                        }

                        if (currentAttempt < maxAttempts) {
                            val delayToApply =
                                if (isParsingError) 0L else (extractedDelay ?: RETRY_DELAY.toLong())

                            if (delayToApply > 0) {
                                Log.w(
                                    this@GemmaClient::class.java.simpleName,
                                    "Retrying HIGH priority request in $delayToApply seconds due to ${e.javaClass.simpleName}...",
                                )
                                delay(delayToApply.seconds)
                            } else {
                                Log.w(
                                    this@GemmaClient::class.java.simpleName,
                                    "Retrying immediately due to parsing error (${e.javaClass.simpleName})...",
                                )
                            }
                        } else {
                            // Final failure
                            if (!isParsingError) {
                                retryDelay =
                                    extractedDelay?.toInt() ?: retryDelay?.let {
                                        if (it > 30) it / 2 else it + it
                                    } ?: 2
                            }
                            Log.e(
                                javaClass.simpleName,
                                "Final failure after $maxAttempts attempts.",
                            )
                            Log.e(javaClass.simpleName, "generate: Failed prompt")
                            Log.w(javaClass.simpleName, prompt)
                            return@withContext null
                        }
                    }
                }
                return@withContext null
            }

        /**
         * Recursively checks if a class or any of its nested classes contain String fields.
         */
        @PublishedApi
        internal fun containsStringFields(
            clazz: Class<*>,
            visited: MutableSet<Class<*>> = mutableSetOf(),
        ): Boolean {
            if (clazz in visited || clazz.isPrimitive || clazz.isEnum) return false
            if (clazz == String::class.java) return true
            visited.add(clazz)

            return clazz.declaredFields.any { field ->
                val fieldType = field.type
                when {
                    fieldType == String::class.java -> true
                    fieldType.isPrimitive || fieldType.isEnum -> false
                    else -> containsStringFields(fieldType, visited)
                }
            }
        }

        fun Bitmap.toBase64(): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            this.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.NO_WRAP)
        }
    }

const val KEY_FLAG = "FIREBASE_KEY"
