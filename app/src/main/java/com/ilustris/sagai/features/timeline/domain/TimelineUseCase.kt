package com.ilustris.sagai.features.timeline.domain

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import kotlinx.coroutines.flow.Flow

interface TimelineUseCase {
    suspend fun getAllTimelines(): Flow<List<Timeline>>

    suspend fun getTimeline(id: String): Flow<Timeline>

    suspend fun saveTimeline(timeline: Timeline): Timeline

    suspend fun updateTimeline(timeline: Timeline): Timeline

    suspend fun deleteTimeline(timeline: Timeline)

    suspend fun createTimelineReview(
        content: SagaContent,
        timelineContent: TimelineContent,
    ): RequestResult<Exception, String>
}
