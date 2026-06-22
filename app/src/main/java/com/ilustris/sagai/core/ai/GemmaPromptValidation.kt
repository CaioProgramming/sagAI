package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.GeminiErrorResponse
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.network.GeminiApiCodec
import com.ilustris.sagai.core.network.GeminiHttpException
import timber.log.Timber

/**
 * Raised when the prompt exceeds [GemmaClient.INPUT_TOKEN_LIMIT], either from a pre-flight
 * countTokens check or from a non-retryable API rejection.
 */
class PromptTooLargeException(
    message: String,
    val tokenCount: Int?,
    val tokenLimit: Int,
    val fullPrompt: String,
    cause: Throwable? = null,
) : IllegalStateException(message, cause)

internal data class ParsedGeminiHttpError(
    val statusCode: Int,
    val message: String,
    val body: String?,
    val parsed: GeminiErrorResponse?,
    val retryDelaySeconds: Long?,
    val isInputTokenLimit: Boolean,
)

object GeminiTokenEstimator {
    private const val IMAGE_TOKEN_ESTIMATE = 300

    fun estimateTextTokens(text: String): Int {
        if (text.isEmpty()) return 0
        var charUnits = 0
        for (ch in text) {
            charUnits +=
                when {
                    ch.code > 0xFFFF -> 4
                    ch.code > 0x7F -> 3
                    ch.isWhitespace() -> 1
                    else -> 1
                }
        }
        return (charUnits / 3.5).toInt().coerceAtLeast(1)
    }

    fun estimateRequestTokens(parts: List<GeminiPart>): Int =
        parts.sumOf { part ->
            when {
                part.text != null -> estimateTextTokens(part.text)
                part.inlineData != null -> IMAGE_TOKEN_ESTIMATE
                else -> 0
            }
        }

    fun estimateSystemInstructionTokens(systemInstruction: String?): Int = systemInstruction?.let(::estimateTextTokens) ?: 0
}

fun isInputTokenLimitError(
    statusCode: Int?,
    message: String?,
    errorResponse: GeminiErrorResponse?,
): Boolean {
    val normalizedMessage = message?.lowercase().orEmpty()
    if (normalizedMessage.contains("input token") ||
        normalizedMessage.contains("too many tokens") ||
        normalizedMessage.contains("request contains too many tokens") ||
        normalizedMessage.contains("request payload size exceeds") ||
        (normalizedMessage.contains("token count") && normalizedMessage.contains("supports up to"))
    ) {
        return true
    }

    errorResponse?.error?.details?.forEach { detail ->
        detail.violations?.forEach { violation ->
            val metric = violation.quotaMetric?.lowercase().orEmpty()
            val quotaId = violation.quotaId?.lowercase().orEmpty()
            if ((metric.contains("input") && metric.contains("token")) ||
                (quotaId.contains("input") && quotaId.contains("token"))
            ) {
                return true
            }
        }
    }

    return statusCode == 400 && normalizedMessage.contains("token")
}

internal fun parseGeminiHttpError(
    statusCode: Int,
    body: String?,
): ParsedGeminiHttpError {
    val parsed =
        body
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { GeminiApiCodec.decodeErrorResponse(it) }.getOrNull() }
    val message = parsed?.error?.message?.takeIf { it.isNotBlank() } ?: body.orEmpty()
    val retryDelaySeconds =
        parsed
            ?.error
            ?.details
            ?.find { it.type == "type.googleapis.com/google.rpc.RetryInfo" }
            ?.retryDelay
            ?.removeSuffix("s")
            ?.toDoubleOrNull()
            ?.toLong()
    return ParsedGeminiHttpError(
        statusCode = statusCode,
        message = message,
        body = body,
        parsed = parsed,
        retryDelaySeconds = retryDelaySeconds,
        isInputTokenLimit = isInputTokenLimitError(statusCode, message, parsed),
    )
}

fun buildFullPromptText(
    taskPrompt: String,
    referenceDescriptions: List<String>,
    systemInstruction: String? = null,
): String =
    buildString {
        systemInstruction?.takeIf { it.isNotBlank() }?.let { instruction ->
            append("[system instruction]\n")
            append(instruction)
            append("\n\n")
        }
        append(taskPrompt)
        referenceDescriptions.forEach { description ->
            append("\n\n[image reference]\n")
            append(description)
        }
    }

fun buildPromptTooLargeMessage(
    tokenCount: Int?,
    tokenLimit: Int,
    fullPrompt: String,
    apiMessage: String? = null,
): String =
    buildString {
        append("Prompt too large")
        tokenCount?.let { append(": $it tokens exceeds limit of $tokenLimit") }
            ?: append(": exceeds limit of $tokenLimit tokens")
        apiMessage?.takeIf { it.isNotBlank() }?.let { append(" (").append(it).append(")") }
        append(".\n\nFull prompt:\n")
        append(fullPrompt)
    }

fun formatEstimatedTokenDiagnostics(
    parts: List<GeminiPart>,
    tokenLimit: Int,
    systemInstruction: String? = null,
): String {
    if (parts.isEmpty() && systemInstruction.isNullOrBlank()) return ""
    val estimated =
        GeminiTokenEstimator.estimateRequestTokens(parts) +
            GeminiTokenEstimator.estimateSystemInstructionTokens(systemInstruction)
    return "Estimated prompt tokens: ~$estimated / limit $tokenLimit"
}

fun logEstimatedPromptTokensOnFailure(
    parts: List<GeminiPart>,
    tokenLimit: Int,
    context: String,
    logEnabled: Boolean,
    cause: Throwable,
    systemInstruction: String? = null,
) {
    if (!logEnabled) return
    val diagnostics = formatEstimatedTokenDiagnostics(parts, tokenLimit, systemInstruction)
    if (diagnostics.isEmpty()) return
    Timber.tag("GemmaClient").w(
        "$context — $diagnostics — ${cause.javaClass.simpleName}: ${cause.message}",
    )
}

fun appendEstimatedTokenDiagnostics(
    message: String,
    parts: List<GeminiPart>,
    tokenLimit: Int,
    systemInstruction: String? = null,
): String {
    val diagnostics = formatEstimatedTokenDiagnostics(parts, tokenLimit, systemInstruction)
    if (diagnostics.isEmpty()) return message
    return "$message\n$diagnostics"
}

fun classifyPromptLimitFailure(
    throwable: Throwable,
    fullPrompt: String,
    tokenLimit: Int,
): PromptTooLargeException? {
    if (throwable is PromptTooLargeException) return throwable

    val parsed =
        when (throwable) {
            is GeminiHttpException -> parseGeminiHttpError(throwable.code, throwable.errorBody)
            else -> null
        } ?: return null

    if (!parsed.isInputTokenLimit) return null

    return PromptTooLargeException(
        message = buildPromptTooLargeMessage(null, tokenLimit, fullPrompt, parsed.message),
        tokenCount = null,
        tokenLimit = tokenLimit,
        fullPrompt = fullPrompt,
        cause = throwable,
    )
}

fun extractRetryDelayFromException(e: Exception): Long? {
    val parsed =
        when (e) {
            is GeminiHttpException -> parseGeminiHttpError(e.code, e.errorBody)
            else -> null
        } ?: return null
    if (parsed.isInputTokenLimit) return null
    return parsed.retryDelaySeconds
}
