package com.ilustris.sagai.features.act.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.chapterNumber

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
    fun isFull(): Boolean = chapters.count { it.isComplete() } >= UpdateRules.ACT_UPDATE_LIMIT

    fun isComplete(): Boolean =
        isFull() &&
            data.title.isNotEmpty() &&
            data.content.isNotEmpty()

    fun emotionalSummary(sagaContent: SagaContent) =
        buildString {
            appendLine("${sagaContent.actNumber(data)} Act ${data.title}")
            appendLine("Emotional notes for chapters in this act:")
            chapters.forEach {
                appendLine("${sagaContent.chapterNumber(it.data)} Chapter ${it.data.title}: ${it.data.emotionalReview}")
            }
        }

    fun actSummary(saga: SagaContent) =
        buildString {
            appendLine("${saga.actNumber(data)} Act ${data.title}")
            appendLine("Introduction: ")
            appendLine(data.introduction)
            appendLine("Chapters: ")
            chapters.forEach {
                appendLine("${saga.chapterNumber(it.data)}: ${it.data.title}")
                appendLine("Introduction: ")
                appendLine(it.data.introduction)
                appendLine("Overview: ")
                appendLine(it.data.overview)
            }
            appendLine("Overview: ")
            appendLine(data.content)
        }
}
