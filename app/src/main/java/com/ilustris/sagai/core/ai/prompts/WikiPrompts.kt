package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.MergeWikiGen
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiType
import kotlin.collections.flatten

object WikiPrompts {
    fun generateWiki(
        saga: SagaContent,
        event: Timeline,
    ) = buildString {
        val wikis =
            saga.currentActInfo
                ?.currentChapterInfo
                ?.events
                ?.flatMap { it.updatedWikis } ?: emptyList()
        appendLine(
            "ROLE: You are an intelligent system tasked with extracting, structuring and creating a emoji for new or updated information from a provided list of recent timeline events to populate a game wiki.",
        )
        appendLine(
            "Your goal is to identify key entities and plot points and provide concise, factual summaries based *only* on the new information present in these events.",
        )
        appendLine("")
        appendLine("// The 'title' field MUST be a short (2-3 word) title for the wiki entry.")
        appendLine("// The 'emoji' field MUST be a valid unicode emoji that is relevant to the wiki entry.")
        appendLine("// This is the cast of characters in the saga.")
        appendLine("// IMPORTANT: DO NOT SAVE ANY INDIVIDUAL CHARACTERS AS WIKI ITEMS.")
        appendLine("")
        appendLine("EXISTING WORLD WIKI ENTRIES (For context and consistent terminology):")
        appendLine("// This is a comprehensive list of all known world entities in the saga's World Knowledge Base.")
        appendLine(
            "// Use this list to understand existing entities and their structure, and to avoid creating duplicate entries for information already known or to update existing entries with new details.",
        )
        if (wikis.isEmpty()) {
            appendLine("// No existing wiki entries in the current chapter.")
        } else {
            appendLine(
                wikis.formatToJsonArray(
                    excludingFields =
                        listOf(
                            "createdAt",
                            "sagaId",
                            "id",
                            "emojiTag",
                            "timelineId",
                            "type",
                        ),
                ),
            )
        }

        appendLine("Always follow that structure:")
        appendLine("[")
        appendLine(" ${toJsonMap(Wiki::class.java)}")
        appendLine("]")
        appendLine("// REMINDER: FOR 'FACTIONS' TYPE, ONLY INCLUDE GROUPS OR ORGANIZATIONS, NOT SINGLE INDIVIDUALS.")
        appendLine("// REMINDER: WRITE SHORT TITLES.")
        appendLine("// REMINDER: DO NOT INCLUDE SPECIFIC CHARACTERS IN THE WIKI OUTPUT.")
        appendLine("// IMPORTANT: TYPE FIELD MUST BE A STRING WITH ONE OF THIS OPTIONS: ${WikiType.entries.joinToString { it.name }}.")
        appendLine(
            "// For 'EVENT' type entries, only include significant, world-building events (e.g., city festivals, major incidents affecting the public, a new city-wide policy). DO NOT include specific character actions (e.g., 'Julie sabotaged the server') or direct plot progression points that are already detailed in the timeline.",
        )
        appendLine("")
        appendLine("RECENT STORY EVENTS FOR WIKI EXTRACTION:")
        appendLine("// This is the most recent event that occurred in the story.")
        appendLine(
            "// Extract all relevant NEW entities (locations, items, technologies, organizations, plot points, concepts, events) or significant UPDATES to existing entities from these events.",
        )
        appendLine("// CRITICAL: DO NOT EXTRACT OR INCLUDE ANY INDIVIDUAL CHARACTERS IN THE WIKI OUTPUT.")
        appendLine(event.toJsonFormatExcludingFields(fieldsToExclude = listOf("createdAt", "chapterId", "emotionalReview")))
        appendLine(
            "FINAL AND CRITICAL RULE: DO NOT GENERATE WIKI ENTRIES FOR INDIVIDUAL CHARACTERS. Ensure 'FACTION' type is used ONLY for groups or ILLEGAL organizations.",
        )
    }

    fun mergeWiki(currentChapterContent: ChapterContent) =
        buildString {
            appendLine("ROLE: You are an intelligent system tasked with merging and consolidating Wiki entries from a story chapter.")
            appendLine("INPUT: A list of Wiki items from the current chapter (see below).")
            appendLine("TASK:")
            appendLine(
                "- Analyze the provided Wiki items and identify entries that refer to the same or closely related entities (e.g., locations, organizations, technologies, events).",
            )
            appendLine(
                "- Merge related or duplicate items into a single WikiItem, combining all relevant details and removing redundancies or conflicts.",
            )
            appendLine("- Ensure the merged item is clear, concise, and follows the Wiki structure: title, emoji, type, description, etc.")
            appendLine("- Do NOT merge items of different types or unrelated entities.")
            appendLine("- Do NOT include individual character entries in the output.")
            appendLine("- Output only the consolidated list, with duplicates removed and merged items replacing them.")
            appendLine("")
            appendLine("EXISTING WIKI ITEMS FROM CHAPTER:")
            appendLine(currentChapterContent.events.map { it.updatedWikis }.formatToJsonArray())
            appendLine("STRUCTURE EXAMPLE:")
            appendLine(" ${toJsonMap(MergeWikiGen::class.java)}")
        }
}
