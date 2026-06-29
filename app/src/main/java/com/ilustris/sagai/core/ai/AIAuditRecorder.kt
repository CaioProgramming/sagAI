package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.GeminiUsageMetadata
import com.ilustris.sagai.core.database.model.AIAuditLog

/**
 * Snapshot of a completed or failed AI call, persisted via [AIClient.recordAudit].
 */
data class AIAuditSnapshot(
    val model: String,
    val blueprintKey: String? = null,
    val dataType: String,
    val status: String,
    val reasoning: String? = null,
    val rawResponse: String? = null,
    val errorMessage: String? = null,
    val responseTimeMs: Long = 0,
    val queueWaitMs: Long = 0,
    val safetyStatus: String? = null,
    val systemInstruction: String? = null,
    val sentVariables: String? = null,
    val promptTokens: Int? = null,
    val candidatesTokens: Int? = null,
    val totalTokens: Int? = null,
) {
    fun toEntity(): AIAuditLog =
        AIAuditLog(
            model = model,
            blueprintKey = blueprintKey,
            dataType = dataType,
            status = status,
            reasoning = reasoning,
            rawResponse = rawResponse,
            errorMessage = errorMessage,
            responseTime = responseTimeMs,
            queueWaitMs = queueWaitMs,
            safetyStatus = safetyStatus,
            systemInstruction = systemInstruction,
            sentVariables = sentVariables,
            promptTokens = promptTokens,
            candidatesTokens = candidatesTokens,
            totalTokens = totalTokens,
        )

    companion object {
        fun success(
            model: String,
            blueprintKey: String?,
            dataType: String,
            reasoning: String?,
            rawResponse: String?,
            responseTimeMs: Long,
            queueWaitMs: Long = 0,
            systemInstruction: String?,
            sentVariables: String?,
            usageMetadata: GeminiUsageMetadata? = null,
        ): AIAuditSnapshot =
            AIAuditSnapshot(
                model = model,
                blueprintKey = blueprintKey,
                dataType = dataType,
                status = "SUCCESS",
                reasoning = reasoning,
                rawResponse = rawResponse,
                responseTimeMs = responseTimeMs,
                queueWaitMs = queueWaitMs,
                systemInstruction = systemInstruction,
                sentVariables = sentVariables,
                promptTokens = usageMetadata?.promptTokenCount,
                candidatesTokens = usageMetadata?.candidatesTokenCount,
                totalTokens = usageMetadata?.totalTokenCount,
            )

        fun error(
            model: String,
            blueprintKey: String?,
            dataType: String,
            errorMessage: String,
            responseTimeMs: Long,
            queueWaitMs: Long = 0,
            safetyStatus: String? = null,
            systemInstruction: String?,
            sentVariables: String?,
        ): AIAuditSnapshot =
            AIAuditSnapshot(
                model = model,
                blueprintKey = blueprintKey,
                dataType = dataType,
                status = "ERROR",
                errorMessage = errorMessage,
                responseTimeMs = responseTimeMs,
                queueWaitMs = queueWaitMs,
                safetyStatus = safetyStatus,
                systemInstruction = systemInstruction,
                sentVariables = sentVariables,
            )
    }
}
