package com.ilustris.sagai.features.timeline.domain

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineWithAct
import kotlinx.coroutines.flow.Flow

interface TimelineUseCase {
    suspend fun getAllTimelines(): Flow<List<Timeline>>

    suspend fun getTimeline(id: String): Flow<Timeline>

    suspend fun generateFullLoreUpdate(
        saga: SagaMetadata,
        timeline: Timeline,
    ): RequestResult<Unit>

    fun generateFullLoreUpdateStream(
        saga: SagaMetadata,
        timeline: Timeline,
    ): Flow<StreamingState<GeneratedContent<Timeline>>>

    fun generateTimelineStream(
        saga: SagaMetadata,
        currentTimeline: Timeline,
    ): Flow<StreamingState<GeneratedContent<Timeline>>>

    suspend fun saveTimeline(timeline: Timeline): Timeline

    suspend fun updateTimeline(timeline: Timeline): Timeline

    suspend fun deleteTimeline(timeline: Timeline)

    suspend fun getTimelineObjective(
        saga: SagaMetadata,
        timeline: Timeline,
    ): RequestResult<Timeline>

    suspend fun generateTimelineContent(
        saga: SagaMetadata,
        timeline: Timeline,
    ): RequestResult<Unit>

    fun getTimelineWithActBySaga(sagaId: Int): Flow<List<TimelineWithAct>>
}
