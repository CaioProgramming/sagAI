package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.PromptBlueprint
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.prompts.PromptDirectives
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import timber.log.Timber
import javax.inject.Inject

interface PromptService {
    @Deprecated(
        message = "Use buildSplitBlueprint for auditability and Split & Merge architecture",
        replaceWith =
            ReplaceWith(
                expression = "buildSplitBlueprint(remoteConfigKey, variables, logEnabled)",
                imports = ["com.ilustris.sagai.core.ai.services.PromptService"],
            ),
    )
    suspend fun buildRemotePrompt(
        remoteConfigKey: String,
        variables: Map<String, String> = emptyMap(),
        logEnabled: Boolean = true,
    ): String

    /**
     * Fetches a template from Remote Config by [remoteConfigKey] and injects the data class properties.
     */
    @Deprecated(
        message = "Use buildSplitBlueprint for auditability and Split & Merge architecture",
        replaceWith =
            ReplaceWith(
                expression = "buildSplitBlueprint(remoteConfigKey, variablesDataClass, logEnabled)",
                imports = ["com.ilustris.sagai.core.ai.services.PromptService"],
            ),
    )
    suspend fun <T : Any> buildRemotePrompt(
        remoteConfigKey: String,
        variablesDataClass: T,
        logEnabled: Boolean = true,
    ): String

    /**
     * Fetches a [PromptBlueprint] from Remote Config and splits it into static instruction buckets
     * and a processed dynamic template, ready for the **"Split & Merge"** architecture.
     *
     * - Static fields (`role`, `directives`, `rules`, `instructions`) are promoted to
     *   [SplitPrompt.instructionBuckets] with **no placeholder substitution**.
     * - Dynamic fields (`template`, `examples`) are processed with `{key}` substitution using
     *   the provided [variables] map, and returned as [SplitPrompt.processedTemplate].
     *
     * @param remoteConfigKey Remote Config key pointing to a [PromptBlueprint] JSON.
     * @param variables Map of `{placeholder}` values used only in the template and examples.
     * @param logEnabled Whether to emit Timber logs for variable resolution.
     */
    suspend fun buildSplitBlueprint(
        remoteConfigKey: String,
        variables: Map<String, String> = emptyMap(),
        logEnabled: Boolean = false,
    ): SplitPrompt

    suspend fun buildSplitBlueprint(
        remoteConfigKey: String,
        variables: Any,
        logEnabled: Boolean = false,
    ): SplitPrompt

    suspend fun getPromptDirectives(): PromptDirectives

    suspend fun fetchBlueprintData(
        remoteConfigKey: String,
        logEnabled: Boolean = false,
    ): PromptBlueprint
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

        override suspend fun fetchBlueprintData(
            remoteConfigKey: String,
            logEnabled: Boolean,
        ): PromptBlueprint = remoteConfigService.getJson<PromptBlueprint>(remoteConfigKey, logEnabled)!!

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

        @Deprecated(
            message = "Use buildSplitBlueprint for auditability and Split & Merge architecture",
            replaceWith =
                ReplaceWith(
                    expression = "buildSplitBlueprint(remoteConfigKey, variables, logEnabled)",
                    imports = ["com.ilustris.sagai.core.ai.services.PromptService"],
                ),
        )
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

        @Deprecated(
            message = "Use buildSplitBlueprint for auditability and Split & Merge architecture",
            replaceWith =
                ReplaceWith(
                    expression = "buildSplitBlueprint(remoteConfigKey, variablesDataClass, logEnabled)",
                    imports = ["com.ilustris.sagai.core.ai.services.PromptService"],
                ),
        )
        override suspend fun <T : Any> buildRemotePrompt(
            remoteConfigKey: String,
            variablesDataClass: T,
            logEnabled: Boolean,
        ): String {
            val stringMap = variablesDataClass.asMap()
            return buildRemotePrompt(remoteConfigKey, stringMap, logEnabled)
        }

