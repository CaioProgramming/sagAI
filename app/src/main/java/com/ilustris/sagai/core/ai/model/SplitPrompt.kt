package com.ilustris.sagai.core.ai.model

import com.ilustris.sagai.core.database.model.AIStats
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.core.utils.toJsonFormat

/**
 * The result of splitting a [PromptBlueprint] into its static and dynamic parts.
 *
 * Produced by [com.ilustris.sagai.core.ai.services.PromptService.buildSplitBlueprint] and
 * consumed by [com.ilustris.sagai.core.ai.GemmaClient] to assemble the Gemini API request:
 *
 * - [instructionBuckets] → rendered as a single Markdown string and sent as the `system_instruction` field.
 * - [processedTemplate] → sent inside `contents` (user turn), alongside the user prompt.
 *
 * @param instructionBuckets Nested map of `category → (ruleKey → ruleContent)`.
 *   Categories are rendered as level-1 Markdown headers (e.g. `# IDENTITY`),
 *   and individual rules as level-2 headers (e.g. `## Persona`).
 * @param processedTemplate The blueprint `template` (and `examples`) after all `{key}` placeholders
 *   have been replaced with their runtime values.
 */
data class SplitPrompt(
    val blueprintKey: String? = null,
    private val instructionBuckets: Map<String, Any>,
    val processedTemplate: String,
    val sentVariables: Map<String, String> = emptyMap(),
    val missingVariables: List<String> = emptyList(),
) {
    /**
     * Renders all instruction buckets into a single Markdown-formatted string.
     */
    fun renderInstructions() = instructionBuckets.asMap()

    /**
     * Converts the split prompt data into [AIStats] for auditing and logging purposes.
     */
    fun getAIStats(): AIStats =
        AIStats(
            blueprintKey = blueprintKey,
            sentVariables = sentVariables,
            missingVariables = missingVariables,
            systemInstructions = renderInstructions().toJsonFormat(),
        )
}
