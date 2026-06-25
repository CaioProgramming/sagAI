package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.SideEffect
import com.ilustris.sagai.core.database.model.AIStats
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import com.ilustris.sagai.core.network.GeminiApiClient
import com.ilustris.sagai.core.services.AgeVerificationService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.SideEffectService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaClient
    @Inject
    constructor(
        remoteConfig: RemoteConfigService,
        val sideEffectService: SideEffectService,
        geminiApiClient: GeminiApiClient,
        promptService: PromptService,
        aiAuditLogDao: AIAuditLogDao,
        ageVerificationService: AgeVerificationService,
    ) : GeminiAIClient(
            remoteConfig,
            promptService,
            ageVerificationService,
            aiAuditLogDao,
            geminiApiClient,
) {
        companion object {
            const val CORE_FLAG = "SAGA_CORE"

            /** @see GeminiGenerationPolicy.lastGenerateFailure */
            val lastGenerateFailure: String?
                get() = GeminiGenerationPolicy.lastGenerateFailure
        }

        /**
         * @param blueprintKey Optional key identifying the prompt blueprint used. Providing this greatly helps trace prompt generation in the local debugging ai_audit_logs database.
         */
        @Deprecated(
            message = "Use SplitPrompt overload for auditability",
            replaceWith =
                ReplaceWith(
                    expression = "generate(promptSplit = SplitPrompt(processedTemplate = prompt))",
                    imports = ["com.ilustris.sagai.core.ai.model.SplitPrompt"],
                ),
        )
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
                val (type, structure) =
                    buildDataStructure(
                        requirement,
                        describeOutput,
                        getJavaType<T>(),
                        filterOutputFields,
                    )
                val finalInstructions =
                    buildUnifiedInstructions(
                        requirement,
                        requireTranslation,
                        type,
                        structure,
                        userInteraction,
                        prompt,
                        systemInstructions,
                    )
                val normalizedInstructions = finalInstructions.toAINormalize()

                executeSyncGenerationWithRetry(
                    GeminiSyncGenerationParams(
                        model = model,
                        requirement = requirement,
                        useCore = useCore,
                        logEnabled = logEnabled,
                        taskPrompt = prompt,
                        systemInstruction = normalizedInstructions,
                        references = references,
                        temperatureRandomness = temperatureRandomness,
                        audit =
                            GeminiGenerationAuditContext(
                                blueprintKey = aiStats?.blueprintKey ?: blueprintKey,
                                dataType = type,
                                systemInstruction = finalInstructions.toJsonFormat(),
                                sentVariables = aiStats?.sentVariables.toJsonFormat(),
                            ),
                        promptForFailureLog = prompt,
                    ),
                )
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
                val (dataTypeName, systemInstructionMap) =
                    buildStructure<T>(
                        describeOutput,
                        filterOutputFields,
                        requirement,
                        requireTranslation,
                        promptSplit.renderInstructions(),
                    )
                val systemInstruction =
                    buildUnifiedInstructions(
                        requirement,
                        requireTranslation,
                        dataTypeName,
                        "Prompt blueprint instructions",
                        userInteraction,
                        prompt,
                        systemInstructionMap,
                    ).toAINormalize()

                executeSyncGenerationWithRetry(
                    GeminiSyncGenerationParams(
                        model = model,
                        requirement = requirement,
                        useCore = useCore,
                        logEnabled = logEnabled,
                        taskPrompt = prompt,
                        systemInstruction = systemInstruction,
                        references = references,
                        temperatureRandomness = temperatureRandomness,
                        audit =
                            GeminiGenerationAuditContext(
                                blueprintKey = promptSplit.blueprintKey,
                                dataType = dataTypeName,
                                systemInstruction = systemInstruction,
                                sentVariables = promptSplit.sentVariables.toJsonFormat(),
                            ),
                        promptForFailureLog = prompt,
                    ),
                )
            }

        /**
         * Builds a [SplitPrompt] from Remote Config and runs [generate] with optional instruction merges.
         */
        suspend fun buildBlueprintPrompt(
            remoteConfigKey: String,
            variables: Map<String, String> = emptyMap(),
            mergedInstructionMaps: List<Map<String, Any>> = emptyList(),
            logEnabled: Boolean = true,
        ): SplitPrompt {
            var prompt =
                promptService.buildSplitBlueprint(
                    remoteConfigKey,
                    variables,
                    logEnabled = logEnabled,
                )
            mergedInstructionMaps.forEach { prompt = prompt.mergeInstructions(it) }
            return prompt
        }

        suspend inline fun <reified T> generateBlueprint(
            remoteConfigKey: String,
            variables: Map<String, String> = emptyMap(),
            mergedInstructionMaps: List<Map<String, Any>> = emptyList(),
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
            generate(
                promptSplit =
                    buildBlueprintPrompt(
                        remoteConfigKey,
                        variables,
                        mergedInstructionMaps,
                        logEnabled,
                    ),
                userInteraction = userInteraction,
                references = references,
                temperatureRandomness = temperatureRandomness,
                requireTranslation = requireTranslation,
                describeOutput = describeOutput,
            filterOutputFields = filterOutputFields,
            useCore = useCore,
            requirement = requirement,
            logEnabled = logEnabled,
        )

        suspend inline fun <reified T> buildStructure(
            describeOutput: Boolean,
            filterOutputFields: List<String>,
            requirement: ModelRequirement,
            requireTranslation: Boolean,
            systemInstructions: Map<String, Any>,
        ): Pair<String, Map<String, Any>> {
            val dataType = getJavaType<T>()

            val (typeName, structure) =
                buildDataStructure(
                    requirement,
                    describeOutput,
                    dataType,
                    filterOutputFields,
                )

            val corePrompt = buildCorePrompt(requirement, requireTranslation, typeName, structure)

            val systemInstruction = buildInstructionsMap(corePrompt, systemInstructions)
            return Pair(typeName, systemInstruction)
        }

        suspend fun checkSafety(
            userInteraction: Boolean,
            prompt: String,
        ) {
            // No-op: Safety is integrated into main requests via AIGeneration.error and safety directives.
        }

        @Deprecated(
            message = "Use SplitPrompt overload for auditability",
            replaceWith =
                ReplaceWith(
                    expression = "generateStreaming(promptSplit = SplitPrompt(processedTemplate = prompt))",
                    imports = ["com.ilustris.sagai.core.ai.model.SplitPrompt"],
                ),
        )
        inline fun <reified T> generateStreaming(
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
        ): Flow<StreamingState<T?>> =
            flow {
                val (dataTypeName, baseSystemInstruction) =
                    buildStructure<T>(
                        describeOutput,
                        filterOutputFields,
                        requirement,
                        requireTranslation,
                        systemInstructions,
                    )

                val systemInstruction =
                    buildUnifiedInstructions(
                        requirement,
                        requireTranslation,
                        dataTypeName,
                        "Prompt blueprint instructions",
                        userInteraction,
                        prompt,
                        baseSystemInstruction,
                    ).toAINormalize()

                val model = modelName(requirement)

                emitAll(
                    streamingGenerationFlow<T>(
                        GeminiStreamingGenerationParams(
                            model = model,
                            requirement = requirement,
                            useCore = useCore,
                            logEnabled = logEnabled,
                            taskPrompt = prompt,
                            systemInstruction = systemInstruction,
                            references = references,
                            temperatureRandomness = temperatureRandomness,
                            audit =
                                GeminiGenerationAuditContext(
                                    blueprintKey = aiStats?.blueprintKey ?: blueprintKey,
                                    dataType = dataTypeName,
                                    systemInstruction = systemInstruction,
                                    sentVariables = aiStats?.sentVariables.toJsonFormat(),
                                ),
                            onGuardrailBlock = {
                                sideEffectService.emit(SideEffect.GuardrailBlock(it.status))
                            },
                        ),
                    ),
                )
            }.flowOn(Dispatchers.IO)

    inline fun <reified T> generateStreaming(
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
    ): Flow<StreamingState<T?>> =
        flow {
                val prompt = promptSplit.processedTemplate
                val (dataTypeName, baseSystemInstruction) =
                    buildStructure<T>(
                        describeOutput,
                        filterOutputFields,
                        requirement,
                        requireTranslation,
                        promptSplit.renderInstructions(),
                    )

                val systemInstruction =
                    buildUnifiedInstructions(
                        requirement,
                        requireTranslation,
                        dataTypeName,
                        "Prompt blueprint instructions",
                        userInteraction,
                        prompt,
                        baseSystemInstruction,
                    ).toAINormalize()

                val model = modelName(requirement)

                emitAll(
                    streamingGenerationFlow<T>(
                        GeminiStreamingGenerationParams(
                            model = model,
                            requirement = requirement,
                            useCore = useCore,
                            logEnabled = logEnabled,
                            taskPrompt = prompt,
                            systemInstruction = systemInstruction,
                            references = references,
                            temperatureRandomness = temperatureRandomness,
                            audit =
                                GeminiGenerationAuditContext(
                                    blueprintKey = promptSplit.blueprintKey,
                                    dataType = dataTypeName,
                                    systemInstruction = systemInstruction,
                                    sentVariables = promptSplit.sentVariables.toJsonFormat(),
                                ),
                        includeSystemInFullPrompt = false,
                        onGuardrailBlock = {
                            sideEffectService.emit(SideEffect.GuardrailBlock(it.status))
                        },
                    ),
                    ),
                )
            }.flowOn(Dispatchers.IO)

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
    }

fun buildInstructionsMap(
    corePrompt: SplitPrompt,
    systemInstructions: Map<String, Any>,
): Map<String, Any> =
    buildMap {
        putAll(corePrompt.renderInstructions().plus("task" to corePrompt.processedTemplate))
        putAll(systemInstructions)
    }

fun buildInstructions(
    corePrompt: SplitPrompt,
    systemInstructions: Map<String, Any>,
): String = buildInstructionsMap(corePrompt, systemInstructions).toAINormalize()

const val KEY_FLAG = "FIREBASE_KEY"
