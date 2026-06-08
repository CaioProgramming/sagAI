package com.ilustris.sagai.features.settings.domain.audit.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.model.PromptBlueprint
import com.ilustris.sagai.core.ai.prompts.AuditLogPrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.settings.domain.audit.repository.AIAuditLogRepository
import kotlinx.coroutines.flow.Flow
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

        override suspend fun getRecentLogs(): Flow<List<AIAuditLog>> = repository.getRecentLogs()

        override suspend fun generateSuggestion(log: AIAuditLog): RequestResult<Unit> =
            executeRequest {
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
                        "pipelineData" to log.toAINormalize(logExclusions),
                    )

                val prompt =
                    promptService.buildSplitBlueprint(
                        AuditLogPrompts.AUDIT_LOG_SUGGESTION_BLUEPRINT,
                        promptArgs,
                    )

                val suggestionResult =
                    gemmaClient.generate<String>(
                        prompt,
                    )

                repository.updateLog(log.copy(suggestion = suggestionResult))
            }

        override suspend fun generateGlobalInsight(logs: List<AIAuditLog>) =
            executeRequest {
                val successfulLogs =
                    logs
                        .filter { it.status != "ERROR" }
                        .distinctBy { it.blueprintKey }
                        .map {
                            it.blueprintKey to remoteConfigService.getJson<PromptBlueprint>(it.blueprintKey)
                        }.filter {
                            it.first != null && it.second != null
                        }

                if (successfulLogs.isEmpty()) {
                    error("No successful logs with blueprints found. Please generate some content first!")
                }
                val pipelineContext = successfulLogs.normalizetoAIItems()

                val prompt =
                    promptService.buildSplitBlueprint(
                        AuditLogPrompts.GLOBAL_PIPELINE_AUDIT_BLUEPRINT,
                        mapOf("pipelineData" to pipelineContext),
                    )

                gemmaClient.generate<String>(
                    prompt,
                    describeOutput = false,
                )!!
            }
    }
