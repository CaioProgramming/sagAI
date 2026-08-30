package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.GeminiError
import com.ilustris.sagai.core.ai.model.GeminiErrorResponse
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.GeminiResponse
import com.ilustris.sagai.core.ai.key.ApiUsageTracker
import com.ilustris.sagai.core.ai.key.QuotaStatusService
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import com.ilustris.sagai.core.network.GeminiApiClient
import com.ilustris.sagai.core.services.AgeVerificationService
import com.ilustris.sagai.core.services.RemoteConfigService
import kotlinx.coroutines.sync.Mutex
import okhttp3.ResponseBody
import java.util.concurrent.ConcurrentHashMap

/**
 * Gemini-specific [AIClient] that owns API execution, token checks, retry policy, and audit logging.
 * [GemmaClient] builds prompts and delegates execution here via [GeminiGenerationEngine].
 */
abstract class GeminiAIClient(
    remoteConfigService: RemoteConfigService,
    promptService: PromptService,
    ageVerificationService: AgeVerificationService,
    aiAuditLogDao: AIAuditLogDao,
    @PublishedApi internal val geminiApiClient: GeminiApiClient,
    userApiKeyStore: UserApiKeyStore,
    quotaStatusService: QuotaStatusService,
    modelCatalog: ModelCatalog,
    apiUsageTracker: ApiUsageTracker,
) : AIClient(
        remoteConfigService,
        promptService,
        ageVerificationService,
        aiAuditLogDao,
        userApiKeyStore,
        quotaStatusService,
        modelCatalog,
        apiUsageTracker,
    ) {
    @PublishedApi
    internal val requestMutexes = ConcurrentHashMap<String, Mutex>()

    @Volatile
    var lastTokenCount: Int = 0

    /**
     * Tokens the last pre-flight counted, kept so the retry handler can tell a throttle that will
     * pass from a request that can never fit inside the per-minute budget.
     */
    @Volatile
    var lastPromptTokenCount: Int = 0

    /** Ceiling applied to the last pre-flight, quoted by the error paths that report on it. */
    @Volatile
    var lastTokenLimit: Int = ModelCatalog.DEFAULT_INPUT_TOKEN_LIMIT

    @PublishedApi
    internal fun updateReactiveTokenCount(
        promptTokenCount: Int,
        tokenLimit: Int,
    ) {
        lastTokenCount = promptTokenCount
        if (lastTokenCount < (tokenLimit * GeminiGenerationPolicy.REACTIVE_DELAY_THRESHOLD)) {
            lastTokenCount = 0
        }
    }

    @PublishedApi
    internal suspend fun ensurePromptWithinTokenLimit(
        model: String,
        apiKey: String,
        request: GeminiRequest,
        parts: List<GeminiPart>,
        fullPromptText: String,
        systemInstruction: String? = null,
        blueprintKey: String? = null,
    ) {
        val tokenLimit = modelCatalog.effectiveInputLimit(model, apiKey)
        lastTokenLimit = tokenLimit
        val tokenCount =
            runCatching {
                geminiApiClient.countTokens(model, apiKey, request).totalTokens
            }.getOrNull()
                ?: (
                    GeminiTokenEstimator.estimateRequestTokens(parts) +
                        GeminiTokenEstimator.estimateSystemInstructionTokens(
                            systemInstruction,
                        )
                )

        lastPromptTokenCount = tokenCount

        if (tokenCount > tokenLimit) {
            throw PromptTooLargeException(
                message =
                    buildPromptTooLargeMessage(
                        tokenCount = tokenCount,
                        tokenLimit = tokenLimit,
                        fullPrompt = fullPromptText,
                    ),
                tokenCount = tokenCount,
                tokenLimit = tokenLimit,
                fullPrompt = fullPromptText,
                blueprintKey = blueprintKey,
            )
        }
    }

    @PublishedApi
    internal fun throwIfApiInputTokenLimitError(
        error: GeminiError,
        fullPromptText: String,
    ) {
        if (isInputTokenLimitError(error.code, error.message, GeminiErrorResponse(error))) {
            throw PromptTooLargeException(
                message =
                    buildPromptTooLargeMessage(
                        tokenCount = null,
                        tokenLimit = lastTokenLimit,
                        fullPrompt = fullPromptText,
                        apiMessage = error.message,
                    ),
                tokenCount = null,
                tokenLimit = lastTokenLimit,
                fullPrompt = fullPromptText,
            )
        }
    }

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
    ): ResponseBody = geminiApiClient.streamGenerateContent(model, apiKey, request)
}
