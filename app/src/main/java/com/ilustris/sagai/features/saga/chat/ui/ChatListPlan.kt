package com.ilustris.sagai.features.saga.chat.ui

import androidx.compose.runtime.Immutable
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.timeline.domain.TimelineCardContent

/**
 * One row of the chat list, resolved before composition so that a chapter's position in the
 * `LazyColumn` is knowable — [ChapterScrollRail] jumps by looking an entry up in this list.
 *
 * Entries carry identity and references only, never rendered content: the reasoning text, for
 * instance, is read from the composable scope at render time so a streaming chunk doesn't
 * rebuild the plan (and shift every index) on each token.
 */
@Immutable
sealed interface ChatEntry {
    val key: String
    val contentType: String
    val chapterId: Int? get() = null
    val actId: Int? get() = null

    data object BottomSpacer : ChatEntry {
        override val key = "bottom-spacer"
        override val contentType = "spacer"
    }

    data object Reasoning : ChatEntry {
        override val key = "reasoning"
        override val contentType = "reasoning"
    }

    data object RecapHero : ChatEntry {
        override val key = "recap-hero"
        override val contentType = "recap-hero"
    }

    data object EndMessage : ChatEntry {
        override val key = "end-message"
        override val contentType = "end-message"
    }

    data object EndedAt : ChatEntry {
        override val key = "ended-at"
        override val contentType = "ended-at"
    }

    data class ActConclusion(
        val act: ActMetadata,
    ) : ChatEntry {
        override val key = "act-${act.data.id}-conclusion"
        override val contentType = "act-conclusion"
        override val actId = act.data.id
    }

    data class ChapterCover(
        val chapter: ChapterMetadata,
        /** True for the act's oldest chapter — `chapters` arrives reversed. */
        val isLast: Boolean,
        override val actId: Int,
    ) : ChatEntry {
        override val key = "chapter-${chapter.data.id}"
        override val contentType = "chapter-cover"
        override val chapterId = chapter.data.id
    }

    data class TimelineCard(
        val timeline: TimelineCardContent,
        override val chapterId: Int,
        override val actId: Int,
    ) : ChatEntry {
        override val key = "timeline-${timeline.timelineContent.data.id}"
        override val contentType = "timeline-card"
    }

    data class Message(
        val content: MessageContent,
        override val chapterId: Int,
        override val actId: Int,
    ) : ChatEntry {
        override val key = "message-${content.message.id}"
        override val contentType = "message"
    }

    data class TimelineSpark(
        val timelineId: Int,
        override val chapterId: Int,
        override val actId: Int,
    ) : ChatEntry {
        override val key = "timeline-$timelineId-spark"
        override val contentType = "timeline-spark"
    }

    data class ChapterIntro(
        val chapter: ChapterMetadata,
        override val actId: Int,
    ) : ChatEntry {
        override val key = "chapter-${chapter.data.id}-intro"
        override val contentType = "chapter-intro"
        override val chapterId = chapter.data.id
    }

    data class ChapterTitle(
        val chapter: ChapterMetadata,
        override val actId: Int,
    ) : ChatEntry {
        override val key = "chapter-${chapter.data.id}-title"
        override val contentType = "chapter-title"
        override val chapterId = chapter.data.id
    }

    data class ActIntro(
        val act: ActMetadata,
    ) : ChatEntry {
        override val key = "act-${act.data.id}-intro"
        override val contentType = "act-intro"
        override val actId = act.data.id
    }

    data class ActTitle(
        val act: ActMetadata,
    ) : ChatEntry {
        override val key = "act-${act.data.id}-title"
        override val contentType = "act-title"
        override val actId = act.data.id
    }

    data class SagaHeader(
        val sagaId: Int,
    ) : ChatEntry {
        override val key = "saga-$sagaId-header"
        override val contentType = "saga-header"
    }
}

/** Act synthesis body shown in chat after its chapters (see [ChatList] item order with `reverseLayout`). */
internal fun ActDisplayData.hasConclusionContent(): Boolean {
    val data = content.data
    return data.content.isNotBlank() || !data.emotionalReview.isNullOrBlank()
}

/**
 * Flattens the display tree into the exact order the chat renders, newest first — [actList] and
 * every level under it already arrive reversed from `SagaMetadataUIMapper`.
 */
fun buildChatPlan(
    actList: List<ActDisplayData>,
    sagaId: Int,
    isEnded: Boolean,
    endMessage: String,
    hasReasoning: Boolean,
): List<ChatEntry> =
    buildList {
        add(ChatEntry.BottomSpacer)

        if (hasReasoning) add(ChatEntry.Reasoning)

        if (isEnded && endMessage.isNotEmpty()) {
            add(ChatEntry.RecapHero)
            add(ChatEntry.EndMessage)
            add(ChatEntry.EndedAt)
        }

        actList.forEach { act ->
            val actId = act.content.data.id
            val hasConclusion = act.hasConclusionContent()

            if (hasConclusion) add(ChatEntry.ActConclusion(act.content))

            act.chapters.forEach { chapter ->
                val chapterMetadata = chapter.chapter
                val chapterId = chapterMetadata.data.id

                if (chapter.isComplete) {
                    add(
                        ChatEntry.ChapterCover(
                            chapter = chapterMetadata,
                            isLast = act.chapters.lastOrNull() == chapter,
                            actId = actId,
                        ),
                    )
                }

                chapter.timelineSummaries.forEach { timelineDisplay ->
                    val timeline = timelineDisplay.timeline

                    if (timeline.canShowData) {
                        add(ChatEntry.TimelineCard(timeline, chapterId, actId))
                    }

                    timeline.timelineContent.messages.forEach { message ->
                        add(ChatEntry.Message(message, chapterId, actId))
                    }

                    add(
                        ChatEntry.TimelineSpark(
                            timelineId = timeline.timelineContent.data.id,
                            chapterId = chapterId,
                            actId = actId,
                        ),
                    )
                }

                add(ChatEntry.ChapterIntro(chapterMetadata, actId))
                add(ChatEntry.ChapterTitle(chapterMetadata, actId))
            }

            if (act.content.data.introduction.isNotEmpty()) add(ChatEntry.ActIntro(act.content))
            if (!hasConclusion) add(ChatEntry.ActTitle(act.content))
        }

        add(ChatEntry.SagaHeader(sagaId))
    }

/**
 * Where the rail scrolls to for [chapterId]: the chapter's oldest message, which is its *last*
 * message entry here because the plan runs newest first. Falls back to the chapter title — always
 * emitted — for a chapter that has no messages yet. Returns -1 when the chapter isn't in the plan.
 */
fun List<ChatEntry>.chapterAnchorIndex(chapterId: Int): Int =
    indexOfLast { it is ChatEntry.Message && it.chapterId == chapterId }
        .takeIf { it >= 0 }
        ?: indexOfFirst { it is ChatEntry.ChapterTitle && it.chapterId == chapterId }
