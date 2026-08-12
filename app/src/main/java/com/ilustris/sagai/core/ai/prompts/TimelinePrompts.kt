package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.buildContextualHistory
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.narrative.domain.buildChatContinuityContext
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

data class PageGenerationArgs(
    val sagaMainContext: String,
    val recentPagesSummary: String,
    val newDialogueBurst: String,
    val extraContent: String,
)

object TimelinePrompts {
    const val PAGE_GENERATION_BLUEPRINT = "page_generation_blueprint"
    const val UNIFIED_LORE_GENERATION_BLUEPRINT = "unified_lore_generation_blueprint"

    val timelineExclusions =
        listOf("id", "chapterId", "createdAt") + LorePrompts.LORE_OUTPUT_ONLY_FIELDS

    suspend fun generateUnifiedLorePrompt(
        promptService: PromptService,
        narrativeRules: NarrativeRules,
        sagaContent: SagaContent,
    ): SplitPrompt {
        val recentEvents =
            sagaContent
                .flatEvents()
                .filter { it.isComplete(narrativeRules) && it.data.content.isNotEmpty() }
                .takeLast(narrativeRules.loreUpdateLimit)
                .map { it.data }
                .normalizetoAIItems(LorePrompts.TIMELINE_EXCLUDED_FIELDS)

        val charactersList =
            sagaContent.characters.map {
                buildMap {
                    putAll(it.data.asMap())
                    it.summarizeRelationships()
                }
            }

        val args =
            PageGenerationArgs(
                sagaMainContext = sagaContent.buildContextualHistory(narrativeRules).toAINormalize(),
                recentPagesSummary = recentEvents,
                newDialogueBurst =
                    sagaContent
                        .flatMessages()
                        .takeLast(narrativeRules.loreUpdateLimit)
                        .map { it.message }
                        .normalizetoAIItems(ChatPrompts.messageExclusions),
                extraContent =
                    buildMap {
                        put("Characters", charactersList)
                        put("Wiki", sagaContent.wikis.normalizetoAIItems())
                    }.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS),
            )

        return promptService.buildSplitBlueprint(UNIFIED_LORE_GENERATION_BLUEPRINT, args)
    }

    suspend fun generateTimelinePrompt(
        promptService: PromptService,
        narrativeRules: NarrativeRules,
        sagaContent: SagaContent,
    ): SplitPrompt {
        val recentEvents =
            sagaContent
                .flatEvents()
                .filter { it.isComplete(narrativeRules) && it.data.content.isNotEmpty() }
                .takeLast(narrativeRules.loreUpdateLimit)
                .map { it.data }
                .normalizetoAIItems(LorePrompts.TIMELINE_EXCLUDED_FIELDS)

        val charactersList =
            sagaContent.characters.map {
                buildMap {
                    putAll(it.data.asMap())
                    it.summarizeRelationships()
                }
            }

        val args =
            PageGenerationArgs(
                sagaMainContext = sagaContent.buildContextualHistory(narrativeRules).toAINormalize(),
                recentPagesSummary = recentEvents,
                newDialogueBurst =
                    sagaContent
                        .flatMessages()
                        .takeLast(narrativeRules.loreUpdateLimit)
                        .map { it.message }
                        .normalizetoAIItems(ChatPrompts.messageExclusions),
                extraContent =
                    buildMap {
                        put("Characters", charactersList)
                        put("Wiki", sagaContent.wikis.normalizetoAIItems())
                    }.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS),
            )

        return promptService.buildSplitBlueprint(PAGE_GENERATION_BLUEPRINT, args)
    }
}
