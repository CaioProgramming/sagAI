package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.PromptBlueprint
import com.ilustris.sagai.core.ai.prompts.PromptDirectives
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toPromptVariables
import timber.log.Timber
import javax.inject.Inject

interface PromptService {
    /**
     * Replaces `{key}` placeholders in the [template] with the corresponding values from the [variables] map.
     * Use this when you already have the template string in hand (e.g. from a local config object).
     */
    fun buildPrompt(
        template: String,
        variables: Map<String, String>,
        logEnabled: Boolean = true,
    ): String

    /**
     * Converts a Data Class to a Map<String, String> and injects its properties into the [template].
     * Use this when you already have the template string in hand.
     */
    fun <T : Any> buildPrompt(
        template: String,
        variablesDataClass: T,
        logEnabled: Boolean = true,
    ): String

    /**
     * Fetches a template from Remote Config by [remoteConfigKey] and replaces `{key}` placeholders
     * with the corresponding values from the [variables] map.
     */
    suspend fun buildRemotePrompt(
        remoteConfigKey: String,
        variables: Map<String, String> = emptyMap(),
        logEnabled: Boolean = true,
    ): String

    /**
     * Fetches a template from Remote Config by [remoteConfigKey] and injects the data class properties.
     */
    suspend fun <T : Any> buildRemotePrompt(
        remoteConfigKey: String,
        variablesDataClass: T,
        logEnabled: Boolean = true,
    ): String

    suspend fun getPromptDirectives(): PromptDirectives
}

class PromptServiceImpl
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
    ) : PromptService {
        override suspend fun getPromptDirectives(): PromptDirectives =
            PromptDirectives(
                remoteConfigService.getJson<Map<String, String>>("prompt_directives") ?: emptyMap(),
            )

        override fun buildPrompt(
            template: String,
            variables: Map<String, String>,
            logEnabled: Boolean,
        ): String {
            if (logEnabled) {
                Timber
                    .tag("PromptService")
                    .i("buildPrompt: Received vars ->\n${variables.toJsonFormat()}")
            }

            val placeholders =
                Regex("\\{(\\w+)\\}").findAll(template).map { it.groupValues[1] }.toList()
            val uniquePlaceholders = placeholders.distinct()

            if (logEnabled) {
                Timber
                    .tag(
                        "PromptService",
                    ).i(
                        "buildPrompt: Found ${placeholders.size} placeholders (${uniquePlaceholders.size} unique) in template: $uniquePlaceholders",
                    )
            }

            var result = template

            uniquePlaceholders.forEach { key ->
                val value = variables[key]
                if (value != null) {
                    result = result.replace("{$key}", value)
                    if (logEnabled) {
                        Timber.tag("PromptService").d("buildPrompt: Replaced {$key}")
                    }
                } else {
                    Timber.tag("PromptService").e("buildPrompt: CRITICAL - Variable '{$key}' not found in provided args!")
                }
            }

            if (logEnabled) {
                Timber.tag("PromptService").d("buildPrompt: Final Prompt Construction Complete.")
            }
            return result
        }

        override fun <T : Any> buildPrompt(
            template: String,
            variablesDataClass: T,
            logEnabled: Boolean,
        ): String {
            val stringMap = variablesDataClass.toPromptVariables()
            Timber
                .tag(
                    "PromptService",
                ).d("buildPrompt: Converted ${variablesDataClass::class.java.simpleName} to Map with ${stringMap.size} keys")
            return buildPrompt(template, stringMap, logEnabled)
        }

        override suspend fun buildRemotePrompt(
            remoteConfigKey: String,
            variables: Map<String, String>,
            logEnabled: Boolean,
        ): String {
            val blueprint =
                remoteConfigService.getJson<PromptBlueprint>(remoteConfigKey, logEnabled)!!

            if (logEnabled) {
                Timber.tag("PromptService").d("buildRemotePrompt: Found Blueprint for '$remoteConfigKey'")
            }
            if (blueprint.template.isBlank()) {
                throw IllegalStateException(
                    "Prompt template not found for Remote Config key: $remoteConfigKey",
                )
            }

            return buildString {
                if (blueprint.omitHeaders) {
                    if (blueprint.role.isNotBlank()) appendLine(blueprint.role)
                    if (blueprint.directives.isNotEmpty()) {
                        blueprint.directives.values.forEach { appendLine(it) }
                    }
                    if (blueprint.rules.isNotEmpty()) {
                        blueprint.rules.values.forEach { appendLine(it) }
                    }
                    appendLine(buildPrompt(blueprint.template, variables, logEnabled))
                } else {
                    // 1. Identity
                    if (blueprint.role.isNotBlank()) {
                        appendLine("# IDENTITY")
                        appendLine(blueprint.role)
                        appendLine()
                    }

                    // 2. Local Governance (Directives)
                    if (blueprint.directives.isNotEmpty()) {
                        appendLine("# MODULE DIRECTIVES")
                        blueprint.directives.forEach { (key, value) ->
                            appendLine("## $key")
                            appendLine(value)
                        }
                        appendLine()
                    }

                    // 3. Narrative Rules
                    if (blueprint.rules.isNotEmpty()) {
                        appendLine("# RULES")
                        blueprint.rules.forEach { (key, value) ->
                            appendLine("## $key")
                            appendLine(value)
                        }
                        appendLine()
                    }

                    // 4. The Core Template
                    appendLine("# TASK DEFINITION")
                    appendLine(buildPrompt(blueprint.template, variables, logEnabled))
                }
            }.trimIndent()
        }

        override suspend fun <T : Any> buildRemotePrompt(
            remoteConfigKey: String,
            variablesDataClass: T,
            logEnabled: Boolean,
        ): String {
            val stringMap = variablesDataClass.toPromptVariables()
            return buildRemotePrompt(remoteConfigKey, stringMap, logEnabled)
        }
    }
