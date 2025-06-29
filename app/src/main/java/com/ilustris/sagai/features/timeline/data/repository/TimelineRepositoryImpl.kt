package com.ilustris.sagai.features.timeline.data.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.timeline.data.model.Timeline
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import kotlin.getValue

class TimelineRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : TimelineRepository {
        private val timelineDao by lazy {
            database.timelineDao()
        }

        override suspend fun saveTimeline(timeline: Timeline) =
            timeline.copy(
                id =
                    timelineDao
                        .saveTimeline(
                            timeline
                                .copy(id = 0, createdAt = Calendar.getInstance().timeInMillis),
                        ).toInt(),
            )

        override suspend fun getTimeline(id: String): Flow<Timeline> = timelineDao.getTimeline(id)

        override suspend fun getAllTimelines(): Flow<List<Timeline>> = timelineDao.getAllTimelines()

        override suspend fun deleteTimeline(timeline: Timeline) = timelineDao.deleteTimeline(timeline)

        override suspend fun updateTimeline(timeline: Timeline) = timelineDao.updateTimeline(timeline)

        override suspend fun getTimelineForSaga(sagaId: String) = timelineDao.getTimelineForSaga(sagaId)
    }
