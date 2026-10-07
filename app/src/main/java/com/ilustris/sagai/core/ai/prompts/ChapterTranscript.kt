package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent

/**
 * A chat message as the model reads it: who said it and what, nothing else. A class of its own, not
 * the Message entity, so a field added to the entity later (reasoning, original text, audio path)
 * never reaches a prompt unnoticed.
 */
internal data class TranscriptLine(
    val speakerName: String,
    val text: String,
)

/**
 * One event of a closing chapter together with the chat it came from, so the model reads each
 * moment next to what really happened in it. Carries the event fields the prompts read; the rest of
 * Timeline is internal.
 */
internal data class TimelineWithMessages(
    val title: String,
    val content: String,
    val emotionalTone: EmotionalTone?,
    val narrativeGuide: String?,
    val messages: List<TranscriptLine>,
)

private fun MessageContent.speaker(): String = character?.name ?: message.speakerName ?: message.senderType.name

/** One chat line as a single string, for prompts that lay a transcript out as plain text. */
internal fun MessageContent.transcriptLine(): String = "${speaker()}: ${message.text}"

/**
 * The chapter's events in the order they happened, each with its whole chat in order. Sent whole,
 * never trimmed, so a prompt that needs the truth of the chapter has all of it.
 */
internal fun ChapterContent.timelineWithMessages(): List<TimelineWithMessages> =
    events
        .sortedBy { it.data.createdAt }
        .map { event ->
            TimelineWithMessages(
                title = event.data.title,
                content = event.data.content,
                emotionalTone = event.data.emotionalTone,
                narrativeGuide = event.data.narrativeGuide,
                messages =
                    event.messages
                        .sortedBy { it.message.timestamp }
                        .map { TranscriptLine(it.speaker(), it.message.text) },
            )
        }
