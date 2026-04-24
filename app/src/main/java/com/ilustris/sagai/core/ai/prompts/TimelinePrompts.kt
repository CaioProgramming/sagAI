package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

data class PageGenerationArgs(
    val sagaMainContext: String,
    val storyContext: String,
    val recentPagesSummary: String,
    val newDialogueBurst: String,
    val genreInfo: String,
    val activeCharacters: String,
    val narrativeStyle: String,
    val existingWikis: String,
    val existingRelationships: String,
)

object TimelinePrompts {
    const val PAGE_GENERATION_BLUEPRINT = "page_generation_blueprint"
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
            if (this.isEmpty()) return "No recent pages recorded."
            return this.joinToString(separator = "\n") { t ->
                "- ${t.title}: ${t.content.take(150).replace('\n', ' ')}"
            }
        }

        val charactersList =
            sagaContent.characters.joinToString("\n") {
                "- ${it.data.name}: ${it.data.backstory.take(100)}"
            }

        val args =
            PageGenerationArgs(
                sagaMainContext =
                    sagaContent.data.toAINormalize(
                        listOf(
                            "id",
                            "createdAt",
                            "image",
                            "userId",
                        ),
                    ),
                storyContext = LorePrompts.storyContext(sagaContent, narrativeRules),
                recentPagesSummary = recentEvents.toBulletList(),
                newDialogueBurst =
                    currentTimeline.messages
                        .map { it.message }
                        .normalizetoAIItems(ChatPrompts.messageExclusions),
                genreInfo = "${sagaContent.data.genre.name} (${sagaContent.data.variationId ?: "Original"})",
                activeCharacters = charactersList,
                narrativeStyle = conversationDirective,
                existingWikis =
                    sagaContent.wikis.joinToString("\n") {
                        "- ${it.title} (${it.type?.name}): ${
                            it.content.take(
                                100,
                            )
                        }..."
                    },
                existingRelationships =
                    sagaContent.mainCharacter?.summarizeRelationships()
                        ?: "No established relationships.",
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
            if (this.isEmpty()) return "No recent pages recorded."
            return this.joinToString(separator = "\n") { t ->
                "- ${t.title}: ${t.content.take(150).replace('\n', ' ')}"
            }
        }

        val args =
            PageGenerationArgs(
                sagaMainContext = SagaPrompts.mainContext(sagaContent),
                storyContext = LorePrompts.storyContext(sagaContent, narrativeRules),
                recentPagesSummary = recentEvents.toBulletList(),
                newDialogueBurst =
                    currentTimeline.messages
                        .map { it.message }
                        .take(narrativeRules.loreUpdateLimit)
                        .normalizetoAIItems(ChatPrompts.messageExclusions),
                genreInfo = "${sagaContent.data.genre.name} (${sagaContent.data.variationId ?: "Original"})",
                activeCharacters = sagaContent.mainCharacter?.data?.name ?: "Unknown",
                narrativeStyle = conversationDirective,
                existingWikis = sagaContent.wikis.joinToString("\n") { "- ${it.title}" },
                existingRelationships =
                    sagaContent.mainCharacter?.summarizeRelationships()
                        ?: "None.",
            )

        return promptService.buildRemotePrompt(PAGE_GENERATION_BLUEPRINT, args)
    }
}
