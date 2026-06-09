package com.ilustris.sagai.core.ai

import com.google.gson.Gson
import com.ilustris.sagai.BuildConfig
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
import com.ilustris.sagai.core.utils.findJsonContent
import com.ilustris.sagai.core.utils.sanitizeAndExtractJsonString
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafetyClient
    @Inject
    constructor(
        remoteConfig: RemoteConfigService,
        private val ageVerificationService: AgeVerificationService,
        promptService: PromptService,
        private val geminiApiClient: GeminiApiClient,
        private val aiAuditLogDao: AIAuditLogDao,
    ) : AIClient(remoteConfig, promptService) {
        suspend fun checkSafety(userInput: String): SafeGuard {
            val startTime = System.currentTimeMillis()
            val userAge = ageVerificationService.getUserAgeGroup()
            val blueprintKey = "safety_guardrails_blueprint"

            return try {
                val type = getJavaType<SafeGuard>()
                val dataStructure =
                    buildDataStructure(
                        ModelRequirement.TINY,
                        false,
                        type,
                        listOf("reasoning"),
                    )
                val coreInstructions =
                    buildCoreInstructions(
                        ModelRequirement.TINY,
                        true,
                        dataStructure.first,
                        dataStructure.second,
                    )

                val prompt =
                    promptService.buildSplitBlueprint(
                        blueprintKey,
                        mapOf(
                            "userAge" to userAge.name,
                            "userInput" to userInput,
                        ),
                    )

                val modelName = modelName(ModelRequirement.TINY)
                val apiKey = getApiKey()

                val instructions =
                    buildMap {
                        putAll(coreInstructions)
                        putAll(prompt.renderInstructions())
                    }
                val request =
                    GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt.processedTemplate)))),
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = instructions.toAINormalize()))),
                        generationConfig = GeminiGenerationConfig(),
                    )

                val response = geminiApiClient.generateContent(modelName, apiKey, request)
                val candidate = response.candidates?.firstOrNull()

                if (candidate?.finishReason == "SAFETY" || candidate?.finishReason == "OTHER") {
                    Timber
                        .tag("SafetyClient")
                        .w("API blocked content with reason: ${candidate.finishReason}")
                    return SafeGuard.BLOCKED
                }

                val responseParts = candidate?.content?.parts
                val nativeThoughts =
                    responseParts
                        ?.filter { it.thought == true }
                        ?.joinToString("\n") { it.text.orEmpty() }

                // Use intelligent JSON locator that searches across all parts
                val (responseText, _) = responseParts.findJsonContent()

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
                            blueprintKey = prompt.blueprintKey,
                            dataType = "SafeGuard",
                            status = "SUCCESS",
                            safetyStatus = safetyResult.name,
                            reasoning = nativeThoughts,
                            rawResponse = responseText,
                            responseTime = duration,
                            systemInstruction = instructions.toJsonFormat(),
                            sentVariables = prompt.sentVariables.toJsonFormat(),
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

        private suspend fun getApiKey(): String = remoteConfigService.getString(GemmaClient.CORE_FLAG, false)!!
    }
