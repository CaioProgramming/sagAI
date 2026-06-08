package com.ilustris.sagai.core.ai

import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.GenerationConfig
import com.ilustris.sagai.core.ai.GemmaClient.Companion.CORE_FLAG
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.services.RemoteConfigService
import timber.log.Timber
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Locale

enum class ModelRequirement {
    TINY,
    LOW,
    MEDIUM,
    HIGH,
}

abstract class AIClient(
    protected val remoteConfigService: RemoteConfigService,
    protected val promptService: PromptService,
) {
    open suspend fun buildModel(generationConfig: GenerationConfig): GenerativeModel? = null

    fun getLanguage(requireTranslation: Boolean = true): String {
        val locale = if (requireTranslation) Locale.getDefault() else Locale.US
        val languageName = locale.getDisplayName(locale)
        return languageName.ifBlank { locale.toLanguageTag() }
    }

    fun buildDataStructure(
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
    ): SplitPrompt =
        promptService.buildSplitBlueprint(
            remoteConfigKey = getCoreBlueprintKey(requirement),
            variables =
                mapOf(
                    "language" to getLanguage(requireTranslation),
                    "type" to dataTypeName,
                    "structure" to structure,
                ),
            logEnabled = false,
        )

    suspend fun buildCoreInstructions(
        requirement: ModelRequirement,
        requireTranslation: Boolean,
        dataTypeName: String,
        structure: String,
    ) = buildMap {
        val config = buildCorePrompt(requirement, requireTranslation, dataTypeName, structure)
        put("task", config.processedTemplate)
        putAll(config.renderInstructions())
    }

    fun getCoreBlueprintKey(requirement: ModelRequirement): String =
        if (requirement == ModelRequirement.HIGH) {
            "core_blueprint"
        } else {
            "core_${requirement.name.lowercase()}_blueprint"
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
}

val AI_EXCLUDED_FIELDS =
    listOf(
        "text\$delegate",
        "functionResponse\$delegate",
        "functionCall\$delegate",
        "functionCalls\$delegate",
        "\"inlineDataParts\$delegate",
    )
