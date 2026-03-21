package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

data class LoreGenerationArgs(
    val sagaTitle: String,
    val loreUpdateLimit: String,
    val sagaContext: String,
    val storyContext: String,
    val newConversationBust: String,
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

    val TIMELINE_EXCLUDED_FIELDS =
        listOf("id", "timelineId", "emotionalReview", "createdAt", "chapterId")

    val CHAPTER_EXCLUDED_FIELDS =
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

    val ACT_EXCLUDED_FIELDS =
        listOf("id", "sagaId", "currentChapterId", "emotionalReview", "createdAt")

    @Suppress("ktlint:standard:max-line-length")
    suspend fun loreGeneration(
        promptService: PromptService,
        narrativeRules: NarrativeRules,
        sagaContent: SagaContent,
        currentTimeline: TimelineContent,
        conversationDirective: String,
    ): String {
        val args =
            LoreGenerationArgs(
                sagaTitle = sagaContent.data.title,
                loreUpdateLimit = narrativeRules.loreUpdateLimit.toString(),
                sagaContext = SagaPrompts.mainContext(sagaContent),
                storyContext = storyContext(sagaContent, narrativeRules),
                newConversationBust =
                    currentTimeline.messages
                        .map { it.message }
                        .take(narrativeRules.loreUpdateLimit)
                        .normalizetoAIItems(
                            ChatPrompts.messageExclusions,
                        ),
                conversationDirective = conversationDirective,
            )

        return promptService.buildRemotePrompt("lore_generation_blueprint", args)
    }

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
