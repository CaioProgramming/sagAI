package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.features.chapter.data.model.ChapterContent

object TimelinePrompts {
    val timelineExclusions = listOf("id", "chapterId", "createdAt")

    fun timeLineDetails(currentChapter: ChapterContent?) =
        buildString {
            val events =
                currentChapter
                    ?.events
                    ?.filter { it.isComplete() }
                    ?.map { it.data }?.takeLast(5)
            if (events?.isNotEmpty() == true) {
                appendLine("**Most Recent Events:**")
                appendLine("// This section provides the most recent events from the chapter's timeline.")
                appendLine("// Use this to understand the immediate plot progression and current situation.")
                appendLine(events.normalizetoAIItems(timelineExclusions))
            }
        }

}
