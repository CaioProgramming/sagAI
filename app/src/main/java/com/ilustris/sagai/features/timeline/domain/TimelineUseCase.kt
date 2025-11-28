package com.ilustris.sagai.features.timeline.domain

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import kotlinx.coroutines.flow.Flow

interface TimelineUseCase {
    suspend fun getAllTimelines(): Flow<List<Timeline>>

    suspend fun getTimeline(id: String): Flow<Timeline>

    suspend fun generateTimeline(
        saga: SagaContent,
        currentTimeline: TimelineContent,
    ): RequestResult<Timeline>

    suspend fun saveTimeline(timeline: Timeline): Timeline

    suspend fun updateTimeline(timeline: Timeline): Timeline

    suspend fun deleteTimeline(timeline: Timeline)

    suspend fun generateEmotionalReview(
        saga: SagaContent,
        timeline: TimelineContent,
    ): RequestResult<Timeline>

    suspend fun getTimelineObjective(
        saga: SagaContent,
        timelineContent: Timeline,
    ): RequestResult<Timeline>

    suspend fun generateTimelineContent(
        saga: SagaContent,
        timelineContent: TimelineContent,
    ): RequestResult<Unit>
}
