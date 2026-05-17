package com.ilustris.sagai.features.chapter.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
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
    fun isFull(
        updateLimit: Int,
        narrativeRules: NarrativeRules,
    ): Boolean = events.count { it.isComplete(narrativeRules) } >= updateLimit

    fun isComplete(narrativeRules: NarrativeRules): Boolean =
        isFull(narrativeRules.chapterUpdateLimit, narrativeRules) &&
            data.title.isNotEmpty() &&
            data.overview.isNotEmpty()

    fun fetchCharacters(saga: SagaContent) =
        this.data.featuredCharacters.map {
            saga.findCharacter(it)
        }

    fun fetchChapterMessages() = events.flatMap { it.messages }

    fun fetchChapterWikis() = events.map { it.updatedWikis }.flatten()

    fun toInfo(sagaId: Int) =
        ChapterInfo(
            id = data.id,
            title = data.title,
            overview = data.overview,
            coverImage = data.coverImage,
            actId = data.actId,
            sagaId = sagaId,
            featuredCharacters = data.featuredCharacters,
            emotionalReview = data.emotionalReview,
            createdAt = data.createdAt,
        )
}
