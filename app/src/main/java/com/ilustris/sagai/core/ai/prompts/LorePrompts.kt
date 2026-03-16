package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

data class LoreGenerationArgs(
    val sagaTitle: String,
    val loreUpdateLimit: String,
    val sagaMainContext: String,
    val storyContext: String,
    val conversationHistory: String,
    val conversationDirective: String,
)

object LorePrompts {
    private val SAGA_EXCLUDED_FIELDS =
        listOf(
            "id",
            "icon",
            "createdAt",
            "mainCharacterId",
            "isDebug",
            "endMessage",
            "currentActId",
            "endedAt",
            "review",
            "emotionalReview",
            "isEnded",
        )

    private val CHARACTER_EXCLUDED_FIELDS =
        listOf(
            "id",
            "image",
            "hexColor",
            "sagaId",
            "details",
            "joinedAt",
        )

    private val TIMELINE_EXCLUDED_FIELDS =
        listOf("id", "timelineId", "emotionalReview", "createdAt", "chapterId")

    private val CHAPTER_EXCLUDED_FIELDS =
        listOf(
            "id",
            "currentEventId",
            "sagaId",
            "actId",
            "featuredCharacters",
            "coverImage",
            "emotionalReview",
            "createdAt",
        )

    private val ACT_EXCLUDED_FIELDS =
        listOf("id", "sagaId", "currentChapterId", "emotionalReview", "createdAt")

    @Suppress("ktlint:standard:max-line-length")
    suspend fun loreGeneration(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        narrativeRules: NarrativeRules,
        sagaContent: SagaContent,
        currentTimeline: TimelineContent,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
    ): String {
        val args =
            LoreGenerationArgs(
                sagaTitle = sagaContent.data.title,
                loreUpdateLimit = narrativeRules.loreUpdateLimit.toString(),
                sagaMainContext = SagaPrompts.mainContext(sagaContent),
                storyContext = storyContext(sagaContent),
                conversationHistory =
                    currentTimeline.messages
                        .map { it.message }
                        .take(narrativeRules.loreUpdateLimit)
                        .normalizetoAIItems(
                            ChatPrompts.messageExclusions,
                        ),
                conversationDirective = config.conversationDirective,
            )

        return promptService.buildRemotePrompt("lore_generation_prompt", args)
    }

    private fun storyContext(sagaContent: SagaContent) =
        buildString {
            sagaContent.currentActInfo?.let {
                appendLine("Current story context: ")
                it.actSummary(sagaContent)
            }

            val previousEvents =
                sagaContent.acts.filter { it.isComplete() && it != sagaContent.currentActInfo }
            if (previousEvents.isNotEmpty()) {
                appendLine("Previous events: ")
                previousEvents.forEach {
                    it.actSummary(sagaContent, showEvents = false)
                }
            }
        }
}
