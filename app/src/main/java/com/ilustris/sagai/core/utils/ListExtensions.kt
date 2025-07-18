package com.ilustris.sagai.core.utils

import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.timeline.data.model.Timeline

fun <T> List<T>.afterLast(predicate: (T) -> Boolean): List<T> {
    val index = indexOfLast(predicate)
    return if (index != -1 && index < lastIndex) subList(index + 1, size) else emptyList()
}

fun List<Any>.formatToJsonArray() = joinToString(separator = ",\n") { it.toJsonFormat() }

fun sortCharactersByMessageCount(
    characters: List<Character>,
    messages: List<MessageContent>,
) = characters.sortedByDescending { character ->
    messages
        .count { message ->

            message.character?.id == character.id ||
                message.message.speakerName.equals(character.name, true)
        }
}

/**
 * Retrieves a sublist of Timeline events that belong to a specific chapter.
 *
 * It determines the range of events based on the `eventReference` of the target chapter
 * and the `eventReference` of the previous chapter.
 *
 * @receiver A list of `Timeline` events, assumed to be sorted chronologically (e.g., by ID or createdAt).
 * @param targetChapter The chapter for which to retrieve events. Its `eventReference` marks the end of its event span.
 * @param previousChapter The chapter immediately preceding the `targetChapter`, or null if `targetChapter` is the first.
 *                        Its `eventReference` marks the end of the previous span.
 * @return A list of `Timeline` events for the `targetChapter`. Returns an empty list if references are missing,
 *         events are not found, or the range is invalid.
 */
fun List<Timeline>.getEventsForChapter(
    targetChapter: Chapter,
    previousChapter: Chapter?,
): List<Timeline> {
    val endEventId =
        targetChapter.eventReference
            ?: return emptyList()
    val endEventIndex = this.indexOfFirst { it.id == endEventId }
    if (endEventIndex == -1) {
        return emptyList()
    }

    val startEventIndex: Int
    if (previousChapter == null) {
        startEventIndex = 0
    } else {
        val prevChapterEndEventId =
            previousChapter.eventReference
                ?: return emptyList()
        val prevChapterEndEventIndex = this.indexOfFirst { it.id == prevChapterEndEventId }
        if (prevChapterEndEventIndex == -1) {
            return emptyList()
        }
        startEventIndex = prevChapterEndEventIndex + 1
    }

    if (startEventIndex > endEventIndex) {
        return emptyList()
    }

    return this.subList(startEventIndex, endEventIndex + 1)
}

/**
 * Builds a structured string representation of the saga's acts, chapters, and events.
 * Useful for providing comprehensive context to AI prompts.
 */
fun SagaContent.buildFormattedSagaContextString(): String {
    val builder = StringBuilder()
    val sortedActs = this.acts.sortedBy { it.id }
    val sortedChaptersGlobal = this.chapters.sortedBy { it.id }
    val sortedTimelines = this.timelines.sortedBy { it.id }

    builder.append("## Saga Overview: ${this.data.title}\n")
    builder.append("Genre: ${this.data.genre.title}\n")

    if (sortedActs.isEmpty()) {
        builder.append("No acts available yet.\n")
    }

    sortedActs.forEachIndexed { actIndex, act ->
        builder.append("### Act ${actIndex + 1}: ${act.title.ifBlank { "(Untitled Act)" }}\n")
        if (act.content.isNotBlank()) {
            builder.append("Act Description: ${act.content.lines().firstOrNull() ?: emptyString()}")
            if (act.content.lines().size > 1) builder.append("...")
            builder.append("\n")
        }
        builder.append("\n")

        val chaptersInThisAct = sortedChaptersGlobal.filter { it.actId == act.id }

        if (chaptersInThisAct.isEmpty()) {
            builder.append("  No chapters in this act yet.\n\n")
        }

        chaptersInThisAct.forEach { chapter ->
            builder.append("  #### Chapter: ${chapter.title.ifBlank { "(Untitled Chapter)" }}\n")
            if (chapter.overview.isNotBlank()) {
                builder.append("  Chapter Overview: ${chapter.overview.lines().firstOrNull() ?: emptyString()}")
                if (chapter.overview.lines().size > 1) builder.append("...")
                builder.append("\n")
            }

            // Find the global index of the current chapter to find its previous chapter
            val globalChapterIndex = sortedChaptersGlobal.indexOfFirst { it.id == chapter.id }
            val previousChapter = if (globalChapterIndex > 0) sortedChaptersGlobal.getOrNull(globalChapterIndex - 1) else null

            val eventsForChapter = sortedTimelines.getEventsForChapter(chapter, previousChapter)

            if (eventsForChapter.isEmpty()) {
                builder.append("    No timeline events recorded for this chapter.\n")
            } else {
                builder.append("    Events:\n")
                eventsForChapter.forEach { event ->
                    builder.append("    - ${event.title.ifBlank { "(Untitled Event)" }}: ")
                    builder.append(
                        event.content
                            .lines()
                            .firstOrNull()
                            ?.take(80) ?: "(No content)",
                    )
                    if ((
                            event.content
                                .lines()
                                .firstOrNull()
                                ?.length ?: 0
                        ) > 80 ||
                        event.content.lines().size > 1
                    ) {
                        builder.append("...")
                    }
                    builder.append("\n")
                }
            }
            builder.append("\n")
        }
        builder.append("---\n\n")
    }

    if (this.characters.isNotEmpty()) {
        builder.append("### Main Characters:\n")
        this.characters.forEach { character ->
            // Changed from .take(5)
            builder.append(
                "- ${character.name}: ${character.backstory
                    .lines()
                    .firstOrNull()
                    ?.take(100) ?: "(No backstory snippet)"}",
            )
            if ((
                    character.backstory
                        .lines()
                        .firstOrNull()
                        ?.length ?: 0
                ) > 100 ||
                character.backstory.lines().size > 1
            ) {
                builder.append("...")
            }
            builder.append("\n")
        }
        // Removed: if (this.characters.size > 5) builder.append("...and more.\n")
    }

    return builder.toString()
}
