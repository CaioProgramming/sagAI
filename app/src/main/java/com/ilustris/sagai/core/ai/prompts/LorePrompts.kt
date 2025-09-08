package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.findTimelineChapter
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.isFirstAct
import com.ilustris.sagai.features.home.data.model.isTimelineOnFirstChapter
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

object LorePrompts {
    private val SAGA_EXCLUDED_FIELDS =
        listOf(
            "id",
            "icon",
            "createdAt",
            "mainCharacterId",
            "isDebug",
            "endMessage",
            "currentActId",
            "endedAt",
            "review",
            "emotionalReview",
            "isEnded",
        )

    private val CHARACTER_EXCLUDED_FIELDS =
        listOf(
            "id",
            "image",
            "hexColor",
            "sagaId",
            "details",
            "joinedAt",
        )

    private val TIMELINE_EXCLUDED_FIELDS =
        listOf("id", "timelineId", "emotionalReview", "createdAt", "chapterId")

    private val CHAPTER_EXCLUDED_FIELDS =
        listOf(
            "id",
            "currentEventId",
            "sagaId",
            "actId",
            "featuredCharacters",
            "coverImage",
            "emotionalReview",
            "createdAt",
        )

    private val ACT_EXCLUDED_FIELDS =
        listOf("id", "sagaId", "currentChapterId", "emotionalReview", "createdAt")

    @Suppress("ktlint:standard:max-line-length")
    fun loreGeneration(
        sagaContent: SagaContent,
        currentTimeline: TimelineContent,
    ) = buildString {
        val isFirstTimeline =
            sagaContent
                .flatEvents()
                .firstOrNull()
                ?.data
                ?.id == currentTimeline.data.id
        val isFirstAct = sagaContent.isFirstAct()
        val isFirstChapter = sagaContent.isTimelineOnFirstChapter(currentTimeline.data)
        val chapterCurrentTimeline = sagaContent.findTimelineChapter(currentTimeline.data)

        appendLine(
            "You are the Saga Chronicler, an AI specialized in identifying and documenting the single most significant new narrative development from conversations.",
        )
        appendLine(
            "Your SOLE TASK is to review the saga's current timeline (if provided) and the latest segment of conversation history (e.g., last ${UpdateRules.LORE_UPDATE_LIMIT} messages for '${sagaContent.data.title}' RPG).",
        )
        appendLine(
            "Based on this review, generate A SINGLE NEW, significant timeline event that occurred in the provided \"CONVERSATION HISTORY TO SUMMARIZE\".",
        )
        appendLine(
            "If multiple potential events are present, choose the ONE that is most impactful or moves the narrative forward the most.",
        )
        appendLine(
            "If NO new significant timeline event is found in the conversation, you should still generate a Timeline object, but with a title like \"Quiet Conversation\" or \"No New Developments\" and a description reflecting that the conversation continued without a major plot progression.",
        )
        appendLine("For the SINGLE timeline event you generate, create a complete JSON object with the following fields:")
        appendLine(
            "- \"title\": Generate a SHORT and ASSERTIVE title for this event. It should be concise (ideally 3-7 words) and directly capture the essence of the event.",
        )
        appendLine(
            "- \"content\": Write a MEDIUM-LENGTH narrative description of what happened in the event (e.g., 1-3 paragraphs). This description must provide USEFUL INFORMATION, focusing on key actions, important dialogue, critical decisions, significant discoveries, or consequences revealed in the conversation.",
        )
        appendLine("")
        appendLine(
            "Focus ONLY on generating this single timeline event. Do NOT generate lists of events, world knowledge entries, or character updates.",
        )

        appendLine("Saga Context:")
        appendLine(sagaContent.data.toJsonFormatExcludingFields(SAGA_EXCLUDED_FIELDS))

        appendLine("Main Character Context:")
        appendLine(
            sagaContent.mainCharacter?.data?.toJsonFormatExcludingFields(
                CHARACTER_EXCLUDED_FIELDS,
            ),
        )

        storyContext(sagaContent, currentTimeline)

        appendLine("CONVERSATION HISTORY TO SUMMARIZE (New Segment - e.g., last ${UpdateRules.LORE_UPDATE_LIMIT} messages):")
        appendLine("// This is the new chunk of messages that needs to be analyzed for a new lore event.")
        appendLine("// Focus on extracting the most significant and lasting single event from this segment.")
        appendLine(
            currentTimeline.messages.joinToString(";\n") {
                it.joinMessage().formatToString()
            },
        )

        appendLine("GENERATE A SINGLE JSON OBJECT representing this timeline event.")
        appendLine("FOLLOW THIS EXACT JSON STRUCTURE FOR YOUR RESPONSE. DO NOT INCLUDE ANY OTHER TEXT OUTSIDE.")
        appendLine(toJsonMap(Timeline::class.java, TIMELINE_EXCLUDED_FIELDS))
    }.trimIndent()

    private fun storyContext(
        sagaContent: SagaContent,
        currentTimeline: TimelineContent,
    ) = buildString {
        val isFirstTimelineInChapter =
            sagaContent
                .flatEvents()
                .firstOrNull()
                ?.data
                ?.id == currentTimeline.data.id
        val isFirstAct = sagaContent.isFirstAct()
        val isFirstChapterInAct = sagaContent.isTimelineOnFirstChapter(currentTimeline.data)
        val timelineChapter = sagaContent.findTimelineChapter(currentTimeline.data)
        val chapterAct = sagaContent.findChapterAct(timelineChapter?.data)
        if (!isFirstAct) {
            val acts =
                sagaContent.acts.filter { it.data.id != chapterAct?.data?.id && it.isComplete() }
            if (acts.isNotEmpty()) {
                appendLine("Previous acts context:")
                appendLine(acts.toJsonFormatExcludingFields(ACT_EXCLUDED_FIELDS))
            }
        }

        if (!isFirstChapterInAct) {
            val chapters =
                sagaContent
                    .flatChapters()
                    .filter {
                        it.data.id !=
                            sagaContent.currentActInfo
                                ?.currentChapterInfo
                                ?.data
                                ?.id && it.isComplete()
                    }
            if (chapters.isNotEmpty()) {
                appendLine("Previous chapters context:")
                appendLine(chapters.toJsonFormatExcludingFields(CHAPTER_EXCLUDED_FIELDS))
            }
        }

        if (!isFirstTimelineInChapter) {
            sagaContent
                .findTimelineChapter(currentTimeline.data)
                ?.events
                ?.filter { it.isComplete() && it.data.id != currentTimeline.data.id }
                ?.let {
                    if (it.isNotEmpty()) {
                        appendLine("Previous timeline context:")
                        appendLine(it.toJsonFormatExcludingFields(TIMELINE_EXCLUDED_FIELDS))
                    }
                }
        }
    }
}
