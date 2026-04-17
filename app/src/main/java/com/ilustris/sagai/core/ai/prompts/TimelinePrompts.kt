package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

data class TimelineArgs(
    val sagaContext: String,
    val storyContext: String,
    val recentTimeline: String,
    val newConversationBurst: String,
    val genreName: String,
    val variationId: String,
    val characterName: String,
    val loreUpdateLimit: String,
    val narrativeStyle: String = "",
)

object TimelinePrompts {
    const val LORE_GENERATION_BLUEPRINT = "lore_generation_blueprint"
    const val UNIFIED_LORE_GENERATION_BLUEPRINT = "unified_lore_generation_blueprint"

    val timelineExclusions = listOf("id", "chapterId", "createdAt", "emotionalReview")

    suspend fun generateUnifiedLorePrompt(
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
                .takeLast(7)

        fun List<Timeline>.toBulletList(): String {
            if (this.isEmpty()) return "No recent events recorded."
            return this.joinToString(separator = "\n") { t ->
                "- ${t.title}: ${t.content.take(150).replace('\n', ' ')}"
            }
        }

        val charactersList =
            sagaContent.characters.joinToString("\n") {
                "- ${it.data.name} (ID: ${it.data.id}): ${it.data.backstory.take(100)}"
            }

        val args =
            TimelineArgs(
                sagaContext =
                    sagaContent.data.toAINormalize(
                        listOf(
                            "id",
                            "createdAt",
                            "image",
                            "userId",
                        ),
                    ),
                storyContext = LorePrompts.storyContext(sagaContent, narrativeRules),
                recentTimeline = recentEvents.toBulletList(),
                newConversationBurst =
                    currentTimeline.messages
                        .map { it.message }
                        .normalizetoAIItems(ChatPrompts.messageExclusions),
                genreName = sagaContent.data.genre.name,
                variationId = sagaContent.data.variationId ?: emptyString(),
                characterName = charactersList,
                loreUpdateLimit = narrativeRules.loreUpdateLimit.toString(),
                narrativeStyle = conversationDirective,
            )

        return promptService.buildRemotePrompt(UNIFIED_LORE_GENERATION_BLUEPRINT, args)
    }

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

        fun List<Timeline>.toBulletList(): String {
            if (this.isEmpty()) return "No recent events recorded."
            return this.joinToString(separator = "\n") { t ->
                "- ${t.title}: ${t.content.take(150).replace('\n', ' ')}"
            }
        }

        val args =
            TimelineArgs(
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
                variationId = sagaContent.data.variationId ?: emptyString(),
                narrativeStyle = conversationDirective,
            )

        return promptService.buildRemotePrompt(LORE_GENERATION_BLUEPRINT, args)
    }
}
