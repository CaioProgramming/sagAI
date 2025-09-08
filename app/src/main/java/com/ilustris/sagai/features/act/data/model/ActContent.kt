package com.ilustris.sagai.features.act.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.narrative.UpdateRules
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
    fun isFull(): Boolean = chapters.count { it.isComplete() } >= UpdateRules.ACT_UPDATE_LIMIT

    fun isComplete(): Boolean =
        isFull() &&
            data.title.isNotEmpty() &&
            data.content.isNotEmpty()
}
