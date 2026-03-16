package com.ilustris.sagai.core.ai.services

import android.util.Log
import com.ilustris.sagai.core.ai.model.PromptBlueprint
import com.ilustris.sagai.core.ai.prompts.PromptDirectives
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.toPromptVariables
import javax.inject.Inject

interface PromptService {
    /**
     * Replaces `{key}` placeholders in the [template] with the corresponding values from the [variables] map.
     * Use this when you already have the template string in hand (e.g. from a local config object).
     */
    fun buildPrompt(
        template: String,
        variables: Map<String, String>,
    ): String

    /**
     * Converts a Data Class to a Map<String, String> and injects its properties into the [template].
     * Use this when you already have the template string in hand.
     */
    fun <T : Any> buildPrompt(
        template: String,
        variablesDataClass: T,
    ): String

    /**
     * Fetches a template from Remote Config by [remoteConfigKey] and replaces `{key}` placeholders
     * with the corresponding values from the [variables] map.
     */
    suspend fun buildRemotePrompt(
        remoteConfigKey: String,
        variables: Map<String, String>,
    ): String

    /**
     * Fetches a template from Remote Config by [remoteConfigKey] and injects the data class properties.
     */
    suspend fun <T : Any> buildRemotePrompt(
        remoteConfigKey: String,
        variablesDataClass: T,
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
        ): String {
            var result = template
            Log.d(javaClass.simpleName, "buildPrompt: Received args ->\n$variables")
            variables.forEach { (key, value) ->
                if (result.contains("{$key}")) {
                    Log.d("PromptService", "buildPrompt: Replaced {$key}")
                    result = result.replace("{$key}", value)
                }
            }
            Log.d("PromptService", "buildPrompt: New Prompt ->\n$result")
            return result
        }

        override fun <T : Any> buildPrompt(
            template: String,
            variablesDataClass: T,
        ): String {
            val stringMap = variablesDataClass.toPromptVariables()
            Log.d(
                "PromptService",
                "buildPrompt: Converted ${variablesDataClass::class.java.simpleName} to Map with ${stringMap.size} keys",
            )
            return buildPrompt(template, stringMap)
        }

        override suspend fun buildRemotePrompt(
            remoteConfigKey: String,
            variables: Map<String, String>,
        ): String {
            val blueprint =
                remoteConfigService.getJson<PromptBlueprint>(remoteConfigKey)!!

            Log.d("PromptService", "buildRemotePrompt: Found Blueprint for '$remoteConfigKey'")
            if (blueprint.template.isBlank()) {
                throw IllegalStateException(
                    "Prompt template not found for Remote Config key: $remoteConfigKey",
                )
            }

            return buildString {
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
                    appendLine("# MODULE RULES")
                    blueprint.rules.forEach { (key, value) ->
                        appendLine("## $key")
                        appendLine(value)
                    }
                    appendLine()
                }

                // 4. The Core Template
                appendLine("# TASK DEFINITION")
                appendLine(buildPrompt(blueprint.template, variables))
            }.trimIndent()
        }

        override suspend fun <T : Any> buildRemotePrompt(
            remoteConfigKey: String,
            variablesDataClass: T,
        ): String {
            val stringMap = variablesDataClass.toPromptVariables()
            return buildRemotePrompt(remoteConfigKey, stringMap)
        }
    }
