package com.ilustris.sagai.features.timeline.data.repository

import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineWithAct
import kotlinx.coroutines.flow.Flow

interface TimelineRepository {
    suspend fun saveTimeline(timeline: Timeline): Timeline

    suspend fun getTimeline(id: String): Flow<Timeline>

    suspend fun getAllTimelines(): Flow<List<Timeline>>

    suspend fun deleteTimeline(timeline: Timeline)

    suspend fun updateTimeline(timeline: Timeline)

    fun getTimelineWithActBySaga(sagaId: Int): Flow<List<TimelineWithAct>>
}
