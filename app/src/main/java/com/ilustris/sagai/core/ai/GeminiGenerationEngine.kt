package com.ilustris.sagai.core.ai

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.ilustris.sagai.core.ai.key.ApiKeyDiagnosis
import com.ilustris.sagai.core.ai.key.classifyApiKeyFailure
import com.ilustris.sagai.core.ai.model.AIGeneration
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.GeminiResponse
import com.ilustris.sagai.core.ai.model.GeminiUsageMetadata
import com.ilustris.sagai.core.ai.model.SafeGuard
import com.ilustris.sagai.core.data.isFlowCancellation
import com.ilustris.sagai.core.network.GeminiApiCodec
import com.ilustris.sagai.core.network.GeminiHttpException
import com.ilustris.sagai.core.utils.findJsonContent
import com.ilustris.sagai.core.utils.sanitizeAndExtractJsonString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.ResponseBody
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

object GeminiGenerationPolicy {
    const val REACTIVE_DELAY_THRESHOLD = 0.7f
    const val DEFAULT_RATE_LIMIT_RETRY_SECONDS = 3L
    const val NETWORK_RETRY_SECONDS = 2L

    /** Last AI generation failure (release debugging when sync generation returns null). */
    @Volatile
    var lastGenerateFailure: String? = null

    fun maxAttempts(requirement: ModelRequirement): Int =
        when (requirement) {
            ModelRequirement.MINIMAL, ModelRequirement.LOW -> 1
            ModelRequirement.MEDIUM -> 2
            ModelRequirement.HIGH -> 3
        }

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
        blueprintKey: String? = null,
    ) {
        val summary = "${throwable.javaClass.simpleName}: ${throwable.message}"
        lastGenerateFailure = summary
        FirebaseCrashlytics.getInstance().apply {
            log("GemmaClient.generate failure")
            setCustomKey("ai_data_type", dataType)
            setCustomKey("ai_model", model)
            setCustomKey("ai_attempt", attempt)
            setCustomKey("ai_max_attempts", maxAttempts)
            // An oversized prompt is a bug in a specific blueprint. Without this the report says
            // only that something was too big, which is not something you can go and fix.
            setCustomKey("ai_blueprint_key", blueprintKey ?: "unknown")
            recordException(throwable)
        }
    }
}

@PublishedApi
internal inline fun <reified T> parseGenerationJson(
    rawText: String,
    nativeThoughts: String? = null,
    usageMetadata: GeminiUsageMetadata? = null,
): GeminiParsedGeneration<T> {
    val cleanedJsonString =
        rawText.sanitizeAndExtractJsonString(AIGeneration::class.java)
    if (cleanedJsonString.isEmpty()) {
        error("Failed to parse JSON")
    }
    val aiGeneration = parseAIGenerationFromJson<T>(Gson(), cleanedJsonString)
    aiGeneration.error?.let { generationError ->
        generationError.type?.let { status -> throw GuardrailsException(status) }
    }
    return GeminiParsedGeneration(
        data = aiGeneration.data,
        rawResponseText = rawText,
        nativeThoughts = nativeThoughts,
        usageMetadata = usageMetadata,
    )
}

