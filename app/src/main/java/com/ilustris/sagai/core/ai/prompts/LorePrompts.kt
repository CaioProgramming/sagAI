package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.home.data.model.SagaContent

data class LoreGenerationArgs(
    val sagaTitle: String,
    val loreUpdateLimit: String,
    val sagaContext: String,
    val storyContext: String,
    val newConversationBust: String,
    val conversationDirective: String,
)

object LorePrompts {
    const val LORE_GENERATION_BLUEPRINT = "lore_generation_blueprint"

    val LORE_OUTPUT_ONLY_FIELDS = listOf("emotionalReview")

    val LORE_CONTENT_FIELDS =
        listOf(
            "title",
            "content",
            "emotionalReview",
            "narrativeGuide",
            "emotionalTone",
        )

    val GENERATED_TIMELINE_EXCLUSIONS =
        listOf(
            "sceneSummary",
            "currentObjective",
        )

    val GENERATED_CHAPTER_EXCLUSIONS =
        listOf(
            "introduction",
            "featuredCharacters",
        )

    val GENERATED_ACT_EXCLUSIONS = listOf("introduction")

    private val CHARACTER_EXCLUDED_FIELDS =
        listOf(
            "id",
            "image",
            "hexColor",
            "sagaId",
            "details",
            "joinedAt",
        )

    val TIMELINE_EXCLUDED_FIELDS =
        listOf(
            "id",
            "timelineId",
            "createdAt",
            "chapterId",
            "sceneSummary",
            "currentObjective",
            "emotionalReview",
        )

    val CHAPTER_EXCLUDED_FIELDS =
        listOf(
            "id",
            "currentEventId",
            "sagaId",
            "actId",
            "featuredCharacters",
            "coverImage",
            "createdAt",
            "emotionalReview",
        )

    val ACT_EXCLUDED_FIELDS =
        listOf("id", "sagaId", "currentChapterId", "createdAt", "emotionalReview")

    fun storyContext(
        sagaContent: SagaContent,
        rules: NarrativeRules,
    ) = buildString {
        sagaContent.currentActInfo?.let {
            appendLine("Current story context: ")
            it.actSummary(showEvents = true)
        }

        val previousEvents =
            sagaContent.acts.filter { it.isComplete(rules) && it != sagaContent.currentActInfo }
        if (previousEvents.isNotEmpty()) {
            appendLine("Previous events: ")
            previousEvents.forEach {
                it.actSummary(showEvents = false)
            }
        }
    }
}
