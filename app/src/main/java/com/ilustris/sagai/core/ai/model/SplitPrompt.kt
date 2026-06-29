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

/**
 * Deep-merges instruction bucket maps for the Split & Merge architecture.
 *
 * Shallow [putAll] drops earlier content when keys collide (`Persona`, `Directives`, `rules`, etc.),
 * which breaks layering task blueprints with genre/act conversation styles.
 */
fun mergeInstructionBuckets(
    base: Map<String, Any>,
    vararg extras: Map<String, Any>,
): Map<String, Any> = extras.fold(base) { acc, extra -> mergeInstructionBucketPair(acc, extra) }

internal fun mergeInstructionBucketPair(
    base: Map<String, Any>,
    extra: Map<String, Any>,
): Map<String, Any> {
    if (extra.isEmpty()) return base
    if (base.isEmpty()) return extra

    return buildMap {
        (base.keys + extra.keys).forEach { key ->
            put(key, mergeInstructionBucketValue(base[key], extra[key]))
        }
    }
}

private fun mergeInstructionBucketValue(
    left: Any?,
    right: Any?,
): Any =
    when {
        left == null -> {
            right!!
        }

        right == null -> {
            left
        }

        left is String && right is String -> {
            mergeInstructionText(left, right)
        }

        left is Map<*, *> && right is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            mergeInstructionBucketPair(left as Map<String, Any>, right as Map<String, Any>)
        }

        left is List<*> && right is List<*> -> {
            left + right
        }

        else -> {
            mergeInstructionText(left.toString(), right.toString())
        }
    }

private fun mergeInstructionText(
    base: String,
    extra: String,
): String =
    when {
        base.isBlank() -> extra.trim()
        extra.isBlank() -> base.trim()
        else -> "${base.trim()}\n\n${extra.trim()}"
    }

/** Merges extra instruction buckets into [instructionBuckets] for the Split & Merge API. */
fun SplitPrompt.mergeInstructions(vararg extras: Map<String, Any>): SplitPrompt =
    copy(
        instructionBuckets = mergeInstructionBuckets(renderInstructions(), *extras),
    )
