package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import android.util.Base64
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.model.AIGeneration
import com.ilustris.sagai.core.ai.model.GeminiContent
import com.ilustris.sagai.core.ai.model.GeminiGenerationConfig
import com.ilustris.sagai.core.ai.model.GeminiInlineData
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.GeminiResponse
import com.ilustris.sagai.core.ai.model.GeminiThinkingConfig
import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.model.SafeGuard
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.SideEffect
import com.ilustris.sagai.core.data.isFlowCancellation
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.core.database.model.AIStats
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import com.ilustris.sagai.core.network.GeminiApiClient
import com.ilustris.sagai.core.network.GeminiApiCodec
import com.ilustris.sagai.core.network.GeminiHttpException
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.SideEffectService
import com.ilustris.sagai.core.utils.findJsonContent
import com.ilustris.sagai.core.utils.sanitizeAndExtractJsonString
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class GemmaClient
    @Inject
    constructor(
        remoteConfig: RemoteConfigService,
        val safetyClient: SafetyClient,
        val sideEffectService: SideEffectService,
        val geminiApiClient: GeminiApiClient,
        promptService: PromptService,
        @PublishedApi internal val aiAuditLogDao: AIAuditLogDao,
    ) : AIClient(remoteConfig, promptService) {
        @PublishedApi
        internal val requestMutexes = java.util.concurrent.ConcurrentHashMap<String, Mutex>()

        @Volatile
        var lastTokenCount: Int = 0

        companion object {
            const val CORE_FLAG = "SAGA_CORE"
            const val INPUT_TOKEN_LIMIT = 15000
            const val REACTIVE_DELAY_THRESHOLD = 0.7f
            const val MAX_RETRIES = 2

            /** Last AI generation failure (release debugging when [generate] returns null). */
            @Volatile
            var lastGenerateFailure: String? = null

            /** Fallback when the API omits RetryInfo on rate-limit responses. */
            const val DEFAULT_RATE_LIMIT_RETRY_SECONDS = 3L

            const val NETWORK_RETRY_SECONDS = 2L

            fun retryDelaySeconds(
                e: Exception,
                isParsingError: Boolean,
                extractedDelay: Long?,
            ): Long =
                when {
                    isParsingError -> 0L
                    extractedDelay != null -> extractedDelay
                    e is java.io.IOException -> NETWORK_RETRY_SECONDS
                    else -> DEFAULT_RATE_LIMIT_RETRY_SECONDS
                }

            fun recordGenerationFailure(
                dataType: String,
                model: String,
                attempt: Int,
                maxAttempts: Int,
                throwable: Throwable,
            ) {
                val summary = "${throwable.javaClass.simpleName}: ${throwable.message}"
                lastGenerateFailure = summary
                FirebaseCrashlytics.getInstance().apply {
                    log("GemmaClient.generate failure")
                    setCustomKey("ai_data_type", dataType)
                    setCustomKey("ai_model", model)
                    setCustomKey("ai_attempt", attempt)
                    setCustomKey("ai_max_attempts", maxAttempts)
                    recordException(throwable)
                }
            }
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
            requirement: ModelRequirement = ModelRequirement.MEDIUM,
            aiStats: AIStats? = null,
            blueprintKey: String? = null,
            systemInstructions: Map<String, Any> = emptyMap(),
            logEnabled: Boolean = true,
        ): T? =
            withContext(Dispatchers.IO) {
                checkSafety(userInteraction, prompt)
                val model = modelName(requirement)

                val maxAttempts = MAX_RETRIES + 1
                val startTime = System.currentTimeMillis()

                val (type, structure) =
                    buildDataStructure(
                        requirement,
                        describeOutput,
                        getJavaType<T>(),
                        filterOutputFields,
                    )
                val finalInstructions =
                    buildCoreInstructions(requirement, requireTranslation, type, structure)
                        .plus(systemInstructions)

                for (currentAttempt in 1..maxAttempts) {
                    try {
                        return@withContext requestMutexes.getOrPut(model) { Mutex() }.withLock {
                            val promptLength =
                                prompt.length +
                                    references.filterNotNull().sumOf {
                                        it.description.length
                                    }
                            if (logEnabled) Timber.i("Requesting $model\nPrompt with $promptLength chars.")

                            if (promptLength > (INPUT_TOKEN_LIMIT * 5)) {
                                throw IllegalArgumentException("Prompt is too long. verify your prompt and try again.")
                            }

                            val parts = mutableListOf<GeminiPart>()
                            parts.add(GeminiPart(text = prompt))

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
                                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = finalInstructions.toAINormalize()))),
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
                                            thinkingConfig =
                                                if (requirement == ModelRequirement.TINY ||
                                                    requirement == ModelRequirement.LOW
                                                ) {
                                                    GeminiThinkingConfig(
                                                        includeThoughts = false,
                                                        thinkingLevel = "LOW",
                                                    )
                                                } else {
                                                    null
                                                },
                                        ),
                                )

                            val formattedModel = model.replace("models/", "")
                            val response =
                                callGenerateContent(
                                    formattedModel,
                                    apiConfig(useCore),
                                    geminiRequest,
                                )

                            // Check for API error
                            response.error?.let { error ->
                                if (logEnabled) Timber.e("Gemini API error: ${error.code} - ${error.message}")
                                throw Exception("Gemini API error: ${error.message}")
                            }

                            val candidate = response.candidates?.firstOrNull()
                            if (candidate?.finishReason == "SAFETY" || candidate?.finishReason == "OTHER") {
                                if (logEnabled) Timber.w("API blocked content with reason: ${candidate.finishReason}")
                                throw GuardrailsException(SafeGuard.BLOCKED)
                            }

                            lastTokenCount = response.usageMetadata?.promptTokenCount ?: 0
                            if (lastTokenCount < (INPUT_TOKEN_LIMIT * REACTIVE_DELAY_THRESHOLD)) {
                                lastTokenCount = 0
                            }

                            val responseContent =
                                response.candidates
                                    ?.firstOrNull()
                                    ?.content
                                    ?.parts

                            // Use intelligent JSON locator that searches across all parts
                            val (requiredText, partIndex) = responseContent.findJsonContent()
                            val nativeThoughts =
                                responseContent
                                    ?.filter { it.thought == true }
                                    ?.joinToString("\n") { it.text.orEmpty() }

                            if (logEnabled) {
                                Timber.d("Request stats: \n${response.usageMetadata.toJsonFormat()}\n")

                                Timber.d("Instructions: \n${finalInstructions.toJsonFormat()}\n")

                                Timber.d("Prompt requested:\n$prompt\n")

                                if (partIndex >= 0) {
                                    Timber.i("JSON extracted from response part[$partIndex]")
                                }

                                Timber.i("API Response: ${response.toJsonFormat()}")

                                Timber.d(
                                    "Input JSON: ${
                                        geminiRequest.toJsonFormatExcludingFields(
                                            AI_EXCLUDED_FIELDS,
                                        )
                                    }",
                                )
                            }

                            val cleanedJsonString =
                                requiredText.sanitizeAndExtractJsonString(AIGeneration::class.java)
                            val aiGeneration =
                                parseAIGenerationFromJson<T>(Gson(), cleanedJsonString)
                            val duration = System.currentTimeMillis() - startTime
                            if (BuildConfig.DEBUG && logEnabled) {
                                aiAuditLogDao.insertLog(
                                    AIAuditLog(
                                        model = model,
                                        blueprintKey = aiStats?.blueprintKey ?: blueprintKey,
                                        dataType = type,
                                        status = "SUCCESS",
                                        reasoning = nativeThoughts,
                                        rawResponse = requiredText,
                                        responseTime = duration,
                                        systemInstruction = finalInstructions.toJsonFormat(),
                                        sentVariables = aiStats?.sentVariables.toJsonFormat(),
                                    ),
                                )
                                Timber.i("Generation Bench: $model took ${duration}ms (Prompt: $promptLength chars)")
                            }
                            val data = aiGeneration.data
                            if (logEnabled) Timber.d("AI data ->\n$data\n")
                            data
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        recordGenerationFailure(
                            dataType = this.javaClass.simpleName,
                            model = model,
                            attempt = currentAttempt,
                            maxAttempts = maxAttempts,
                            throwable = e,
                        )
                        if (BuildConfig.DEBUG && logEnabled) {
                            try {
                                val duration = System.currentTimeMillis() - startTime
                                val safetyStatus =
                                    if (e is GuardrailsException) e.status.name else null
                                aiAuditLogDao.insertLog(
                                    AIAuditLog(
                                        model = model,
                                        blueprintKey = aiStats?.blueprintKey ?: blueprintKey,
                                        dataType = type,
                                        status = "ERROR",
                                        errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                                        responseTime = duration,
                                        safetyStatus = safetyStatus,
                                        systemInstruction = finalInstructions.toJsonFormat(),
                                        sentVariables = aiStats?.sentVariables.toJsonFormat(),
                                    ),
                                )
                            } catch (logEx: Exception) {
                                Timber
                                    .tag(this@GemmaClient::class.java.simpleName)
                                    .e("Error saving log: ${logEx.message}")
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

                        if (e is GeminiHttpException) {
                            val errorBody = e.errorBody
                            if (logEnabled) {
                                Timber
                                    .tag(this@GemmaClient::class.java.simpleName)
                                    .e("HTTP Error ($model): $errorBody")
                            }

                            try {
                                val errorResponse =
                                    GeminiApiCodec.decodeErrorResponse(errorBody ?: "")
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
                                Timber
                                    .tag(this@GemmaClient::class.java.simpleName)
                                    .e("Failed to parse error body: ${parseEx.message}")
                            }
                        }

                        if (currentAttempt < maxAttempts) {
                            val delayToApply =
                                retryDelaySeconds(e, isParsingError, extractedDelay)

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
                            Timber.e("Final failure after $maxAttempts attempts.")
                            Timber.e("generate: Failed prompt")
                            Timber.w(prompt)
                            return@withContext null
                        }
                    }
                }
                return@withContext null
            }

        suspend inline fun <reified T> generate(
            promptSplit: SplitPrompt,
            userInteraction: Boolean = false,
            references: List<ImageReference?> = emptyList(),
            temperatureRandomness: Float = .5f,
            requireTranslation: Boolean = true,
            describeOutput: Boolean = true,
            filterOutputFields: List<String> = emptyList(),
            useCore: Boolean = false,
            requirement: ModelRequirement = ModelRequirement.MEDIUM,
            logEnabled: Boolean = true,
        ): T? =
            withContext(Dispatchers.IO) {
                val prompt = promptSplit.processedTemplate
                checkSafety(userInteraction, prompt)
                val model = modelName(requirement)

                val maxAttempts = MAX_RETRIES + 1
                val startTime = System.currentTimeMillis()

                val (dataTypeName, systemInstruction) =
                    buildStructure<T>(
                        describeOutput,
                        filterOutputFields,
                        requirement,
                        requireTranslation,
                        promptSplit.renderInstructions(),
                    )

                for (currentAttempt in 1..maxAttempts) {
                    try {
                        return@withContext requestMutexes.getOrPut(model) { Mutex() }.withLock {
                            val fullPrompt = prompt

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
                                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction))),
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
                                            thinkingConfig =
                                                if (requirement == ModelRequirement.TINY ||
                                                    requirement == ModelRequirement.LOW
                                                ) {
                                                    GeminiThinkingConfig(
                                                        includeThoughts = false,
                                                        thinkingLevel = "LOW",
                                                    )
                                                } else {
                                                    null
                                                },
                                        ),
                                )

                            val formattedModel = model.replace("models/", "")
                            val response =
                                callGenerateContent(
                                    formattedModel,
                                    apiConfig(useCore),
                                    geminiRequest,
                                )

                            // Check for API error
                            response.error?.let { error ->
                                if (logEnabled) Timber.e("Gemini API error: ${error.code} - ${error.message}")
                                throw Exception("Gemini API error: ${error.message}")
                            }

                            val candidate = response.candidates?.firstOrNull()
                            if (candidate?.finishReason == "SAFETY" || candidate?.finishReason == "OTHER") {
                                if (logEnabled) Timber.w("API blocked content with reason: ${candidate.finishReason}")
                                throw GuardrailsException(SafeGuard.BLOCKED)
                            }

                            lastTokenCount = response.usageMetadata?.promptTokenCount ?: 0
                            if (lastTokenCount < (INPUT_TOKEN_LIMIT * REACTIVE_DELAY_THRESHOLD)) {
                                lastTokenCount = 0
                            }

                            val responseContent =
                                response.candidates
                                    ?.firstOrNull()
                                    ?.content
                                    ?.parts

                            // Use intelligent JSON locator that searches across all parts
                            val (requiredText, partIndex) = responseContent.findJsonContent()
                            val nativeThoughts =
                                responseContent
                                    ?.filter { it.thought == true }
                                    ?.joinToString("\n") { it.text.orEmpty() }

                            if (logEnabled) {
                                Timber.d("Request stats: \n${response.usageMetadata.toJsonFormat()}\n")

                                Timber.d("Instructions: \n$systemInstruction\n")

                                Timber.d("Prompt requested:\n$fullPrompt\n")

                                if (partIndex >= 0) {
                                    Timber.i("JSON extracted from response part[$partIndex]")
                                }

                                Timber.i("API Response: ${response.toJsonFormat()}")

                                Timber.d(
                                    "Input JSON: ${
                                        geminiRequest.toJsonFormatExcludingFields(
                                            AI_EXCLUDED_FIELDS,
                                        )
                                    }",
                                )
                            }

                            val cleanedJsonString =
                                requiredText.sanitizeAndExtractJsonString(AIGeneration::class.java)
                            val aiGeneration =
                                parseAIGenerationFromJson<T>(Gson(), cleanedJsonString)
                            val duration = System.currentTimeMillis() - startTime
                            if (BuildConfig.DEBUG && logEnabled) {
                                aiAuditLogDao.insertLog(
                                    AIAuditLog(
                                        model = model,
                                        blueprintKey = promptSplit.blueprintKey,
                                        dataType = dataTypeName,
                                        status = "SUCCESS",
                                        reasoning = nativeThoughts,
                                        rawResponse = requiredText,
                                        responseTime = duration,
                                        systemInstruction = systemInstruction,
                                        sentVariables = promptSplit.sentVariables.toJsonFormat(),
                                    ),
                                )
                                Timber.i("Generation Bench: $model took ${duration}ms (Prompt: $promptLength chars)")
                            }
                            val data = aiGeneration.data
                            if (logEnabled) Timber.d("AI data ->\n$data\n")
                            data
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        recordGenerationFailure(
                            dataType = this.javaClass.simpleName,
                            model = model,
                            attempt = currentAttempt,
                            maxAttempts = maxAttempts,
                            throwable = e,
                        )
                        if (BuildConfig.DEBUG && logEnabled) {
                            try {
                                val duration = System.currentTimeMillis() - startTime
                                val safetyStatus =
                                    if (e is GuardrailsException) e.status.name else null
                                aiAuditLogDao.insertLog(
                                    AIAuditLog(
                                        model = model,
                                        blueprintKey = promptSplit.blueprintKey,
                                        dataType = this.javaClass.simpleName,
                                        status = "ERROR",
                                        errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                                        responseTime = duration,
                                        safetyStatus = safetyStatus,
                                        systemInstruction = systemInstruction,
                                        sentVariables = promptSplit.sentVariables.toJsonFormat(),
                                    ),
                                )
                            } catch (logEx: Exception) {
                                Timber
                                    .tag(this@GemmaClient::class.java.simpleName)
                                    .e("Error saving log: ${logEx.message}")
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

                        if (e is GeminiHttpException) {
                            val errorBody = e.errorBody
                            if (logEnabled) {
                                Timber
                                    .tag(this@GemmaClient::class.java.simpleName)
                                    .e("HTTP Error ($model): $errorBody")
                            }

                            try {
                                val errorResponse = GeminiApiCodec.decodeErrorResponse(errorBody ?: "")
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
                                Timber
                                    .tag(this@GemmaClient::class.java.simpleName)
                                    .e("Failed to parse error body: ${parseEx.message}")
                            }
                        }

                        if (currentAttempt < maxAttempts) {
                            val delayToApply =
                                retryDelaySeconds(e, isParsingError, extractedDelay)

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
                            Timber.e("Final failure after $maxAttempts attempts.")
                            Timber.e("generate: Failed prompt")
                            Timber.w(prompt)
                            return@withContext null
                        }
                    }
                }
                return@withContext null
            }

        suspend inline fun <reified T> buildStructure(
            describeOutput: Boolean,
            filterOutputFields: List<String>,
            requirement: ModelRequirement,
            requireTranslation: Boolean,
            systemInstructions: Map<String, Any>,
        ): Pair<String, String> {
            val dataType = getJavaType<T>()

            val (typeName, structure) =
                buildDataStructure(
                    requirement,
                    describeOutput,
                    dataType,
                    filterOutputFields,
                )

            val corePrompt = buildCorePrompt(requirement, requireTranslation, typeName, structure)

            val systemInstruction = buildInstructions(corePrompt, systemInstructions)
            return Pair(typeName, systemInstruction)
        }

        suspend fun checkSafety(
            userInteraction: Boolean,
            prompt: String,
        ) {
            if (userInteraction) {
                val safetyStatus = safetyClient.checkSafety(prompt)
                if (safetyStatus != SafeGuard.OK) {
                    throw GuardrailsException(safetyStatus)
                }
            }
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
            aiStats: AIStats? = null,
            systemInstructions: Map<String, Any> = emptyMap(),
        ): Flow<StreamingState<T>> =
            flow {
                try {
                    checkSafety(userInteraction, prompt)
                    val (dataTypeName, systemInstruction) =
                        buildStructure<T>(
                            describeOutput,
                            filterOutputFields,
                            requirement,
                            requireTranslation,
                            systemInstructions,
                        )

                    val model = modelName(requirement)

                    val maxAttempts = MAX_RETRIES + 1
                    val startTime = System.currentTimeMillis()

                    for (currentAttempt in 1..maxAttempts) {
                        try {
                            val fullPrompt = prompt

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
                                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction))),
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
                                            thinkingConfig =
                                                if (requirement == ModelRequirement.TINY ||
                                                    requirement == ModelRequirement.LOW
                                                ) {
                                                    GeminiThinkingConfig(
                                                        includeThoughts = false,
                                                        thinkingLevel = "LOW",
                                                    )
                                                } else {
                                                    null
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
                                callStreamGenerateContent(
                                    formattedModel,
                                    apiConfig(useCore),
                                    geminiRequest,
                                )

                            val accumulatedText = StringBuilder()
                            val accumulatedThoughts = StringBuilder()

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
                                        val partialResponse = GeminiApiCodec.decodeResponse(jsonStr)

                                        val candidate = partialResponse.candidates?.firstOrNull()
                                        if (candidate?.finishReason == "SAFETY" || candidate?.finishReason == "OTHER") {
                                            if (logEnabled) Timber.w("Streaming API blocked content with reason: ${candidate.finishReason}")
                                            throw GuardrailsException(SafeGuard.BLOCKED)
                                        }

                                        candidate?.content?.parts?.forEach { part ->
                                            if (part.text != null) {
                                                if (part.thought == true) {
                                                    accumulatedThoughts.append(part.text)
                                                    emit(
                                                        StreamingState.Reasoning(
                                                            accumulatedThoughts.toString(),
                                                        ),
                                                    )
                                                } else {
                                                    accumulatedText.append(part.text)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Timber.w("Failed to parse stream chunk: $jsonStr => ${e.message}")
                                    }
                                }
                            }

                            val fullText = accumulatedText.toString()
                            val fullThoughts = accumulatedThoughts.toString()
                            if (logEnabled) {
                                Timber.i("Streaming completed, accumulated text length: ${fullText.length}")
                            }
                            val cleanedJsonString =
                                fullText.sanitizeAndExtractJsonString(AIGeneration::class.java)
                            if (cleanedJsonString.isEmpty()) {
                                error("Failed to parse JSON")
                            }
                            val aiGeneration =
                                parseAIGenerationFromJson<T>(Gson(), cleanedJsonString)

                            val duration = System.currentTimeMillis() - startTime
                            if (BuildConfig.DEBUG && logEnabled) {
                                aiAuditLogDao.insertLog(
                                    AIAuditLog(
                                        model = model,
                                        blueprintKey = aiStats?.blueprintKey ?: blueprintKey,
                                        dataType = dataTypeName,
                                        status = "SUCCESS",
                                        reasoning = fullThoughts,
                                        rawResponse = fullText,
                                        responseTime = duration,
                                    ),
                                )
                                Timber.i("Generation Streaming Bench: $model took ${duration}ms")
                            }

                            Timber.d("generateStreaming: final state on streaming:\n${aiGeneration.toJsonFormat()}")
                            emit(StreamingState.Success(aiGeneration.data))
                            return@flow
                        } catch (e: Exception) {
                            if (e.isFlowCancellation()) {
                                throw e
                            }
                            recordGenerationFailure(
                                dataType = javaClass.simpleName,
                                model = model,
                                attempt = currentAttempt,
                                maxAttempts = maxAttempts,
                                throwable = e,
                            )
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

                            if (e is GuardrailsException) {
                                sideEffectService.emit(SideEffect.GuardrailBlock(e.status))
                            }

                            if (e is GeminiHttpException) {
                                val errorBody = e.errorBody
                                try {
                                    val errorResponse =
                                        GeminiApiCodec.decodeErrorResponse(errorBody ?: "")
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
                                    Timber.e(parseEx)
                                }
                            }

                            if (currentAttempt < maxAttempts) {
                                val delayToApply =
                                    retryDelaySeconds(e, isParsingError, extractedDelay)
                                if (delayToApply > 0) delay(delayToApply.seconds)
                            } else {
                                if (logEnabled && BuildConfig.DEBUG) {
                                    val duration = System.currentTimeMillis() - startTime
                                    val safetyStatus =
                                        if (e is GuardrailsException) e.status.name else null
                                    aiAuditLogDao.insertLog(
                                        AIAuditLog(
                                            model = model,
                                            blueprintKey = aiStats?.blueprintKey ?: blueprintKey,
                                            dataType = javaClass.simpleName,
                                            status = "ERROR",
                                            errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                                            responseTime = duration,
                                            safetyStatus = safetyStatus,
                                        ),
                                    )
                                }
                                emit(StreamingState.Error(e.message ?: "Unknown error", e))
                                return@flow
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (e.isFlowCancellation()) {
                        throw e
                    }
                    emit(StreamingState.Error(e.message ?: "Unknown error", e))
                }
            }.flowOn(Dispatchers.IO)

        suspend inline fun <reified T> generateStreaming(
            promptSplit: SplitPrompt,
            references: List<ImageReference?> = emptyList(),
            temperatureRandomness: Float = .5f,
            requireTranslation: Boolean = true,
            describeOutput: Boolean = true,
            filterOutputFields: List<String> = emptyList(),
            useCore: Boolean = false,
            requirement: ModelRequirement = ModelRequirement.MEDIUM,
            userInteraction: Boolean = false,
            logEnabled: Boolean = true,
        ): Flow<StreamingState<T>> =
            flow {
                try {
                    val prompt = promptSplit.processedTemplate
                    checkSafety(userInteraction, prompt)
                    val (dataTypeName, systemInstruction) =
                        buildStructure<T>(
                            describeOutput,
                            filterOutputFields,
                            requirement,
                            requireTranslation,
                            promptSplit.renderInstructions(),
                        )

                    val model = modelName(requirement)

                    val maxAttempts = MAX_RETRIES + 1
                    val startTime = System.currentTimeMillis()

                    for (currentAttempt in 1..maxAttempts) {
                        try {
                            val fullPrompt = prompt

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
                                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction))),
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
                                            thinkingConfig =
                                                if (requirement == ModelRequirement.TINY ||
                                                    requirement == ModelRequirement.LOW
                                                ) {
                                                    GeminiThinkingConfig(
                                                        includeThoughts = false,
                                                        thinkingLevel = "LOW",
                                                    )
                                                } else {
                                                    null
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
                                callStreamGenerateContent(
                                    formattedModel,
                                    apiConfig(useCore),
                                    geminiRequest,
                                )

                            val accumulatedText = StringBuilder()
                            val accumulatedThoughts = StringBuilder()

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
                                        val partialResponse = GeminiApiCodec.decodeResponse(jsonStr)

                                        val candidate = partialResponse.candidates?.firstOrNull()
                                        if (candidate?.finishReason == "SAFETY" || candidate?.finishReason == "OTHER") {
                                            if (logEnabled) Timber.w("Streaming API blocked content with reason: ${candidate.finishReason}")
                                            throw GuardrailsException(SafeGuard.BLOCKED)
                                        }

                                        candidate?.content?.parts?.forEach { part ->
                                            if (part.text != null) {
                                                if (part.thought == true) {
                                                    accumulatedThoughts.append(part.text)
                                                    emit(
                                                        StreamingState.Reasoning(
                                                            accumulatedThoughts.toString(),
                                                        ),
                                                    )
                                                } else {
                                                    accumulatedText.append(part.text)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Timber.w("Failed to parse stream chunk: $jsonStr => ${e.message}")
                                    }
                                }
                            }

                            val fullText = accumulatedText.toString()
                            val fullThoughts = accumulatedThoughts.toString()
                            if (logEnabled) {
                                Timber.i("Streaming completed, accumulated text length: ${fullText.length}")
                            }
                            val cleanedJsonString =
                                fullText.sanitizeAndExtractJsonString(AIGeneration::class.java)
                            if (cleanedJsonString.isEmpty()) {
                                error("Failed to parse JSON")
                            }
                            val aiGeneration =
                                parseAIGenerationFromJson<T>(Gson(), cleanedJsonString)

                            val duration = System.currentTimeMillis() - startTime
                            if (BuildConfig.DEBUG && logEnabled) {
                                aiAuditLogDao.insertLog(
                                    AIAuditLog(
                                        model = model,
                                        blueprintKey = promptSplit.blueprintKey,
                                        dataType = dataTypeName,
                                        status = "SUCCESS",
                                        reasoning = fullThoughts,
                                        rawResponse = fullText,
                                        responseTime = duration,
                                        sentVariables = promptSplit.sentVariables.toJsonFormat(),
                                    ),
                                )
                                Timber.i("Generation Streaming Bench: $model took ${duration}ms")
                            }

                            Timber.d("generateStreaming: final state on streaming:\n${aiGeneration.toJsonFormat()}")
                            emit(StreamingState.Success(aiGeneration.data))
                            return@flow
                        } catch (e: Exception) {
                            if (e.isFlowCancellation()) {
                                throw e
                            }
                            recordGenerationFailure(
                                dataType = javaClass.simpleName,
                                model = model,
                                attempt = currentAttempt,
                                maxAttempts = maxAttempts,
                                throwable = e,
                            )
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

                            if (e is GuardrailsException) {
                                sideEffectService.emit(SideEffect.GuardrailBlock(e.status))
                            }

                            if (e is GeminiHttpException) {
                                val errorBody = e.errorBody
                                try {
                                    val errorResponse =
                                        GeminiApiCodec.decodeErrorResponse(errorBody ?: "")
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
                                    Timber.e(parseEx)
                                }
                            }

                            if (currentAttempt < maxAttempts) {
                                val delayToApply =
                                    retryDelaySeconds(e, isParsingError, extractedDelay)
                                if (delayToApply > 0) delay(delayToApply.seconds)
                            } else {
                                if (logEnabled && BuildConfig.DEBUG) {
                                    val duration = System.currentTimeMillis() - startTime
                                    val safetyStatus =
                                        if (e is GuardrailsException) e.status.name else null
                                    aiAuditLogDao.insertLog(
                                        AIAuditLog(
                                            model = model,
                                            blueprintKey = promptSplit.blueprintKey,
                                            dataType = javaClass.simpleName,
                                            status = "ERROR",
                                            errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                                            responseTime = duration,
                                            safetyStatus = safetyStatus,
                                            sentVariables = promptSplit.sentVariables.toJsonFormat(),
                                        ),
                                    )
                                }
                                emit(StreamingState.Error(e.message ?: "Unknown error", e))
                                return@flow
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (e.isFlowCancellation()) {
                        throw e
                    }
                    emit(StreamingState.Error(e.message ?: "Unknown error", e))
                }
            }.flowOn(Dispatchers.IO)

        @PublishedApi
        internal suspend fun callGenerateContent(
            model: String,
            apiKey: String,
            request: GeminiRequest,
        ): GeminiResponse = geminiApiClient.generateContent(model, apiKey, request)

        @PublishedApi
        internal suspend fun callStreamGenerateContent(
            model: String,
            apiKey: String,
            request: GeminiRequest,
        ) = geminiApiClient.streamGenerateContent(model, apiKey, request)

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

fun buildInstructions(
    corePrompt: SplitPrompt,
    systemInstructions: Map<String, Any>,
): String =
    buildMap {
        putAll(corePrompt.renderInstructions().plus("task" to corePrompt.processedTemplate))
        putAll(systemInstructions)
    }.toAINormalize()

const val KEY_FLAG = "FIREBASE_KEY"
