package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import java.lang.reflect.Type

/**
 * Fully resolved system instructions for a Gemini call: core + safety + blueprint buckets.
 */
data class PreparedGenerationInstructions(
    val dataTypeName: String,
    val taskPrompt: String,
    val normalizedSystemInstruction: String,
    val auditSystemInstruction: String,
    val blueprintKey: String? = null,
    val sentVariables: String? = null,
)

suspend fun AIClient.prepareFromSplitPrompt(
    promptSplit: SplitPrompt,
    dataType: Type,
    requirement: ModelRequirement,
    requireTranslation: Boolean,
    describeOutput: Boolean,
    filterOutputFields: List<String>,
    userInteraction: Boolean,
): PreparedGenerationInstructions {
    val taskPrompt = promptSplit.processedTemplate
    val (dataTypeName, outputStructure) =
        buildDataStructure(
            requirement,
            describeOutput,
            dataType,
            filterOutputFields,
        )
    val instructionsMap =
        buildUnifiedInstructions(
            requirement = requirement,
            requireTranslation = requireTranslation,
            dataTypeName = dataTypeName,
            outputStructure = outputStructure,
            userInteraction = userInteraction,
            blueprintInstructions = promptSplit.renderInstructions(),
        )
    return PreparedGenerationInstructions(
        dataTypeName = dataTypeName,
        taskPrompt = taskPrompt,
        normalizedSystemInstruction = instructionsMap.toAINormalize(),
        auditSystemInstruction = instructionsMap.toJsonFormat(),
        blueprintKey = promptSplit.blueprintKey,
        sentVariables = promptSplit.sentVariables.toJsonFormat(),
    )
}

suspend fun AIClient.prepareFromRawPrompt(
    taskPrompt: String,
    dataType: Type,
    requirement: ModelRequirement,
    requireTranslation: Boolean,
    describeOutput: Boolean,
    filterOutputFields: List<String>,
    userInteraction: Boolean,
    blueprintInstructions: Map<String, Any> = emptyMap(),
    blueprintKey: String? = null,
    sentVariables: String? = null,
): PreparedGenerationInstructions {
    val (dataTypeName, outputStructure) =
        buildDataStructure(
            requirement,
            describeOutput,
            dataType,
            filterOutputFields,
        )
    val instructionsMap =
        buildUnifiedInstructions(
            requirement = requirement,
            requireTranslation = requireTranslation,
            dataTypeName = dataTypeName,
            outputStructure = outputStructure,
            userInteraction = userInteraction,
            blueprintInstructions = blueprintInstructions,
        )
    return PreparedGenerationInstructions(
        dataTypeName = dataTypeName,
        taskPrompt = taskPrompt,
        normalizedSystemInstruction = instructionsMap.toAINormalize(),
        auditSystemInstruction = instructionsMap.toJsonFormat(),
        blueprintKey = blueprintKey,
        sentVariables = sentVariables,
    )
}

fun PreparedGenerationInstructions.toAuditContext(): GeminiGenerationAuditContext =
    GeminiGenerationAuditContext(
        blueprintKey = blueprintKey,
        dataType = dataTypeName,
        systemInstruction = auditSystemInstruction,
        sentVariables = sentVariables,
    )

fun PreparedGenerationInstructions.toSyncParams(
    model: String,
    requirement: ModelRequirement,
    useCore: Boolean,
    logEnabled: Boolean,
    references: List<ImageReference?>,
    temperatureRandomness: Float,
    thinkingLevel: String?,
): GeminiSyncGenerationParams =
    GeminiSyncGenerationParams(
        model = model,
        requirement = requirement,
        useCore = useCore,
        logEnabled = logEnabled,
        taskPrompt = taskPrompt,
        systemInstruction = normalizedSystemInstruction,
        references = references,
        temperatureRandomness = temperatureRandomness,
        thinkingLevel = thinkingLevel,
        audit = toAuditContext(),
        promptForFailureLog = taskPrompt,
    )

fun PreparedGenerationInstructions.toStreamingParams(
    model: String,
    requirement: ModelRequirement,
    useCore: Boolean,
    logEnabled: Boolean,
    references: List<ImageReference?>,
    temperatureRandomness: Float,
    thinkingLevel: String?,
    includeSystemInFullPrompt: Boolean = true,
    onGuardrailBlock: (suspend (GuardrailsException) -> Unit)? = null,
): GeminiStreamingGenerationParams =
    GeminiStreamingGenerationParams(
        model = model,
        requirement = requirement,
        useCore = useCore,
        logEnabled = logEnabled,
        taskPrompt = taskPrompt,
        systemInstruction = normalizedSystemInstruction,
        references = references,
        temperatureRandomness = temperatureRandomness,
        thinkingLevel = thinkingLevel,
        audit = toAuditContext(),
        includeSystemInFullPrompt = includeSystemInFullPrompt,
        onGuardrailBlock = onGuardrailBlock,
    )

suspend inline fun <reified T> AIClient.prepareFromSplitPrompt(
    promptSplit: SplitPrompt,
    requirement: ModelRequirement,
    requireTranslation: Boolean,
    describeOutput: Boolean,
    filterOutputFields: List<String>,
    userInteraction: Boolean,
): PreparedGenerationInstructions =
    prepareFromSplitPrompt(
        promptSplit = promptSplit,
        dataType = getJavaType<T>(),
        requirement = requirement,
        requireTranslation = requireTranslation,
        describeOutput = describeOutput,
        filterOutputFields = filterOutputFields,
        userInteraction = userInteraction,
    )

suspend inline fun <reified T> AIClient.prepareFromRawPrompt(
    taskPrompt: String,
    requirement: ModelRequirement,
    requireTranslation: Boolean,
    describeOutput: Boolean,
    filterOutputFields: List<String>,
    userInteraction: Boolean,
    blueprintInstructions: Map<String, Any> = emptyMap(),
    blueprintKey: String? = null,
    sentVariables: String? = null,
): PreparedGenerationInstructions =
    prepareFromRawPrompt(
        taskPrompt = taskPrompt,
        dataType = getJavaType<T>(),
        requirement = requirement,
        requireTranslation = requireTranslation,
        describeOutput = describeOutput,
        filterOutputFields = filterOutputFields,
        userInteraction = userInteraction,
        blueprintInstructions = blueprintInstructions,
        blueprintKey = blueprintKey,
        sentVariables = sentVariables,
    )
