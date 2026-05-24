package com.ilustris.sagai.core.ai

import com.google.gson.Gson
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.GemmaClient.ModelRequirement
import com.ilustris.sagai.core.ai.model.AIGeneration
import com.ilustris.sagai.core.ai.model.AgeGroup
import com.ilustris.sagai.core.ai.model.GeminiContent
import com.ilustris.sagai.core.ai.model.GeminiGenerationConfig
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.SafeGuard
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import com.ilustris.sagai.core.network.GeminiApiClient
import com.ilustris.sagai.core.services.AgeVerificationService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.sanitizeAndExtractJsonString
import com.ilustris.sagai.core.utils.toJsonMap
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafetyClient
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
        private val ageVerificationService: AgeVerificationService,
        private val promptService: PromptService,
        private val geminiApiClient: GeminiApiClient,
        private val aiAuditLogDao: AIAuditLogDao,
    ) : AIClient() {
        suspend fun checkSafety(userInput: String): SafeGuard {
            val startTime = System.currentTimeMillis()
            val userAge = ageVerificationService.getUserAgeGroup()
            val blueprintKey = "safety_guardrails_blueprint"

            return try {
                val dataStructure = toJsonMap(SafeGuard::class.java)
                val structure =
                    toJsonMap(
                        AIGeneration::class.java,
                        fieldCustomDescriptions = listOf("data" to dataStructure),
                    )

                val prompt =
                    promptService.buildRemotePrompt(
                        blueprintKey,
                        mapOf(
                            "userAge" to userAge.name,
                            "userInput" to userInput,
                            "formattingRule" to "Respond ONLY with a JSON with this structure: $structure",
                        ),
                    )

                val modelName = modelName(ModelRequirement.MEDIUM)
                val apiKey = getApiKey()

                val request =
                    GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                        GeminiGenerationConfig(),
                    )

                val response = geminiApiClient.generateContent(modelName, apiKey, request)
                val responseText =
                    response.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.lastOrNull()
                        ?.text

                val sanitizedJson = responseText.sanitizeAndExtractJsonString(AIGeneration::class.java)
                val result =
                    parseAIGenerationFromJson<SafeGuard>(Gson(), sanitizedJson)

                val safetyResult = result.data
                val duration = System.currentTimeMillis() - startTime

                // Log to Audit
                if (BuildConfig.DEBUG) {
                    aiAuditLogDao.insertLog(
                        AIAuditLog(
                            model = modelName,
                            blueprintKey = "SAFETY_GATE",
                            dataType = "SafeGuard",
                            status = "SUCCESS",
                            safetyStatus = safetyResult.name,
                            reasoning = result.reasoning,
                            rawResponse = responseText,
                            responseTime = duration,
                        ),
                    )
                }

                Timber
                    .tag("SafetyClient")
                    .i("Age group: ${userAge.name} safeGuard result: $safetyResult")

                if (userAge == AgeGroup.ADULT && safetyResult == SafeGuard.AGE_RESTRICTED) {
                    return SafeGuard.OK
                }

                safetyResult
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                Timber.tag("SafetyClient").e(e, "Safety check failed. Defaulting to OK.")

                aiAuditLogDao.insertLog(
                    AIAuditLog(
                        model = "UNKNOWN",
                        blueprintKey = "SAFETY_GATE",
                        dataType = "SafeGuard",
                        status = "ERROR",
                        errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                        responseTime = duration,
                    ),
                )
                SafeGuard.OK
            }
        }

        suspend fun modelName(requirement: ModelRequirement): String {
            val tierConfig =
                remoteConfigService.getJsonMapStringAny("model_configs") ?: emptyMap()
            return when (val config = tierConfig[requirement.name]) {
                is String -> {
                    config.replace("models/", "")
                }

                is Map<*, *> -> {
                    val enabled = config["enabled"] as? Boolean ?: true
                    if (!enabled) {
                        throw ModelOutageException(
                            requirement,
                            config["model"] as? String ?: "UNKNOWN",
                        )
                    }
                    val model =
                        config["model"] as? String
                            ?: error("Model name not found in config for ${requirement.name}")
                    model.replace("models/", "")
                }

                else -> {
                    Timber.e("Invalid model configuration for ${requirement.name}: $config")
                    error("Invalid model configuration for ${requirement.name}")
                }
            }
        }

        private suspend fun getApiKey(): String = remoteConfigService.getString(GemmaClient.CORE_FLAG, false)!!
    }
