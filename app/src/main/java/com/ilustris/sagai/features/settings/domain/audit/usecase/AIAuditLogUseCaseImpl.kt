package com.ilustris.sagai.features.settings.domain.audit.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.model.PromptBlueprint
import com.ilustris.sagai.core.ai.prompts.AuditLogPrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.settings.domain.audit.repository.AIAuditLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AIAuditLogUseCaseImpl
    @Inject
    constructor(
        private val repository: AIAuditLogRepository,
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val remoteConfigService: RemoteConfigService,
    ) : AIAuditLogUseCase {
        override suspend fun clearLogs(): RequestResult<Unit> =
            executeRequest {
                repository.clearLogs()
            }

        override suspend fun getRecentLogs(limit: Int): Flow<List<AIAuditLog>> = repository.getRecentLogs(limit)

        override suspend fun generateSuggestion(log: AIAuditLog): RequestResult<Unit> =
            executeRequest {
                try {
                    val blueprintKey = log.blueprintKey ?: "unknown"
                    val originalBlueprint = remoteConfigService.getJson<PromptBlueprint>(blueprintKey)

                    val logExclusions = listOf("id", "timestamp", "suggestion", "rawResponse")
                    val blueprintExclusions =
                        listOf("omitHeaders") // we want to see the template, role, directives, rules

                    val promptArgs =
                        mapOf(
                            "blueprint" to
                                (
                                    originalBlueprint?.toAINormalize(blueprintExclusions)
                                        ?: "Blueprint not found for key: $blueprintKey"
                                ),
                            "aiLog" to log.toAINormalize(logExclusions),
                        )

                    val prompt =
                        promptService.buildRemotePrompt(
                            AuditLogPrompts.AUDIT_LOG_SUGGESTION_BLUEPRINT,
                            promptArgs,
                        )

                    val suggestionResult =
                        gemmaClient.generate<String>(
                            prompt = prompt,
                            blueprintKey = AuditLogPrompts.AUDIT_LOG_SUGGESTION_BLUEPRINT,
                            describeOutput = false,
                        )

                    repository.updateLog(log.copy(suggestion = suggestionResult))
                } catch (e: Exception) {
                    e.printStackTrace()
                    repository.updateLog(log.copy(suggestion = "Failed to generate suggestion: ${e.message}"))
                }
            }

        override suspend fun generateGlobalInsight(logs: List<AIAuditLog>): Flow<RequestResult<String>> =
            flow {
                try {
                    val successfulLogs =
                        logs
                            .filter { it.status == "SUCCESS" && !it.blueprintKey.isNullOrEmpty() }
                            .take(3)

                    if (successfulLogs.isEmpty()) {
                        emit(
                            RequestResult.Error(Exception("No successful logs with blueprints found. Please generate some content first!")),
                        )
                        return@flow
                    }

                    val pipelineDataParts = mutableListOf<String>()
                    successfulLogs.forEach { log ->
                        val blueprint = promptService.buildRemotePrompt(log.blueprintKey!!)
                        val logNorm =
                            log.toAINormalize(listOf("id", "timestamp", "suggestion", "rawResponse"))
                        val blueprintNorm =
                            blueprint.toAINormalize(listOf("omitHeaders"))
                                ?: "Blueprint not found"
                        pipelineDataParts.add(
                            "BLUEPRINT: ${log.blueprintKey}\n$blueprintNorm\n\nLOG:\n$logNorm",
                        )
                    }

                    val pipelineContext = pipelineDataParts.joinToString("\n---\n")

                    val prompt =
                        promptService.buildRemotePrompt(
                            AuditLogPrompts.GLOBAL_PIPELINE_AUDIT_BLUEPRINT,
                            mapOf("pipelineData" to pipelineContext),
                        )

                    val insight =
                        gemmaClient.generate<String>(
                            prompt = prompt,
                            blueprintKey = AuditLogPrompts.GLOBAL_PIPELINE_AUDIT_BLUEPRINT,
                            describeOutput = false,
                        )

                    if (insight != null) {
                        emit(RequestResult.Success(insight))
                    } else {
                        emit(RequestResult.Error(Exception("Failed to generate insight")))
                    }
                } catch (e: Exception) {
                    emit(RequestResult.Error(e))
                }
            }
    }
