package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

data class TimelineArgs(
    val sagaTitle: String,
    val sagaContext: String,
    val storyContext: String,
    val recentTimeline: String,
    val newConversationBurst: String,
    val genreName: String,
    val characterName: String,
    val loreUpdateLimit: String,
    val conversationDirective: String = "",
)

object TimelinePrompts {
    val timelineExclusions = listOf("id", "chapterId", "createdAt", "emotionalReview")

    suspend fun generateTimelinePrompt(
        promptService: PromptService,
        narrativeRules: NarrativeRules,
        sagaContent: SagaContent,
        currentTimeline: TimelineContent,
        conversationDirective: String,
    ): String {
        val recentEvents =
            sagaContent
                .flatEvents()
                .map { it.data }
                .filter { it.id != currentTimeline.data.id }
                .takeLast(5)

        fun List<com.ilustris.sagai.features.timeline.data.model.Timeline>.toBulletList(): String {
            if (this.isEmpty()) return "No recent events recorded."
            return this.joinToString(separator = "\n") { t ->
                "- ${t.title}: ${t.content.take(150).replace('\n', ' ')}"
            }
        }

        val args =
            TimelineArgs(
                sagaTitle = sagaContent.data.title,
                sagaContext = SagaPrompts.mainContext(sagaContent),
                storyContext = LorePrompts.storyContext(sagaContent, narrativeRules),
                recentTimeline = recentEvents.toBulletList(),
                newConversationBurst =
                    currentTimeline.messages
                        .map { it.message }
                        .take(narrativeRules.loreUpdateLimit)
                        .normalizetoAIItems(ChatPrompts.messageExclusions),
                genreName = sagaContent.data.genre.name,
                characterName = sagaContent.mainCharacter?.data?.name ?: "Unknown",
                loreUpdateLimit = narrativeRules.loreUpdateLimit.toString(),
                conversationDirective = conversationDirective,
            )

        return promptService.buildRemotePrompt("lore_generation_blueprint", args)
    }
}