@PublishedApi
internal suspend inline fun <reified T> GeminiAIClient.executeSyncGenerationWithRetry(params: GeminiSyncGenerationParams): T? {
    val maxAttempts = GeminiGenerationPolicy.maxAttempts(params.requirement)
    for (currentAttempt in 1..maxAttempts) {
        var queueWaitMs = 0L
        var inferenceMs = 0L
        var lastRequestParts = emptyList<GeminiPart>()
        var fullPromptText = ""
        try {
            val queueStartTime = System.currentTimeMillis()
            return requestMutexes.getOrPut(params.model) { Mutex() }.withLock {
                queueWaitMs = System.currentTimeMillis() - queueStartTime
                val assembly = buildGenerationAssembly(params)
                fullPromptText = assembly.fullPromptText
                lastRequestParts = assembly.contentParts
                val geminiRequest = assembly.request
                val formattedModel = params.model.replace("models/", "")

                ensurePromptWithinTokenLimit(
                    model = formattedModel,
                    apiKey = apiConfig(params.useCore),
                    request = geminiRequest,
                    parts = lastRequestParts,
                    fullPromptText = fullPromptText,
                    systemInstruction = params.systemInstruction,
                    blueprintKey = params.audit.blueprintKey,
                )

                logGenerateRequest(
                    logEnabled = params.logEnabled,
                    model = params.model,
                    fullPromptTextLength = fullPromptText.length,
                    instructions = params.systemInstruction,
                    prompt = params.taskPrompt,
                )

                val inferenceStart = System.currentTimeMillis()
                val response =
                    callGenerateContent(
                        formattedModel,
                        apiConfig(params.useCore),
                        geminiRequest,
                    )
                inferenceMs = System.currentTimeMillis() - inferenceStart

                val parsed =
                    parseSyncGenerationResponse<T>(
                        response = response,
                        geminiRequest = geminiRequest,
                        fullPromptText = fullPromptText,
                        logEnabled = params.logEnabled,
                        model = params.model,
                        blueprintKey = params.audit.blueprintKey,
                    )

                apiUsageTracker.record(params.model, parsed.usageMetadata)

                recordAudit(
                    AIAuditSnapshot.success(
                        model = params.model,
                        blueprintKey = params.audit.blueprintKey,
                        dataType = params.audit.dataType,
                        reasoning = parsed.nativeThoughts,
                        rawResponse = parsed.rawResponseText,
                        responseTimeMs = inferenceMs,
                        queueWaitMs = queueWaitMs,
                        systemInstruction = params.audit.systemInstruction,
                        sentVariables = params.audit.sentVariables,
                        usageMetadata = parsed.usageMetadata,
                    ),
                    logEnabled = params.logEnabled,
                )
                if (params.logEnabled) {
                    Timber.i(
                        "Generation Bench: ${params.model} took ${inferenceMs}ms" +
                            (if (queueWaitMs > 0) " (queue: ${queueWaitMs}ms)" else "") +
                            " (Prompt: ${fullPromptText.length} chars)",
                    )
                    Timber.d("AI data ->\n${parsed.data}\n")
                }
                // A request that got through proves the throttle lifted, whatever the backoff
                // last published.
                quotaStatusService.reportRecovered()
                parsed.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
            classifyPromptLimitFailure(
                e,
                fullPromptText,
                lastTokenLimit,
            )?.let { throw it }

            GeminiGenerationPolicy.recordGenerationFailure(
                dataType = params.audit.dataType,
                model = params.model,
                attempt = currentAttempt,
                maxAttempts = maxAttempts,
                throwable = e,
                blueprintKey = params.audit.blueprintKey,
            )
            recordAudit(
                AIAuditSnapshot.error(
                    model = params.model,
                    blueprintKey = params.audit.blueprintKey,
                    dataType = params.audit.dataType,
                    errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                    responseTimeMs = inferenceMs,
                    queueWaitMs = queueWaitMs,
                    safetyStatus = if (e is GuardrailsException) e.status.name else null,
                    systemInstruction = params.audit.systemInstruction,
                    sentVariables = params.audit.sentVariables,
                ),
                logEnabled = params.logEnabled,
            )

            val shouldRetry =
                handleGenerationRetry(
                    throwable = e,
                    currentAttempt = currentAttempt,
                    maxAttempts = maxAttempts,
                    model = params.model,
                    logEnabled = params.logEnabled,
                    lastRequestParts = lastRequestParts,
                    systemInstruction = params.systemInstruction,
                    context = "generate",
                    blueprintKey = params.audit.blueprintKey,
                )
            if (!shouldRetry) {
                if (params.logEnabled) {
                    Timber.e("Final failure after $maxAttempts attempts.")
                    Timber.e("generate: Failed prompt")
                    Timber.w(params.promptForFailureLog)
                }
                return null
            }
        }
    }
    return null
}

@PublishedApi
internal inline fun <reified T> GeminiAIClient.streamingGenerationFlow(params: GeminiStreamingGenerationParams): Flow<StreamingState<T?>> {
    val lastRequestParts = mutableListOf<GeminiPart>()
    val lastFullPromptText = StringBuilder()
    return flow {
        val maxAttempts = GeminiGenerationPolicy.maxAttempts(params.requirement)
        val startTime = System.currentTimeMillis()

        // Same per-model lock the sync path takes. Streaming ran outside it, so a reply could go
        // out while a sync generation on the same model was already in flight — and both spend
        // from one per-minute input-token quota. Held across the whole stream on purpose: the
        // budget is only free again once the response is fully drained.
        // Safe against the reasoning synthesizer that runs during collection: it launches in its
        // own coroutine and asks for a different tier's model, so it never waits on this lock in a
        // way that could hold the stream up.
        requestMutexes.getOrPut(params.model) { Mutex() }.withLock {
            for (currentAttempt in 1..maxAttempts) {
                try {
                    val assembly = buildGenerationAssembly(params)
                    val fullPromptText = assembly.fullPromptText
                    lastFullPromptText.clear()
                    lastFullPromptText.append(fullPromptText)
                    lastRequestParts.clear()
                    lastRequestParts.addAll(assembly.contentParts)
                    val geminiRequest = assembly.request
                    val formattedModel = params.model.replace("models/", "")

                    ensurePromptWithinTokenLimit(
                        model = formattedModel,
                        apiKey = apiConfig(params.useCore),
                        request = geminiRequest,
                        parts = lastRequestParts,
                        fullPromptText = fullPromptText,
                        systemInstruction = params.systemInstruction,
                        blueprintKey = params.audit.blueprintKey,
                    )

                    logGenerateRequest(
                        logEnabled = params.logEnabled,
                        model = params.model,
                        fullPromptTextLength = fullPromptText.length,
                        instructions = params.systemInstruction,
                        prompt = params.taskPrompt,
                    )

                    val responseBody =
                        callStreamGenerateContent(
                            formattedModel,
                            apiConfig(params.useCore),
                            geminiRequest,
                        )

                    val streamResult =
                        consumeStreamingResponse(
                            responseBody = responseBody,
                            fullPromptText = fullPromptText,
                            logEnabled = params.logEnabled,
                            geminiRequest = geminiRequest,
                            model = params.model,
                            blueprintKey = params.audit.blueprintKey,
                            onReasoning = { chunk -> emit(StreamingState.Reasoning(chunk)) },
                        )

                    val parsed =
                        parseGenerationJson<T>(
                            streamResult.fullText,
                            streamResult.fullThoughts,
                            streamResult.usageMetadata,
                        )

                    val duration = System.currentTimeMillis() - startTime
                    recordAudit(
                        AIAuditSnapshot.success(
                            model = params.model,
                            blueprintKey = params.audit.blueprintKey,
                            dataType = params.audit.dataType,
                            reasoning = streamResult.fullThoughts,
                            rawResponse = streamResult.fullText,
                            responseTimeMs = duration,
                            systemInstruction = params.audit.systemInstruction,
                            sentVariables = params.audit.sentVariables,
                            usageMetadata = streamResult.usageMetadata,
                        ),
                        logEnabled = params.logEnabled,
                    )
                    if (params.logEnabled) {
                        Timber.i("Generation Streaming Bench: ${params.model} took ${duration}ms")
                        Timber.d(
                            "generateStreaming: final state on streaming:\n${streamResult.fullText}",
                        )
                    }
                    emit(StreamingState.Success(parsed.data))
                    return@flow
                } catch (e: Exception) {
                    if (e.isFlowCancellation() || e is CancellationException) throw e
                    classifyPromptLimitFailure(
                        e,
                        lastFullPromptText.toString(),
                        lastTokenLimit,
                    )?.let { throw it }

                    GeminiGenerationPolicy.recordGenerationFailure(
                        dataType = params.audit.dataType,
                        model = params.model,
                        attempt = currentAttempt,
                        maxAttempts = maxAttempts,
                        throwable = e,
                    )

                    if (e is GuardrailsException) {
                        params.onGuardrailBlock?.invoke(e)
                    }

                    val shouldRetry =
                        handleGenerationRetry(
                            throwable = e,
                            currentAttempt = currentAttempt,
                            maxAttempts = maxAttempts,
                            model = params.model,
                            logEnabled = params.logEnabled,
                            lastRequestParts = lastRequestParts,
                            systemInstruction = params.systemInstruction,
                            context = "generateStreaming",
                            blueprintKey = params.audit.blueprintKey,
                        )
                    if (!shouldRetry) {
                        val duration = System.currentTimeMillis() - startTime
                        recordAudit(
                            AIAuditSnapshot.error(
                                model = params.model,
                                blueprintKey = params.audit.blueprintKey,
                                dataType = params.audit.dataType,
                                errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                                responseTimeMs = duration,
                                safetyStatus = if (e is GuardrailsException) e.status.name else null,
                                systemInstruction = params.audit.systemInstruction,
                                sentVariables = params.audit.sentVariables,
                            ),
                            logEnabled = params.logEnabled,
                        )
                        throw e
                    }
                }
            }
        }
    }.catch { e ->
        if (e.isFlowCancellation() || e is CancellationException) throw e

        val resolved =
            classifyPromptLimitFailure(
                e,
                lastFullPromptText.toString(),
                lastTokenLimit,
            ) ?: e
        logEstimatedPromptTokensOnFailure(
            parts = lastRequestParts,
            tokenLimit = lastTokenLimit,
            context = "generateStreaming",
            logEnabled = params.logEnabled,
            cause = resolved,
            systemInstruction = null,
        )
        emit(
            StreamingState.Error(
                message =
                    appendEstimatedTokenDiagnostics(
                        message = resolved.message ?: "Unknown error",
                        parts = lastRequestParts,
                        tokenLimit = lastTokenLimit,
                    ),
                throwable = resolved,
            ),
        )
    }
}

/**
 * The thinking level this request should actually carry.
 *
 * Resolved at assembly time rather than trusting the level frozen into the params, because the
 * params are fixed before the retry loop starts — so a `minimal` rejection recorded mid-flight
 * would otherwise be re-sent verbatim on every remaining attempt and fail identically.
 */
@PublishedApi
internal fun GeminiAIClient.effectiveThinkingLevel(
    model: String,
    requested: String?,
): String? =
    if (requested == "minimal" && !modelCatalog.supportsMinimalThinking(model)) {
        "low"
    } else {
        requested
    }

@PublishedApi
internal fun GeminiAIClient.buildGenerationAssembly(params: GeminiSyncGenerationParams): GeminiRequestAssembly =
    assembleGeminiRequest {
        task(params.taskPrompt)
        system(params.systemInstruction)
        references(params.references)
        generation(params.requirement, params.temperatureRandomness)
        thinking(effectiveThinkingLevel(params.model, params.thinkingLevel))
        if (!params.includeSystemInFullPrompt) {
            fullPrompt(includeSystemInstruction = false)
        }
    }

@PublishedApi
internal fun GeminiAIClient.buildGenerationAssembly(params: GeminiStreamingGenerationParams): GeminiRequestAssembly =
    assembleGeminiRequest {
        task(params.taskPrompt)
        system(params.systemInstruction)
        references(params.references)
        generation(params.requirement, params.temperatureRandomness)
        thinking(effectiveThinkingLevel(params.model, params.thinkingLevel))
        if (!params.includeSystemInFullPrompt) {
            fullPrompt(includeSystemInstruction = false)
        }
    }

@PublishedApi
internal inline fun <reified T> GeminiAIClient.parseSyncGenerationResponse(
    response: GeminiResponse,
    geminiRequest: GeminiRequest,
    fullPromptText: String,
    logEnabled: Boolean,
    model: String = "unknown",
    blueprintKey: String? = null,
): GeminiParsedGeneration<T> {
    response.error?.let { error ->
        if (logEnabled) Timber.e("Gemini API error: ${error.code} - ${error.message}")
        throwIfApiInputTokenLimitError(error, fullPromptText)
        throw Exception("Gemini API error: ${error.message}")
    }

    val candidate = response.candidates?.firstOrNull()
    if (candidate?.finishReason == "SAFETY" || candidate?.finishReason == "OTHER") {
        if (logEnabled) Timber.w("API blocked content with reason: ${candidate.finishReason}")
        throw GuardrailsException(SafeGuard.BLOCKED)
    }
    if (candidate?.finishReason == "MAX_TOKENS") {
        // Named rather than left to the JSON sanitizer, which would either fail to parse or hand
        // back a partial object — the second being indistinguishable from the model simply
        // answering briefly.
        throw ResponseTruncatedException(
            model = model,
            blueprintKey = blueprintKey,
            outputTokens = response.usageMetadata?.candidatesTokenCount,
        )
    }

    updateReactiveTokenCount(response.usageMetadata?.promptTokenCount ?: 0, lastTokenLimit)

    val responseContent =
        response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts

    val (requiredText, partIndex) =
        responseContent
            ?.filter { it.thought != true }
            .findJsonContent()
    val nativeThoughts =
        responseContent
            ?.filter { it.thought == true }
            ?.joinToString("\n") { it.text.orEmpty() }

    logGenerateResponse(
        logEnabled = logEnabled,
        response = response,
        geminiRequest = geminiRequest,
        partIndex = partIndex,
    )

    return parseGenerationJson<T>(requiredText.orEmpty(), nativeThoughts, response.usageMetadata)
}

@PublishedApi
internal suspend fun GeminiAIClient.consumeStreamingResponse(
    responseBody: ResponseBody,
    fullPromptText: String,
    logEnabled: Boolean,
    geminiRequest: GeminiRequest,
    model: String = "unknown",
    blueprintKey: String? = null,
    onReasoning: suspend (String) -> Unit,
): StreamingAccumulationResult {
    val accumulatedText = StringBuilder()
    val accumulatedThoughts = StringBuilder()
    var usageMetadata: GeminiUsageMetadata? = null

    responseBody.byteStream().bufferedReader().useLines { lines ->
        for (line in lines) {
            var trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            if (trimmed.startsWith("data:")) {
                trimmed = trimmed.removePrefix("data:").trim()
            }
            if (trimmed.isEmpty()) continue

            val partialResponse = GeminiApiCodec.decodeResponse(trimmed)
            partialResponse.error?.let { streamError ->
                throwIfApiInputTokenLimitError(streamError, fullPromptText)
                throw GeminiHttpException(streamError.code ?: 400, trimmed)
            }

            if (partialResponse.usageMetadata != null) {
                usageMetadata = partialResponse.usageMetadata
            }

            val candidate = partialResponse.candidates?.firstOrNull()
            if (candidate?.finishReason == "MAX_TOKENS") {
                throw ResponseTruncatedException(
                    model = model,
                    blueprintKey = blueprintKey,
                    outputTokens = partialResponse.usageMetadata?.candidatesTokenCount,
                )
            }
            if (candidate?.finishReason == "SAFETY" || candidate?.finishReason == "OTHER") {
                if (logEnabled) {
                    Timber.w(
                        "Streaming API blocked content with reason: ${candidate.finishReason}",
                    )
                }
                throw GuardrailsException(SafeGuard.BLOCKED)
            }

            candidate?.content?.parts?.forEach { part ->
                if (part.text != null) {
                    if (part.thought == true) {
                        accumulatedThoughts.append(part.text)
                        onReasoning(accumulatedThoughts.toString())
                    } else {
                        accumulatedText.append(part.text)
                    }
                }
            }
        }
    }

    apiUsageTracker.record(model, usageMetadata)

    val fullText = accumulatedText.toString()
    val fullThoughts = accumulatedThoughts.toString()

    logGenerateStreamingResponse(
        logEnabled = logEnabled,
        usageMetadata = usageMetadata,
        fullText = fullText,
        fullThoughts = fullThoughts,
        geminiRequest = geminiRequest,
    )

    return StreamingAccumulationResult(
        fullText = fullText,
        fullThoughts = fullThoughts,
        usageMetadata = usageMetadata,
    )
}

@PublishedApi
internal suspend fun GeminiAIClient.handleGenerationRetry(
    throwable: Exception,
    currentAttempt: Int,
    maxAttempts: Int,
    model: String,
    logEnabled: Boolean,
    lastRequestParts: List<GeminiPart>,
    systemInstruction: String?,
    context: String,
    blueprintKey: String? = null,
): Boolean {
    if (logEnabled) {
        Timber
            .tag(javaClass.simpleName)
            .e(
                "Error in Generation($model) Attempt $currentAttempt/$maxAttempts: " +
                    "${throwable.javaClass.simpleName} - ${throwable.message}",
            )
    }

    // Refusals raised before the request ever left: retrying re-raises them identically.
    if (throwable is QuotaExhaustedException || throwable is MissingApiKeyException) return false

    // The model refuses `minimal`. Recording it is what makes the retry different from the
    // attempt that just failed: the assembly is rebuilt each pass and resolves the level through
    // effectiveThinkingLevel, which now returns `low` for this model — here and on every later
    // request, so the cost is one wasted attempt, once per model.
    if (isUnsupportedThinkingLevel(throwable)) {
        modelCatalog.markMinimalThinkingUnsupported(model)
        return currentAttempt < maxAttempts
    }

    // Key verdicts come first, before any backoff maths. A rejected key fails every attempt
    // identically, so the three-attempt policy would just triple the damage; and a spent daily
    // quota cannot recover before midnight Pacific, which no retry delay is going to outwait.
    when (val diagnosis = classifyApiKeyFailure(throwable)) {
        is ApiKeyDiagnosis.Rejected -> {
            userApiKeyStore.markInvalid(diagnosis.failure)
            if (logEnabled) {
                Timber
                    .tag(javaClass.simpleName)
                    .e("API key rejected (${diagnosis.failure.name}) — not retrying.")
            }
            return false
        }

        ApiKeyDiagnosis.QuotaDaily -> {
            quotaStatusService.reportDailyExhausted(model)
            return false
        }

        is ApiKeyDiagnosis.QuotaMinute -> {
            // A prompt bigger than the whole per-minute token budget never fits, however long we
            // wait — burning the retry budget on it would report a permanent problem as a
            // temporary one. Raise it as the blueprint bug it is instead.
            val budget = diagnosis.tokenQuotaValue
            if (budget != null) modelCatalog.recordPerMinuteTokenBudget(model, budget)
            if (budget != null && lastPromptTokenCount > budget) {
                throw PromptTooLargeException(
                    message =
                        "Prompt of $lastPromptTokenCount tokens exceeds the per-minute budget of " +
                            "$budget for $model — no retry delay can satisfy it. " +
                            "Blueprint: ${blueprintKey ?: "unknown"}",
                    tokenCount = lastPromptTokenCount,
                    tokenLimit = budget,
                    fullPrompt = "",
                    blueprintKey = blueprintKey,
                    cause = throwable,
                )
            }
            // Otherwise transient: the request stays alive behind the existing backoff. Publishing
            // the window only replaces a mute spinner with a countdown the user can read.
            quotaStatusService.reportCoolingDown(
                model = model,
                retryDelaySeconds =
                    diagnosis.retryDelaySeconds
                        ?: GeminiGenerationPolicy.DEFAULT_RATE_LIMIT_RETRY_SECONDS,
            )
        }

        null -> Unit
    }

    val isParsingError =
        throwable is JsonSyntaxException ||
            throwable is JsonParseException ||
            throwable is IllegalArgumentException
    var extractedDelay: Long? = extractRetryDelayFromException(throwable)

    if (throwable is GeminiHttpException) {
        extractedDelay =
            resolveHttpRetryDelay(throwable, model, logEnabled) ?: extractedDelay
    }

    if (currentAttempt >= maxAttempts) return false

    logEstimatedPromptTokensOnFailure(
        parts = lastRequestParts,
        tokenLimit = lastTokenLimit,
        context = context,
        logEnabled = logEnabled,
        cause = throwable,
        systemInstruction = systemInstruction,
    )

    val delayToApply =
        GeminiGenerationPolicy.retryDelaySeconds(throwable, isParsingError, extractedDelay)
    if (delayToApply > 0) {
        if (logEnabled) {
            Timber
                .tag(javaClass.simpleName)
                .w(
                    "Retrying request in $delayToApply seconds due to ${throwable.javaClass.simpleName}...",
                )
        }
        delay(delayToApply.seconds)
        quotaStatusService.reportRecovered()
    } else if (logEnabled) {
        Timber
            .tag(javaClass.simpleName)
            .w("Retrying immediately due to parsing error (${throwable.javaClass.simpleName})...")
    }
    return true
}

@PublishedApi
internal fun GeminiAIClient.resolveHttpRetryDelay(
    exception: GeminiHttpException,
    model: String,
    logEnabled: Boolean,
): Long? {
    val errorBody = exception.errorBody
    if (logEnabled) {
        Timber.tag(javaClass.simpleName).e("HTTP Error ($model): $errorBody")
    }

    return try {
        val errorResponse = GeminiApiCodec.decodeErrorResponse(errorBody ?: "")
        val extractedDelay =
            errorResponse.error
                ?.details
                ?.find { it.type == "type.googleapis.com/google.rpc.RetryInfo" }
                ?.retryDelay
                ?.removeSuffix("s")
                ?.toDoubleOrNull()
                ?.toLong()

        errorResponse.error?.details?.forEach { detail ->
            detail.violations?.forEach { violation ->
                Timber
                    .tag(javaClass.simpleName)
                    .w(
                        "Quota Violation: ${violation.quotaId} - ${violation.quotaMetric} " +
                            "(Value: ${violation.quotaValue})",
                    )
            }
        }

        if (extractedDelay != null && logEnabled) {
            Timber
                .tag(javaClass.simpleName)
                .i("Extracted precise delay from error: $extractedDelay seconds")
        }
        extractedDelay
    } catch (parseEx: Exception) {
        if (logEnabled) {
            Timber.tag(javaClass.simpleName).e("Failed to parse error body: ${parseEx.message}")
        }
        null
    }
}

@PublishedApi
internal fun GeminiAIClient.logGenerateRequest(
    logEnabled: Boolean,
    model: String,
    fullPromptTextLength: Int,
    instructions: String,
    prompt: String,
) {
    if (!logEnabled) return
    Timber.i("Requesting $model ($fullPromptTextLength chars)")
    Timber.d("Instructions:\n$instructions\n")
    Timber.d("Prompt requested:\n$prompt\n")
}

@PublishedApi
internal fun GeminiAIClient.logGenerateResponse(
    logEnabled: Boolean,
    response: GeminiResponse,
    geminiRequest: GeminiRequest,
    partIndex: Int,
) {
    if (!logEnabled) return
    Timber.d("Request stats:\n${response.usageMetadata.toJsonFormat()}\n")
    if (partIndex >= 0) {
        Timber.i("JSON extracted from response part[$partIndex]")
    }
    Timber.i("API Response: ${response.toJsonFormat()}")
    Timber.d(
        "Sent parts: ${
            geminiRequest.toJsonFormatExcludingFields(
                AI_EXCLUDED_FIELDS,
            )
        }",
    )
}

