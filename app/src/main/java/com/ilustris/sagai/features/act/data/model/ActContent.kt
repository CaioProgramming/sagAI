package com.ilustris.sagai.features.act.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.ai.prompts.LorePrompts
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent

data class ActContent(
    @Embedded
    val data: Act,
    @Relation(
        parentColumn = "currentChapterId",
        entityColumn = "id",
        entity = Chapter::class,
    )
    val currentChapterInfo: ChapterContent? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "actId",
        entity = Chapter::class,
    )
    val chapters: List<ChapterContent> = emptyList(),
) {
    fun isFull(
        actLimit: Int,
        rules: NarrativeRules,
    ): Boolean = chapters.count { it.isComplete(rules) } >= actLimit

    fun isComplete(rules: NarrativeRules): Boolean =
        isFull(rules.actUpdateLimit, rules) &&
            data.title.isNotEmpty() &&
            data.content.isNotEmpty()

    fun emotionalSummary() =
        buildString {
            appendLine(
                this.toAINormalize(
                    listOf(
                        "id",
                        "currentChapterId",
                        "sagaId",
                        "actId",
                        "featuredCharacters",
                        "coverImage",
                        "createdAt",
                        "content",
                    ),
                ),
            )
            chapters.normalizetoAIItems(
                listOf(
                    "id",
                    "currentEventId",
                    "sagaId",
                    "actId",
                    "featuredCharacters",
                    "coverImage",
                    "createdAt",
                    "overview",
                ),
            )
        }

    fun actSummary(showEvents: Boolean = true) =
        buildString {
            appendLine(
                data.toAINormalize(LorePrompts.ACT_EXCLUDED_FIELDS),
            )
            appendLine("CHAPTERS in this act:")
            chapters.forEach {
                appendLine(
                    it.data.toAINormalize(
                        LorePrompts.CHAPTER_EXCLUDED_FIELDS,
                    ),
                )
                if (showEvents) {
                    appendLine(
                        it.events.map { it.data }.normalizetoAIItems(
                            LorePrompts.TIMELINE_EXCLUDED_FIELDS,
                        ),
                    )
                }
            }
        }
}
