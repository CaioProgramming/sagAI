package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.local.LocalAiConfig
import com.ilustris.sagai.core.ai.local.LocalAiConfigLoader
import com.ilustris.sagai.core.ai.local.LocalAiEligibility
import com.ilustris.sagai.core.ai.local.LocalAiExecutor
import com.ilustris.sagai.core.ai.local.LocalAiSidebackRouting
import com.ilustris.sagai.core.ai.local.LocalAiSidebackStep
import com.ilustris.sagai.core.ai.local.LocalAiTelemetry
import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.SideEffect
import com.ilustris.sagai.core.database.model.AIStats
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import com.ilustris.sagai.core.network.GeminiApiClient
import com.ilustris.sagai.core.services.AgeVerificationService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.SideEffectService
import com.ilustris.sagai.core.utils.toJsonFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
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
        @PublishedApi
        internal val localAiExecutor: LocalAiExecutor,
        @PublishedApi
        internal val localAiConfigLoader: LocalAiConfigLoader,
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
                val prepared =
                    prepareFromRawPrompt<T>(
                        taskPrompt = prompt,
                        requirement = requirement,
                        requireTranslation = requireTranslation,
                        describeOutput = describeOutput,
                        filterOutputFields = filterOutputFields,
                        userInteraction = userInteraction,
                        blueprintInstructions = systemInstructions,
                        blueprintKey = aiStats?.blueprintKey ?: blueprintKey,
                        sentVariables = aiStats?.sentVariables.toJsonFormat(),
                    )
                val params =
                    prepared.toSyncParams(
                        model = modelName(requirement),
                        thinkingLevel = thinkingLevel(requirement),
                        requirement = requirement,
                        useCore = useCore,
                        logEnabled = logEnabled,
                        references = references,
                        temperatureRandomness = temperatureRandomness,
                    )
                executeSyncGenerationWithLocalFallback(
                    params = params,
                    parse = { raw -> parseGenerationJson<T>(raw).data },
                    cloud = { executeSyncGenerationWithRetry(params) },
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
                checkSafety(userInteraction, promptSplit.processedTemplate)
                val prepared =
                    prepareFromSplitPrompt<T>(
                        promptSplit = promptSplit,
                        requirement = requirement,
                        requireTranslation = requireTranslation,
                        describeOutput = describeOutput,
                        filterOutputFields = filterOutputFields,
                        userInteraction = userInteraction,
                    )
                val params =
                    prepared.toSyncParams(
                        model = modelName(requirement),
                        thinkingLevel = thinkingLevel(requirement),
                        requirement = requirement,
                        useCore = useCore,
                        logEnabled = logEnabled,
                        references = references,
                        temperatureRandomness = temperatureRandomness,
                    )
                executeSyncGenerationWithLocalFallback(
                    params = params,
                    parse = { raw -> parseGenerationJson<T>(raw).data },
                    cloud = { executeSyncGenerationWithRetry(params) },
                )
            }

        /**
         * Builds a [SplitPrompt] from Remote Config and runs [generate] with optional instruction merges.
         */
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

        suspend fun checkSafety(
            userInteraction: Boolean,
            prompt: String,
        ) {
            // No-op: Safety is integrated into main requests via AIGeneration.error and safety directives.
        }

        /**
         * Runs a pre-built [PreparedGenerationInstructions] through the Gemini engine.
         * Used by services that prepare instructions on [AIClient] but execute via [GemmaClient].
         */
        suspend inline fun <reified T> executePrepared(
            prepared: PreparedGenerationInstructions,
            requirement: ModelRequirement = ModelRequirement.MEDIUM,
            references: List<ImageReference?> = emptyList(),
            temperatureRandomness: Float = .5f,
            useCore: Boolean = false,
            logEnabled: Boolean = true,
        ): T? =
            withContext(Dispatchers.IO) {
                val params =
                    prepared.toSyncParams(
                        model = modelName(requirement),
                        thinkingLevel = thinkingLevel(requirement),
                        requirement = requirement,
                        useCore = useCore,
                        logEnabled = logEnabled,
                        references = references,
                        temperatureRandomness = temperatureRandomness,
                    )
                executeSyncGenerationWithLocalFallback(
                    params = params,
                    parse = { raw -> parseGenerationJson<T>(raw).data },
                    cloud = { executeSyncGenerationWithRetry(params) },
                )
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
                val prepared =
                    prepareFromRawPrompt<T>(
                        taskPrompt = prompt,
                        requirement = requirement,
                        requireTranslation = requireTranslation,
                        describeOutput = describeOutput,
                        filterOutputFields = filterOutputFields,
                        userInteraction = userInteraction,
                        blueprintInstructions = systemInstructions,
                        blueprintKey = aiStats?.blueprintKey ?: blueprintKey,
                        sentVariables = aiStats?.sentVariables.toJsonFormat(),
                    )
                emitAll(
                    streamingGenerationFlow<T>(
                        prepared.toStreamingParams(
                            model = modelName(requirement),
                            thinkingLevel = thinkingLevel(requirement),
                            requirement = requirement,
                            useCore = useCore,
                            logEnabled = logEnabled,
                            references = references,
                            temperatureRandomness = temperatureRandomness,
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
                val prepared =
                    prepareFromSplitPrompt<T>(
                        promptSplit = promptSplit,
                        requirement = requirement,
                        requireTranslation = requireTranslation,
                        describeOutput = describeOutput,
                        filterOutputFields = filterOutputFields,
                        userInteraction = userInteraction,
                    )
                emitAll(
                    streamingGenerationFlow<T>(
                        prepared.toStreamingParams(
                            model = modelName(requirement),
                            thinkingLevel = thinkingLevel(requirement),
                            requirement = requirement,
                            useCore = useCore,
                            logEnabled = logEnabled,
                            references = references,
                            temperatureRandomness = temperatureRandomness,
                            includeSystemInFullPrompt = false,
                            onGuardrailBlock = {
                                sideEffectService.emit(SideEffect.GuardrailBlock(it.status))
                            },
                        ),
                    ),
                )
            }.flowOn(Dispatchers.IO)

        suspend fun <T> executeSyncGenerationWithLocalFallback(
            params: GeminiSyncGenerationParams,
            parse: (String) -> T?,
            cloud: suspend () -> T?,
        ): T? {
            val config = localAiConfigLoader.load()
            if (!LocalAiEligibility.isEligible(params, config)) {
                return cloud()
            }

            val availability = localAiExecutor.availability()
            when (LocalAiSidebackRouting.resolveStep(availability)) {
                LocalAiSidebackStep.TRIGGER_DOWNLOAD_AND_CLOUD -> {
                    localAiExecutor.ensureModelDownloaded()
                    LocalAiTelemetry.recordLocalMiss("downloadable", availability)
                    return cloud()
                }

                LocalAiSidebackStep.CLOUD_ONLY -> {
                    LocalAiTelemetry.recordLocalMiss(availability.name.lowercase(), availability)
                    return cloud()
                }

                LocalAiSidebackStep.TRY_LOCAL -> {
                    Unit
                }
            }

            val inferenceStart = System.currentTimeMillis()
            try {
                val rawText =
                    withTimeout(config.timeoutMs) {
                        localAiExecutor
                            .generate(
                                prompt = params.taskPrompt,
                                systemInstruction = params.systemInstruction,
                                maxOutputTokens = LocalAiConfig.MAX_OUTPUT_TOKENS,
                            ).getOrThrow()
                    }
                val parsed = parse(rawText)
                val inferenceMs = System.currentTimeMillis() - inferenceStart
                LocalAiTelemetry.recordLocalHit(inferenceMs)
                if (params.logEnabled) {
                    Timber.d("Local AI data ->\n$parsed\n")
                }
                recordAudit(
                    AIAuditSnapshot.success(
                        model = LocalAiConfig.LOCAL_MODEL_AUDIT_NAME,
                        blueprintKey = params.audit.blueprintKey,
                        dataType = params.audit.dataType,
                        reasoning = null,
                        rawResponse = rawText,
                        responseTimeMs = inferenceMs,
                        systemInstruction = params.audit.systemInstruction,
                        sentVariables = params.audit.sentVariables,
                        usageMetadata = null,
                    ),
                    logEnabled = params.logEnabled,
                )
                return parsed
            } catch (e: GuardrailsException) {
                LocalAiTelemetry.recordLocalMiss("guardrail_${e.status.name.lowercase()}")
                return cloud()
            } catch (e: Exception) {
                LocalAiTelemetry.recordLocalMiss(e.javaClass.simpleName)
                if (params.logEnabled) {
                    Timber.w(e, "Local AI failed; falling back to cloud")
                }
                return cloud()
            }
        }

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

const val KEY_FLAG = "FIREBASE_KEY"