        override suspend fun buildSplitBlueprint(
            remoteConfigKey: String,
            variables: Map<String, String>,
            logEnabled: Boolean,
        ): SplitPrompt {
            val blueprint =
                remoteConfigService.getJson<PromptBlueprint>(remoteConfigKey, logEnabled)!!

            if (logEnabled) {
                Timber
                    .tag("PromptService")
                    .d("buildSplitBlueprint: Found Blueprint for '$remoteConfigKey'")
            }

            // --- Build instruction buckets (static — no placeholder substitution) ---
            val buckets = mutableMapOf<String, Any>()

            // Promote legacy fields into named buckets
            if (blueprint.role.isNotBlank()) {
                buckets["Persona"] = blueprint.role
            }
            if (blueprint.directives.isNotEmpty()) {
                buckets["Directives"] =
                    buildMap {
                        putAll(blueprint.directives)
                    }.toAINormalize()
            }

            if (blueprint.examples.isNotEmpty()) {
                buckets["examples"] = blueprint.examples.normalizetoAIItems(describeName = false)
            }

            if (blueprint.rules.isNotEmpty()) {
                buckets["rules"] =
                    buildMap {
                        putAll(blueprint.rules)
                    }.toAINormalize()
            }

            // Merge extra instruction buckets defined in Remote Config (these can override legacy ones)

            blueprint.instructions?.let {
                buckets.putAll(it)
            }

            // --- Build processed template (dynamic — placeholder substitution applied) ---
            val processedTemplate =
                buildString {
                    if (blueprint.template.isNotBlank()) {
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

            val placeholders =
                Regex("\\{(\\w+)\\}").findAll(blueprint.template).map { it.groupValues[1] }.toList()
            val missingVariables = placeholders.filter { it !in variables }

            return SplitPrompt(
                blueprintKey = remoteConfigKey,
                instructionBuckets = buckets,
                processedTemplate = processedTemplate,
                sentVariables = variables,
                missingVariables = missingVariables,
            )
        }

        override suspend fun buildSplitBlueprint(
            remoteConfigKey: String,
            variables: Any,
            logEnabled: Boolean,
        ): SplitPrompt {
            val blueprint =
                remoteConfigService.getJson<PromptBlueprint>(remoteConfigKey, logEnabled)!!

            if (logEnabled) {
                Timber
                    .tag("PromptService")
                    .d("buildSplitBlueprint: Found Blueprint for '$remoteConfigKey'")
            }

            // --- Build instruction buckets (static — no placeholder substitution) ---
            val buckets = mutableMapOf<String, Any>()

            // Promote legacy fields into named buckets
            if (blueprint.role.isNotBlank()) {
                buckets.putAll(mapOf("Persona" to blueprint.role))
            }
            if (blueprint.directives.isNotEmpty()) {
                buckets.putAll(blueprint.directives.asMap())
            }
            if (blueprint.rules.isNotEmpty()) {
                buckets.putAll(blueprint.rules.asMap())
            }

            // Merge extra instruction buckets defined in Remote Config (these can override legacy ones)

            blueprint.instructions?.let {
                buckets.putAll(it)
            }

            // --- Build processed template (dynamic — placeholder substitution applied) ---
            val processedTemplate =
                buildString {
                    if (blueprint.examples.isNotEmpty()) {
                        appendLine("# FEW-SHOT EXAMPLES")
                        blueprint.examples.forEachIndexed { index, example ->
                            appendLine("## EXAMPLE ${index + 1}")
                            appendLine(example.toJsonFormat())
                        }
                        appendLine()
                    }

                    if (blueprint.template.isNotBlank()) {
                        appendLine("# TASK DEFINITION")
                        appendLine(
                            buildPrompt(
                                blueprint.template,
                                variables.asMap(),
                                logEnabled,
                                remoteConfigKey,
                            ),
                        )
                    }
                }.trimIndent()

            val placeholders =
                Regex("\\{(\\w+)\\}").findAll(blueprint.template).map { it.groupValues[1] }.toList()
            val missingVariables = placeholders.filter { it !in variables.asMap() }

            return SplitPrompt(
                blueprintKey = remoteConfigKey,
                instructionBuckets = buckets,
                processedTemplate = processedTemplate,
                sentVariables = variables.asMap(),
                missingVariables = missingVariables,
            )
        }
    }
