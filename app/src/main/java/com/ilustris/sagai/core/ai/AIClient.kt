package com.ilustris.sagai.core.ai

import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.GemmaClient.Companion.CORE_FLAG
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import com.ilustris.sagai.core.services.AgeVerificationService
import com.ilustris.sagai.core.services.RemoteConfigService
import timber.log.Timber
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Locale

enum class ModelRequirement {
    MINIMAL,
    LOW,
    MEDIUM,
    HIGH,
}

/**
 * Maps a tier onto a `thinkingLevel` the Gemini API actually accepts.
 *
 * The API's values are lowercase `low`/`medium`/`high`, so the enum name can't be sent raw —
 * it only ever looked harmless because Gemma ignores `thinkingConfig` outright, which meant an
 * invalid level went unnoticed until a tier was pointed at a Gemini 3 model.
 *
 * [ModelRequirement.MINIMAL] maps down to `low` rather than `minimal`: not every Gemini 3 model
 * offers the minimal level, and asking for a level a model doesn't have is how this broke in the
 * first place.
 */
fun ModelRequirement.toGeminiThinkingLevel(): String =
    when (this) {
        ModelRequirement.MINIMAL, ModelRequirement.LOW -> "low"
        ModelRequirement.MEDIUM -> "medium"
        ModelRequirement.HIGH -> "high"
    }

abstract class AIClient(
    protected val remoteConfigService: RemoteConfigService,
    protected val promptService: PromptService,
    protected val ageVerificationService: AgeVerificationService,
    @PublishedApi internal val aiAuditLogDao: AIAuditLogDao,
) {
    fun getLanguage(requireTranslation: Boolean = true): String {
        val locale = if (requireTranslation) Locale.getDefault() else Locale.US
        val languageName = locale.getDisplayName(locale)
        return languageName.ifBlank { locale.toLanguageTag() }
    }

    fun buildDataStructure(
        requirement: ModelRequirement,
        describeOutput: Boolean,
        type: Type,
        filterOutputFields: List<String>,
    ): Pair<String, String> {
        val typeName = validateType(type)
        val structure = validateStructure(describeOutput, filterOutputFields, typeName, type)

        return typeName to structure
    }

    fun validateStructure(
        describeOutput: Boolean,
        filterOutputFields: List<String>,
        dataTypeName: String,
        type: Type,
    ): String =
        if (describeOutput) {
            buildAIPromptOutputStructure(type, filterOutputFields)
        } else {
            dataTypeName
        }

    fun validateType(dataType: Type): String =
        when (dataType) {
            is Class<*> -> {
                dataType.simpleName
            }

            is ParameterizedType -> {
                (dataType.rawType as? Class<*>)?.simpleName
                    ?: dataType.toString()
            }

            else -> {
                dataType.toString().substringAfterLast(".")
            }
        }



    suspend fun buildCorePrompt(
        requirement: ModelRequirement,
        requireTranslation: Boolean,
        dataTypeName: String,
        structure: String,
    ): SplitPrompt {
        val userAge = ageVerificationService.getUserAgeGroup()
        return promptService.buildSplitBlueprint(
            remoteConfigKey = getCoreBlueprintKey(requirement),
            variables =
                mapOf(
                    "type" to dataTypeName,
                    "structure" to structure,
                    "language" to getLanguage(requireTranslation),
                    "userAge" to userAge.name,
                ),
            logEnabled = false,
        )
    }

    suspend fun buildCoreInstructions(
        requirement: ModelRequirement,
        requireTranslation: Boolean,
        dataTypeName: String,
        structure: String,
    ) = buildMap {
        val config = buildCorePrompt(requirement, requireTranslation, dataTypeName, structure)
        put("core", config.renderInstructions())
        put("structure", config.processedTemplate)
    }

    suspend fun buildUnifiedInstructions(
        requirement: ModelRequirement,
        requireTranslation: Boolean,
        dataTypeName: String,
        outputStructure: String,
        userInteraction: Boolean,
        blueprintInstructions: Map<String, Any>,
    ): Map<String, Any> {
        val coreInstructions =
            buildCoreInstructions(requirement, requireTranslation, dataTypeName, outputStructure)
        val safetyInstructions =
            if (userInteraction) {
                buildSafetyInstructions()
            } else {
                emptyMap()
            }

        return buildMap {
            put(
                "CoreDefinitions",
                buildMap {
                    putAll(coreInstructions)
                    if (safetyInstructions.isNotEmpty()) {
                        put("SafetyGuardRails", safetyInstructions)
                    }
                },
            )
            if (blueprintInstructions.isNotEmpty()) {
                put("TaskInstructions", blueprintInstructions)
            }
        }
    }

    suspend fun buildSafetyInstructions(): Map<String, Any> {
        val userAge = ageVerificationService.getUserAgeGroup()
        val safetyPrompt =
            promptService.buildSplitBlueprint(
                "safety_guardrails_blueprint",
                mapOf(
                    "userAge" to userAge.name,
                ),
            )
        return buildMap {
            putAll(safetyPrompt.renderInstructions())
            put("SafetyVerification", safetyPrompt.processedTemplate)
        }
    }

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

    fun getCoreBlueprintKey(requirement: ModelRequirement): String =
        if (requirement == ModelRequirement.HIGH) {
            "core_blueprint"
        } else {
            "core_${requirement.name.lowercase()}_blueprint"
        }

    /**
     * The `thinkingLevel` to send for [requirement], or null when the tier has thinking switched
     * off in `model_configs` — in which case no `thinkingConfig` is sent at all.
     *
     * `thinkingEnabled` has been in `model_configs` all along but nothing ever read it, so every
     * tier requested thoughts regardless of its own configuration. Defaults to enabled when the
     * key is absent, matching the behaviour that shipped.
     */
    suspend fun thinkingLevel(requirement: ModelRequirement): String? {
        val tierConfig =
            remoteConfigService.getJsonMapStringAny("model_configs") ?: emptyMap()
        val enabled =
            (tierConfig[requirement.name] as? Map<*, *>)
                ?.get("thinkingEnabled") as? Boolean
                ?: true
        return requirement.toGeminiThinkingLevel().takeIf { enabled }
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

    suspend fun coreKey() =
        remoteConfigService.getString(CORE_FLAG, false)?.let {
            it.ifEmpty {
                error("Couldn't fetch gemma Model")
            }
        } ?: error("Couldn't get Flag value")

    suspend fun apiConfig(useCore: Boolean): String =
        if (useCore) {
            coreKey()
        } else {
            remoteConfigService.getString(KEY_FLAG, false)?.ifEmpty {
                error("Couldn't fetch firebase key")
            } ?: error("Flag Value unavailable.")
        }

    @PublishedApi
    internal fun assembleGeminiRequest(block: GeminiRequestBuilder.() -> Unit): GeminiRequestAssembly = geminiRequest(block)

    @PublishedApi
    internal suspend fun recordAudit(
        snapshot: AIAuditSnapshot,
        logEnabled: Boolean = true,
    ) {
        if (!BuildConfig.DEBUG || !logEnabled) return
        persistAuditLog(snapshot.toEntity())
    }

    @PublishedApi
    internal suspend fun persistAuditLog(log: AIAuditLog) {
        try {
            aiAuditLogDao.insertLog(log)
        } catch (e: Exception) {
            Timber.tag(javaClass.simpleName).e("Error saving audit log: ${e.message}")
        }
    }
}

val AI_EXCLUDED_FIELDS =
    listOf(
        "text\$delegate",
        "functionResponse\$delegate",
        "functionCall\$delegate",
        "functionCalls\$delegate",
        "\"inlineDataParts\$delegate",
    )
