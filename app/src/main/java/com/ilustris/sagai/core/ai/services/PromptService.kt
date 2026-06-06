package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.PromptBlueprint
import com.ilustris.sagai.core.ai.prompts.PromptDirectives
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toPromptVariables
import timber.log.Timber
import javax.inject.Inject

interface PromptService {
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
                remoteConfigService.getJsonMapStringString("prompt_directives") ?: emptyMap(),
            )

        private fun buildPrompt(
            template: String,
            variables: Map<String, String>,
            logEnabled: Boolean,
            blueprint: String? = null,
        ): String {
            if (logEnabled) {
                Timber
                    .tag("PromptService")
                    .i("buildPrompt($blueprint): Received vars ->\n${variables.toJsonFormat()}")
            }

            val placeholders =
                Regex("\\{(\\w+)\\}").findAll(template).map { it.groupValues[1] }.toList()
            val uniquePlaceholders = placeholders.distinct()

            if (logEnabled) {
                Timber
                    .tag(
                        "PromptService",
                    ).i(
                        "buildPrompt($blueprint): Found ${placeholders.size} placeholders (${uniquePlaceholders.size} unique) in template: $uniquePlaceholders",
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
                    Timber
                        .tag("PromptService")
                        .e("buildPrompt($blueprint): CRITICAL - Variable '{$key}' not found in provided args!")
                }
            }

            if (logEnabled) {
                Timber
                    .tag("PromptService")
                    .d("buildPrompt($blueprint): Final Prompt Construction Complete.")
            }
            return result
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
                    appendLine(
                        buildPrompt(
                            blueprint.template,
                            variables,
                            logEnabled,
                            remoteConfigKey,
                        ),
                    )
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

                    // 4. Few-Shot Examples
                    if (blueprint.examples.isNotEmpty()) {
                        appendLine("# FEW-SHOT EXAMPLES")
                        blueprint.examples.forEachIndexed { index, example ->
                            appendLine("## EXAMPLE ${index + 1}")
                            appendLine(example.toJsonFormat())
                        }
                        appendLine()
                    }

                    // 5. The Core Template
                    appendLine("# TASK DEFINITION")
                    appendLine(
                        buildPrompt(
                            blueprint.template,
                            variables,
                            logEnabled,
                            remoteConfigKey,
                        ),
                    )
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
