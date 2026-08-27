package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.GeminiUsageMetadata
import com.ilustris.sagai.core.ai.model.ImageReference

data class GeminiGenerationAuditContext(
    val blueprintKey: String? = null,
    val dataType: String,
    val systemInstruction: String,
    val sentVariables: String? = null,
)

data class GeminiSyncGenerationParams(
    val model: String,
    val requirement: ModelRequirement,
    val useCore: Boolean,
    val logEnabled: Boolean,
    val taskPrompt: String,
    val systemInstruction: String,
    val references: List<ImageReference?>,
    val temperatureRandomness: Float,
    val thinkingLevel: String?,
    val audit: GeminiGenerationAuditContext,
    val promptForFailureLog: String,
    val includeSystemInFullPrompt: Boolean = true,
)

data class GeminiStreamingGenerationParams(
    val model: String,
    val requirement: ModelRequirement,
    val useCore: Boolean,
    val logEnabled: Boolean,
    val taskPrompt: String,
    val systemInstruction: String,
    val references: List<ImageReference?>,
    val temperatureRandomness: Float,
    val thinkingLevel: String?,
    val audit: GeminiGenerationAuditContext,
    val includeSystemInFullPrompt: Boolean = true,
    val onGuardrailBlock: (suspend (GuardrailsException) -> Unit)? = null,
)

data class GeminiParsedGeneration<T>(
    val data: T?,
    val rawResponseText: String,
    val nativeThoughts: String?,
    val usageMetadata: GeminiUsageMetadata?,
)

data class StreamingAccumulationResult(
    val fullText: String,
    val fullThoughts: String,
    val usageMetadata: GeminiUsageMetadata?,
)
