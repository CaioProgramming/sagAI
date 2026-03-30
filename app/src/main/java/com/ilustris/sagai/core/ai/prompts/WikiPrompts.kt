package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiType

data class WikiGenerationArgs(
    val sagaContext: String,
    val eventData: String,
    val existingWikis: String,
    val globalWikiIndex: String,
    val wikiTypes: String,
)

data class MergeWikiArgs(
    val wikiList: String,
)

object WikiPrompts {
    const val MERGE_WIKI_BLUEPRINT = "merge_wiki_blueprint"
    const val WIKI_GENERATION_BLUEPRINT = "wiki_generation_blueprint"

    suspend fun generateWiki(
        promptService: PromptService,
        saga: SagaContent,
        event: Timeline,
        config: GenreConfig,
    ): String {
        val wikiExclusion = listOf("createdAt", "sagaId", "id", "timelineId", "type")

        val eventRawText = "${event.title} ${event.content}".lowercase()

        val relevantWikis =
            saga.wikis.filter { wiki ->
                eventRawText.contains(wiki.title.lowercase()) || wiki.timelineId == event.id
            }

        val globalWikiIndex =
            (saga.wikis - relevantWikis.toSet()).joinToString(", ") {
                "${it.emojiTag ?: ""} ${it.title}"
            }

        val args =
            WikiGenerationArgs(
                sagaContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                eventData =
                    event.toJsonFormatExcludingFields(
                        listOf(
                            "createdAt",
                            "chapterId",
                            "emotionalReview",
                        ),
                    ),
                existingWikis =
                    if (relevantWikis.isEmpty()) {
                        "No relevant existing wiki entries detected for this event."
                    } else {
                        relevantWikis.formatToJsonArray(excludingFields = wikiExclusion)
                    },
                globalWikiIndex = globalWikiIndex,
                wikiTypes = WikiType.entries.joinToString(", "),
            )

        return promptService.buildRemotePrompt(WIKI_GENERATION_BLUEPRINT, args)
    }

    suspend fun mergeWiki(
        promptService: PromptService,
        wikis: List<Wiki>,
    ): String {
        val wikiExclusion = listOf("id", "sagaId", "timelineId", "createdAt")

        val args =
            MergeWikiArgs(
                wikiList = wikis.formatToJsonArray(excludingFields = wikiExclusion),
            )

        return promptService.buildRemotePrompt(MERGE_WIKI_BLUEPRINT, args)
    }
}
