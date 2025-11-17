package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.timeline.data.model.Timeline

object TimelinePrompts {
    fun timeLineDetails(currentChapter: ChapterContent?) =
        buildString {
            val events = currentChapter?.events?.filter { it.isComplete() }?.map { it.data }
            if (events?.isNotEmpty() == true) {
                appendLine("**CURRENT CHAPTER TIMELINE (Most Recent Events):**")
                appendLine("// This section provides the most recent events from the chapter's timeline.")
                appendLine("// Use this to understand the immediate plot progression and current situation.")
                appendLine(
                    events.toAINormalize(
                        listOf(
                            "emotionalReview",
                            "chapterId",
                            "id",
                            "createdAt",
                        ),
                    ),
                )
            }
        }

    fun generateCurrentObjectivePrompt(
        chapterIntroduction: String,
        recentEvents: List<Timeline>,
    ): String =
        buildString {
            appendLine(
                "ROLE: You are an intelligent system tasked with summarizing the current chapter's main objective for story progression.",
            )
            appendLine("INPUT: The chapter introduction and the most recent timeline events.")
            appendLine("TASK:")
            appendLine("- Analyze the chapter introduction and recent events.")
            appendLine("- Identify the core objective or goal that drives the story forward at this point.")
            appendLine("- Summarize this objective in a single, clear sentence suitable for guiding the next story actions.")
            appendLine("- Avoid redundant details; focus on the main goal or challenge for the characters.")
            appendLine("")
            appendLine("CHAPTER INTRODUCTION:")
            appendLine(chapterIntroduction)
            appendLine("")
            appendLine("RECENT EVENTS:")
            appendLine(
                recentEvents.formatToJsonArray(
                    excludingFields =
                        listOf(
                            "createdAt",
                            "chapterId",
                            "emotionalReview",
                            "id",
                        ),
                ),
            )
        }
}
