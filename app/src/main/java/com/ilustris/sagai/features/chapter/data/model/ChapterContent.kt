package com.ilustris.sagai.features.chapter.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

data class ChapterContent(
    @Embedded
    val data: Chapter,
    @Relation(
        parentColumn = "id",
        entityColumn = "chapterId",
        entity = Timeline::class,
    )
    val events: List<TimelineContent> = emptyList(),
    @Relation(
        parentColumn = "currentEventId",
        entityColumn = "id",
        entity = Timeline::class,
    )
    val currentEventInfo: TimelineContent? = null,
) {
    fun isFull(): Boolean = events.count { it.isComplete() } >= UpdateRules.CHAPTER_UPDATE_LIMIT

    fun isComplete(): Boolean =
        isFull() &&
            data.title.isNotEmpty() &&
            data.overview.isNotEmpty()

    fun fetchCharacters(saga: SagaContent) = saga.getCharacters().filter { this.data.featuredCharacters.contains(it.id) }

    fun fetchChapterMessages() = events.flatMap { it.messages }

    fun fetchChapterWikis() = events.map { it.updatedWikis }.flatten()
}
