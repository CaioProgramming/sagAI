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
    val logEnabled: Boolean,
    /**
     * Whether a quota verdict from this request may change the app's quota state.
     *
     * False for decoration — the holding lines. Those run on the MINIMAL tier, which is a
     * different model from the narrative work and so a different daily bucket, but
     * [com.ilustris.sagai.core.ai.key.QuotaStatusService] surfaces a single aggregate block across
     * models. Letting a loading line record a daily exhaustion would therefore stop every
     * generation in the app, including the one it was decorating, which still had quota of its
     * own. It must not publish a cooldown either: the countdown on screen belongs to the real
     * request, and clearing or replacing it would misreport how long the user is actually waiting.
     */
    val reportsQuota: Boolean = true,
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
    val logEnabled: Boolean,
    /**
     * Whether a quota verdict from this request may change the app's quota state.
     *
     * False for decoration — the holding lines. Those run on the MINIMAL tier, which is a
     * different model from the narrative work and so a different daily bucket, but
     * [com.ilustris.sagai.core.ai.key.QuotaStatusService] surfaces a single aggregate block across
     * models. Letting a loading line record a daily exhaustion would therefore stop every
     * generation in the app, including the one it was decorating, which still had quota of its
     * own. It must not publish a cooldown either: the countdown on screen belongs to the real
     * request, and clearing or replacing it would misreport how long the user is actually waiting.
     */
    val reportsQuota: Boolean = true,
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
