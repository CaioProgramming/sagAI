package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.GeminiError
import com.ilustris.sagai.core.ai.model.GeminiErrorResponse
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.GeminiResponse
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
) : AIClient(
        remoteConfigService,
        promptService,
        ageVerificationService,
        aiAuditLogDao,
    ) {
    @PublishedApi
    internal val requestMutexes = ConcurrentHashMap<String, Mutex>()

    @Volatile
    var lastTokenCount: Int = 0

    companion object {
        const val INPUT_TOKEN_LIMIT = 16000
    }

    @PublishedApi
    internal fun updateReactiveTokenCount(promptTokenCount: Int) {
        lastTokenCount = promptTokenCount
        if (lastTokenCount < (INPUT_TOKEN_LIMIT * GeminiGenerationPolicy.REACTIVE_DELAY_THRESHOLD)) {
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
    ) {
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

        if (tokenCount > INPUT_TOKEN_LIMIT) {
            throw PromptTooLargeException(
                message =
                    buildPromptTooLargeMessage(
                        tokenCount = tokenCount,
                        tokenLimit = INPUT_TOKEN_LIMIT,
                        fullPrompt = fullPromptText,
                    ),
                tokenCount = tokenCount,
                tokenLimit = INPUT_TOKEN_LIMIT,
                fullPrompt = fullPromptText,
            )
        }
    }

    @PublishedApi
    internal fun throwIfApiInputTokenLimitError(
        error: GeminiError,
        fullPromptText: String,
    ) {
        if (isInputTokenLimitError(null, error.message, GeminiErrorResponse(error))) {
            throw PromptTooLargeException(
                message =
                    buildPromptTooLargeMessage(
                        tokenCount = null,
                        tokenLimit = INPUT_TOKEN_LIMIT,
                        fullPrompt = fullPromptText,
                        apiMessage = error.message,
                    ),
                tokenCount = null,
                tokenLimit = INPUT_TOKEN_LIMIT,
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
