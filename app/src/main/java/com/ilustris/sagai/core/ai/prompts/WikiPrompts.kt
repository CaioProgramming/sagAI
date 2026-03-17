package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.MergeWiki
import com.ilustris.sagai.features.wiki.data.model.Wiki

data class WikiGenerationArgs(
    val existingWikis: String,
    val eventContext: String,
    val conversationDirective: String,
    val outputStructure: String,
)

data class MergeWikiArgs(
    val wikiItems: String,
    val outputStructure: String,
)

object WikiPrompts {
    suspend fun generateWiki(
        promptService: PromptService,
        saga: SagaContent,
        event: Timeline,
        config: GenreConfig,
    ): String {
        val wikis =
            saga.currentActInfo
                ?.currentChapterInfo
                ?.events
                ?.flatMap { it.updatedWikis } ?: emptyList()

        val wikiExclusion = listOf("createdAt", "sagaId", "id", "timelineId", "type")

        val args =
            WikiGenerationArgs(
                existingWikis =
                    if (wikis.isEmpty()) {
                        "No existing wiki entries in the current chapter."
                    } else {
                        wikis.formatToJsonArray(excludingFields = wikiExclusion)
                    },
                eventContext =
                    event.toJsonFormatExcludingFields(
                        listOf(
                            "createdAt",
                            "chapterId",
                            "emotionalReview",
                        ),
                    ),
                conversationDirective = config.conversationDirective,
                outputStructure =
                    toJsonMap(
                        Wiki::class.java,
                        filteredFields = listOf("id", "sagaId", "timelineId", "createdAt"),
                    ),
            )

        return promptService.buildRemotePrompt("wiki_generation_blueprint", args)
    }

    suspend fun mergeWiki(
        promptService: PromptService,
        wikis: List<Wiki>,
    ): String {
        val wikiExclusion = listOf("id", "sagaId", "timelineId", "createdAt")

        val args =
            MergeWikiArgs(
                wikiItems = wikis.formatToJsonArray(excludingFields = wikiExclusion),
                outputStructure =
                    toJsonMap(
                        MergeWiki::class.java,
                        filteredFields = wikiExclusion,
                    ),
            )

        return promptService.buildRemotePrompt("merge_wiki_blueprint", args)
    }
}