@PublishedApi
internal fun GeminiAIClient.logGenerateStreamingResponse(
    logEnabled: Boolean,
    usageMetadata: GeminiUsageMetadata?,
    fullText: String,
    fullThoughts: String,
    geminiRequest: GeminiRequest,
) {
    if (!logEnabled) return
    Timber.d("Request stats:\n${usageMetadata.toJsonFormat()}\n")
    if (fullThoughts.isNotEmpty()) {
        Timber.d("Streaming thoughts:\n$fullThoughts\n")
    }
    Timber.i("Streaming response (${fullText.length} chars):\n$fullText")
    Timber.d(
        "Sent parts: ${
            geminiRequest.toJsonFormatExcludingFields(
                AI_EXCLUDED_FIELDS,
            )
        }",
    )
}

/**
 * Whether the API rejected the request purely for the thinking level it asked for.
 *
 * Matched on the message because the API expresses it as a plain `INVALID_ARGUMENT`, with the
 * level named only in prose: "Thinking level MINIMAL is not supported for this model."
 */
@PublishedApi
internal fun isUnsupportedThinkingLevel(throwable: Throwable): Boolean {
    val http = throwable as? GeminiHttpException ?: return false
    if (http.code != 400) return false
    val message = "${http.message.orEmpty()} ${http.errorBody.orEmpty()}".lowercase()
    return message.contains("thinking level") && message.contains("not supported")
}
