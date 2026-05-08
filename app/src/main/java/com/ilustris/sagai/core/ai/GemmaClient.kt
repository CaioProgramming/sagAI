package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.model.AIGeneration
import com.ilustris.sagai.core.ai.model.GeminiContent
import com.ilustris.sagai.core.ai.model.GeminiErrorResponse
import com.ilustris.sagai.core.ai.model.GeminiGenerationConfig
import com.ilustris.sagai.core.ai.model.GeminiInlineData
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.GeminiResponse
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.model.SafeGuard
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import com.ilustris.sagai.core.network.GeminiApiService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.SideEffectService
import com.ilustris.sagai.core.utils.sanitizeAndExtractJsonString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.lang.reflect.ParameterizedType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class GemmaClient
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
        val safetyClient: SafetyClient,
        val sideEffectService: SideEffectService,
        val geminiApiService: GeminiApiService,
        val promptService: PromptService,
        @PublishedApi internal val aiAuditLogDao: AIAuditLogDao,
    ) : AIClient() {
        @PublishedApi
        internal val requestMutexes = java.util.concurrent.ConcurrentHashMap<String, Mutex>()

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

        enum class ModelRequirement {
            TINY,
            LOW,
            MEDIUM,
            HIGH,
        }

        suspend fun modelName(requirement: ModelRequirement): String {
            val tierConfig =
                remoteConfigService.getJson<Map<String, Any>>("model_configs") ?: emptyMap()
            return when (val config = tierConfig[requirement.name]) {
                is String -> {
                    config.replace("models/", "")
                }

                is Map<*, *> -> {
                    val enabled = config["enabled"] as? Boolean ?: true
                    if (!enabled) throw ModelOutageException(requirement)
                    val model =
                        config["model"] as? String
                            ?: error("Model name not found in config for ${requirement.name}")
                    model.replace("models/", "")
                }

                else -> {
                    Timber.e("Invalid model configuration for ${requirement.name}: $config")
                    error("Invalid model configuration for ${requirement.name}")
                }
            }
        }

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

        /**
         * @param blueprintKey Optional key identifying the prompt blueprint used. Providing this greatly helps trace prompt generation in the local debugging ai_audit_logs database.
         */
        suspend inline fun <reified T> generate(
            prompt: String,
            userInteraction: Boolean = false,
            references: List<ImageReference?> = emptyList(),
            temperatureRandomness: Float = .5f,
            requireTranslation: Boolean = true,
            describeOutput: Boolean = true,
            filterOutputFields: List<String> = emptyList(),
            useCore: Boolean = false,
            requirement: ModelRequirement = ModelRequirement.LOW,
            blueprintKey: String? = null,
            logEnabled: Boolean = true,
        ): T? =
            withContext(Dispatchers.IO) {
                if (userInteraction) {
                    val safetyStatus = safetyClient.checkSafety(prompt)
                    if (safetyStatus != SafeGuard.OK) {
                        throw GuardrailsException(safetyStatus)
                    }
                }
                if (lastTokenCount > (INPUT_TOKEN_LIMIT * REACTIVE_DELAY_THRESHOLD) && retryDelay == null) {
                    if (logEnabled) Timber.w("Applying reactive delay due to high token count in last request.")
                    retryDelay = 5
                    delay((retryDelay ?: 5).seconds)
                }
                val model = modelName(requirement)
                if (useCore.not()) {
                    retryDelay?.let {
                        if (logEnabled) Timber.e("generate: Trying delay $retryDelay seconds to avoid rate limit.")
                        delay(it.seconds)
                    }
                } else {
                    if (logEnabled) Timber.i("generate: Core calls don't require delay.")
                }

                val maxAttempts = MAX_RETRIES + 1
                val startTime = System.currentTimeMillis()

                for (currentAttempt in 1..maxAttempts) {
                    try {
                        return@withContext requestMutexes.getOrPut(model) { Mutex() }.withLock {
                            val structure =
                                if (describeOutput) {
                                    val dataType = object : TypeToken<T>() {}.type
                                    val dataStructure =
                                        if (T::class == String::class) {
                                            "\"string\""
                                        } else if (dataType is ParameterizedType && dataType.rawType == GeneratedContent::class.java) {
                                            val innerType =
                                                dataType.actualTypeArguments[0] as Class<*>
                                            val innerStructure =
                                                toJsonMap(
                                                    innerType,
                                                    filteredFields = filterOutputFields,
                                                )
                                            toJsonMap(
                                                GeneratedContent::class.java,
                                                fieldCustomDescriptions = listOf("data" to innerStructure),
                                                filteredFields = filterOutputFields,
                                            )
                                        } else {
                                            toJsonMap(
                                                T::class.java,
                                                filteredFields = filterOutputFields,
                                            )
                                        }
                                    toJsonMap(
                                        AIGeneration::class.java,
                                        fieldCustomDescriptions = listOf("data" to dataStructure),
                                    )
                                } else {
                                    T::class.java.simpleName
                                }

                            val formattingRule =
                                "Respond using STRICTLY VALID JSON. Maintain escaping and UTF-8 encoding."

                            requirement.name.lowercase()
                            val corePrompt =
                                promptService.buildRemotePrompt(
                                    remoteConfigKey = "core_blueprint",
                                    variables =
                                        mapOf(
                                            "language" to getLanguage(requireTranslation),
                                            "type" to T::class.java.simpleName,
                                            "structure" to structure,
                                            "formattingRule" to formattingRule,
                                            "task" to prompt,
                                        ),
                                    logEnabled = false,
                                )

                            val fullPrompt = corePrompt

                            val promptLength =
                                fullPrompt.length +
                                    references.filterNotNull().sumOf {
                                        it.description.length
                                    }
                            if (logEnabled) Timber.i("Requesting $model\nPrompt with $promptLength chars.")

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
                                            temperature =
                                                if (requirement == ModelRequirement.TINY ||
                                                    requirement == ModelRequirement.LOW
                                                ) {
                                                    0.1f
                                                } else {
                                                    temperatureRandomness
                                                },
                                        ),
                                )

                            val formattedModel = model.replace("models/", "")
                            val response =
                                geminiApiService.generateContent(
                                    model = formattedModel,
                                    apiKey = apiConfig(useCore),
                                    request = geminiRequest,
                                )

                            // Check for API error
                            response.error?.let { error ->
                                if (logEnabled) Timber.e("Gemini API error: ${error.code} - ${error.message}")
                                throw Exception("Gemini API error: ${error.message}")
                            }

                            lastTokenCount = response.usageMetadata?.promptTokenCount ?: 0
                            retryDelay = null
                            if (lastTokenCount < (INPUT_TOKEN_LIMIT * REACTIVE_DELAY_THRESHOLD)) {
                                lastTokenCount = 0
                            }

                            val responseContent =
                                response.candidates
                                    ?.firstOrNull()
                                    ?.content
                                    ?.parts

                            val requiredText = responseContent?.lastOrNull()?.text

                            if (logEnabled) {
                                Timber.d(
                                    "Input JSON: ${
                                        geminiRequest.toJsonFormatExcludingFields(
                                            AI_EXCLUDED_FIELDS,
                                        )
                                    }",
                                )

                                Timber.d("Prompt requested:\n$fullPrompt")

                                Timber.i("API Response: ${response.toJsonFormat()}")
                            }

                            val cleanedJsonString =
                                requiredText.sanitizeAndExtractJsonString(AIGeneration::class.java)
                            val typeToken = object : TypeToken<AIGeneration<T>>() {}
                            val aiGeneration =
                                Gson().fromJson<AIGeneration<T>>(cleanedJsonString, typeToken.type)
                            val duration = System.currentTimeMillis() - startTime
                            if (BuildConfig.DEBUG && logEnabled) {
                                aiAuditLogDao.insertLog(
                                    AIAuditLog(
                                        model = model,
                                        blueprintKey = blueprintKey,
                                        dataType = T::class.java.simpleName,
                                        status = "SUCCESS",
                                        reasoning = aiGeneration?.reasoning,
                                        rawResponse = requiredText,
                                        responseTime = duration,
                                    ),
                                )
                                Timber.i("Generation Bench: $model took ${duration}ms (Prompt: $promptLength chars)")
                            }
                            val data = aiGeneration.data
                            if (logEnabled) Timber.d("AI data ->\n$data\n")
                            data
                        }
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG && logEnabled) {
                            try {
                                val duration = System.currentTimeMillis() - startTime
                                val safetyStatus =
                                    if (e is GuardrailsException) e.status.name else null
                                aiAuditLogDao.insertLog(
                                    AIAuditLog(
                                        model = model,
                                        blueprintKey = blueprintKey,
                                        dataType = T::class.java.simpleName,
                                        status = "ERROR",
                                        errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                                        responseTime = duration,
                                        safetyStatus = safetyStatus,
                                    ),
                                )
                            } catch (logEx: Exception) {
                                Timber.tag(this@GemmaClient::class.java.simpleName).e("Error saving log: ${logEx.message}")
                            }
                        }
                        if (logEnabled) {
                            Timber
                                .tag(
                                    this@GemmaClient::class.java.simpleName,
                                ).e(
                                    "Error in Generation($model) Attempt $currentAttempt/$maxAttempts: ${e.javaClass.simpleName} - ${e.message}",
                                )
                        }

                        // Check if it's a parsing error (no delay needed) or network error (delay recommended)
                        val isParsingError =
                            e is JsonSyntaxException || e is JsonParseException || e is IllegalArgumentException
                        var extractedDelay: Long? = null

                        if (e is HttpException) {
                            val errorBody = e.response()?.errorBody()?.string()
                            if (logEnabled) {
                                Timber.tag(this@GemmaClient::class.java.simpleName).e("HTTP Error ($model): $errorBody")
                            }

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
                                        Timber
                                            .tag(
                                                this@GemmaClient::class.java.simpleName,
                                            ).w(
                                                "Quota Violation: ${violation.quotaId} - ${violation.quotaMetric} (Value: ${violation.quotaValue})",
                                            )
                                    }
                                }

                                if (extractedDelay != null) {
                                    Timber
                                        .tag(
                                            this@GemmaClient::class.java.simpleName,
                                        ).i("Extracted precise delay from error: $extractedDelay seconds")
                                }
                            } catch (parseEx: Exception) {
                                Timber.tag(this@GemmaClient::class.java.simpleName).e("Failed to parse error body: ${parseEx.message}")
                            }
                        }

                        val isNetworkError = e is java.io.IOException
                        if (currentAttempt < maxAttempts) {
                            val delayToApply =
                                when {
                                    isParsingError -> 0L

                                    isNetworkError -> 2L

                                    // Quick retry for DNS/Network hiccups
                                    else -> (extractedDelay ?: RETRY_DELAY.toLong())
                                }

                            if (delayToApply > 0) {
                                Timber
                                    .tag(
                                        this@GemmaClient::class.java.simpleName,
                                    ).w("Retrying HIGH priority request in $delayToApply seconds due to ${e.javaClass.simpleName}...")
                                delay(delayToApply.seconds)
                            } else {
                                Timber
                                    .tag(
                                        this@GemmaClient::class.java.simpleName,
                                    ).w("Retrying immediately due to parsing error (${e.javaClass.simpleName})...")
                            }
                        } else {
                            // Final failure
                            if (!isParsingError) {
                                retryDelay =
                                    extractedDelay?.toInt() ?: retryDelay?.let {
                                        if (it > 30) it / 2 else it + it
                                    } ?: 2
                            }
                            Timber.e("Final failure after $maxAttempts attempts.")
                            Timber.e("generate: Failed prompt")
                            Timber.w(prompt)
                            return@withContext null
                        }
                    }
                }
                return@withContext null
            }

        /**
         * Streams the generation of T, emitting chunks of reasoning as they arrive,
         * and finally emitting Success with the data, or Error if it fails.
         */
        suspend inline fun <reified T> generateStreaming(
            prompt: String,
            references: List<ImageReference?> = emptyList(),
            temperatureRandomness: Float = .5f,
            requireTranslation: Boolean = true,
            describeOutput: Boolean = true,
            filterOutputFields: List<String> = emptyList(),
            useCore: Boolean = false,
            requirement: ModelRequirement = ModelRequirement.MEDIUM,
            blueprintKey: String? = null,
            userInteraction: Boolean = false,
            logEnabled: Boolean = true,
        ): Flow<StreamingState<T>> =
            flow {
                try {
                    if (userInteraction) {
                        val safetyStatus = safetyClient.checkSafety(prompt)
                        if (safetyStatus != SafeGuard.OK) {
                            throw GuardrailsException(safetyStatus)
                        }
                    }
                    if (lastTokenCount > (INPUT_TOKEN_LIMIT * REACTIVE_DELAY_THRESHOLD) && retryDelay == null) {
                        Timber.w("Applying reactive delay due to high token count in last request.")
                        retryDelay = 5
                        delay((retryDelay ?: 5).seconds)
                    }
                    val model = modelName(requirement)
                    if (!useCore) {
                        retryDelay?.let {
                            if (logEnabled) Timber.e("generateStreaming: Trying delay $retryDelay seconds to avoid rate limit.")
                            delay(it.seconds)
                        }
                    } else {
                        if (logEnabled) Timber.i("generateStreaming: Core calls don't require delay.")
                    }

                    val maxAttempts = MAX_RETRIES + 1
                    val startTime = System.currentTimeMillis()

                    for (currentAttempt in 1..maxAttempts) {
                        try {
                            val structure =
                                if (describeOutput) {
                                    val dataType = object : TypeToken<T>() {}.type
                                    val dataStructure =
                                        if (T::class == String::class) {
                                            "\"string\""
                                        } else if (dataType is ParameterizedType && dataType.rawType == GeneratedContent::class.java) {
                                            val innerType =
                                                dataType.actualTypeArguments[0] as Class<*>
                                            val innerStructure =
                                                toJsonMap(
                                                    innerType,
                                                    filteredFields = filterOutputFields,
                                                )
                                            toJsonMap(
                                                GeneratedContent::class.java,
                                                fieldCustomDescriptions = listOf("data" to innerStructure),
                                                filteredFields = filterOutputFields,
                                            )
                                        } else {
                                            toJsonMap(
                                                T::class.java,
                                                filteredFields = filterOutputFields,
                                            )
                                        }
                                    toJsonMap(
                                        AIGeneration::class.java,
                                        fieldCustomDescriptions = listOf("data" to dataStructure),
                                    )
                                } else {
                                    T::class.java.simpleName
                                }

                            val formattingRule =
                                "Respond using STRICTLY VALID JSON. Maintain escaping and UTF-8 encoding."

                            requirement.name.lowercase()
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
                                            temperature =
                                                if (requirement == ModelRequirement.TINY ||
                                                    requirement == ModelRequirement.LOW
                                                ) {
                                                    0.1f
                                                } else {
                                                    temperatureRandomness
                                                },
                                        ),
                                )

                            val formattedModel = model.replace("models/", "")

                            if (logEnabled) {
                                Timber.d(
                                    "Input JSON: ${
                                        geminiRequest.toJsonFormatExcludingFields(
                                            AI_EXCLUDED_FIELDS,
                                        )
                                    }",
                                )

                                Timber.d("Prompt requested:\n$fullPrompt")
                            }
                            val responseBody =
                                geminiApiService.streamGenerateContent(
                                    model = formattedModel,
                                    apiKey = apiConfig(useCore),
                                    request = geminiRequest,
                                )

                            val accumulatedText = StringBuilder()

                            responseBody.byteStream().bufferedReader().useLines { lines ->
                                for (line in lines) {
                                    var trimmed = line.trim()
                                    if (trimmed.isEmpty()) continue

                                    if (trimmed.startsWith("data:")) {
                                        trimmed = trimmed.removePrefix("data:").trim()
                                    }

                                    if (trimmed.isEmpty()) continue
                                    val jsonStr = trimmed

                                    try {
                                        if (logEnabled) {
                                            Timber.i("generateStreaming: Trying to parse $jsonStr")
                                        }
                                        val partialResponse =
                                            Gson().fromJson(
                                                jsonStr,
                                                GeminiResponse::class.java,
                                            )
                                        val partialPart =
                                            partialResponse.candidates
                                                ?.firstOrNull()
                                                ?.content
                                                ?.parts
                                                ?.firstOrNull()

                                        if (partialPart != null && partialPart.text != null) {
                                            accumulatedText.append(partialPart.text)
                                            emit(StreamingState.Reasoning(accumulatedText.toString()))
                                        }
                                    } catch (e: Exception) {
                                        Timber.w("Failed to parse stream chunk: $jsonStr => ${e.message}")
                                    }
                                }
                            }

                            val fullText = accumulatedText.toString()
                            val cleanedJsonString =
                                fullText.sanitizeAndExtractJsonString(AIGeneration::class.java)
                            val typeToken = object : TypeToken<AIGeneration<T>>() {}
                            if (cleanedJsonString.isEmpty()) {
                                error("Failed to parse JSON")
                            }
                            val aiGeneration =
                                Gson().fromJson<AIGeneration<T>>(cleanedJsonString, typeToken.type)

                            val duration = System.currentTimeMillis() - startTime
                            if (BuildConfig.DEBUG && logEnabled) {
                                aiAuditLogDao.insertLog(
                                    AIAuditLog(
                                        model = model,
                                        blueprintKey = blueprintKey,
                                        dataType = T::class.java.simpleName,
                                        status = "SUCCESS",
                                        reasoning = aiGeneration?.reasoning,
                                        rawResponse = fullText,
                                        responseTime = duration,
                                    ),
                                )
                                Timber.i("Generation Streaming Bench: $model took ${duration}ms")
                            }

                            Timber.d("generateStreaming: final state on streaming:\n${aiGeneration.toJsonFormat()}")
                            emit(StreamingState.Success(aiGeneration.data))
                            retryDelay = null
                            return@flow
                        } catch (e: Exception) {
                            if (logEnabled) {
                                Timber
                                    .tag(
                                        this@GemmaClient::class.java.simpleName,
                                    ).e(
                                        "Error in Stream Generation($model) Attempt $currentAttempt/$maxAttempts: ${e.javaClass.simpleName} - ${e.message}",
                                    )

                                e.printStackTrace()
                            }

                            val isParsingError =
                                e is JsonSyntaxException || e is JsonParseException || e is IllegalArgumentException
                            var extractedDelay: Long? = null

                            if (e is HttpException) {
                                val errorBody = e.response()?.errorBody()?.string()
                                try {
                                    val errorResponse =
                                        Gson().fromJson(errorBody, GeminiErrorResponse::class.java)
                                    val retryInfo =
                                        errorResponse.error?.details?.find {
                                            it.type ==
                                                "type.googleapis.com/google.rpc.RetryInfo"
                                        }
                                    extractedDelay =
                                        retryInfo
                                            ?.retryDelay
                                            ?.removeSuffix("s")
                                            ?.toDoubleOrNull()
                                            ?.toLong()
                                } catch (parseEx: Exception) {
                                }
                            }

                            val isNetworkError = e is java.io.IOException
                            if (currentAttempt < maxAttempts) {
                                val delayToApply =
                                    when {
                                        isParsingError -> 0L

                                        isNetworkError -> 2L

                                        // Quick retry for DNS/Network hiccups
                                        else -> (extractedDelay ?: RETRY_DELAY.toLong())
                                    }
                                if (delayToApply > 0) delay(delayToApply.seconds)
                            } else {
                                if (logEnabled && BuildConfig.DEBUG) {
                                    val duration = System.currentTimeMillis() - startTime
                                    val safetyStatus =
                                        if (e is GuardrailsException) e.status.name else null
                                    aiAuditLogDao.insertLog(
                                        AIAuditLog(
                                            model = model,
                                            blueprintKey = blueprintKey,
                                            dataType = T::class.java.simpleName,
                                            status = "ERROR",
                                            errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                                            responseTime = duration,
                                            safetyStatus = safetyStatus,
                                        ),
                                    )
                                }
                                if (!isParsingError) {
                                    retryDelay = extractedDelay?.toInt()
                                        ?: retryDelay?.let { if (it > 30) it / 2 else it + it } ?: 2
                                }
                                emit(StreamingState.Error(e.message ?: "Unknown error", e))
                                return@flow
                            }
                        }
                    }
                } catch (e: Exception) {
                    emit(StreamingState.Error(e.message ?: "Unknown error", e))
                }
            }.flowOn(Dispatchers.IO)

        suspend fun generateText(
            prompt: String,
            requirement: ModelRequirement = ModelRequirement.LOW,
            temperatureRandomness: Float = 0.5f,
            logEnabled: Boolean = true,
        ): String? {
            val model = modelName(requirement)
            val parts = listOf(GeminiPart(text = prompt))
            val geminiRequest =
                GeminiRequest(
                    contents = listOf(GeminiContent(parts = parts)),
                    generationConfig = GeminiGenerationConfig(temperature = temperatureRandomness),
                )
            return requestMutexes.getOrPut(model) { Mutex() }.withLock {
                try {
                    val response =
                        geminiApiService.generateContent(
                            model = model,
                            apiKey = apiConfig(false),
                            request = geminiRequest,
                        )
                    response.error?.let { throw Exception(it.message) }
                    response.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.text
                } catch (e: Exception) {
                    Timber.w("generateText failed: ${e.message}")
                    null
                }
            }
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
