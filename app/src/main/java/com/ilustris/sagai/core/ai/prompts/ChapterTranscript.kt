package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent

/** One chat line as the model reads it: who said it, and what. */
internal fun MessageContent.transcriptLine(): String {
    val speaker = character?.name ?: message.speakerName ?: message.senderType.name
    return "$speaker: ${message.text}"
}

/**
 * The chapter's chat, event by event in the order the events happened: what was really said and done
 * in each. It is sent whole, never trimmed, so a prompt that needs the truth of the chapter has all
 * of it. Null when the chapter has no messages, so callers can leave the field out of the prompt.
 */
internal fun ChapterContent.eventTranscripts(): String? =
    events
        .sortedBy { it.data.createdAt }
        .mapNotNull { event ->
            val lines = event.messages.sortedBy { it.message.timestamp }
            if (lines.isEmpty()) return@mapNotNull null
            buildString {
                appendLine("## EVENT: ${event.data.title}")
                lines.forEach { appendLine(it.transcriptLine()) }
            }
        }.takeIf { it.isNotEmpty() }
        ?.joinToString("\n")
