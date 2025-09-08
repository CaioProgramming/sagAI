package com.ilustris.sagai.features.timeline.domain

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.domain.usecase.EmotionalUseCase
import javax.inject.Inject

class TimelineUseCaseImpl
    @Inject
    constructor(
        private val timelineRepository: TimelineRepository,
        private val emotionalUseCase: EmotionalUseCase,
    ) : TimelineUseCase {
        override suspend fun getAllTimelines() = timelineRepository.getAllTimelines()

        override suspend fun getTimeline(id: String) = timelineRepository.getTimeline(id)

        override suspend fun saveTimeline(timeline: Timeline) = timelineRepository.saveTimeline(timeline)

        override suspend fun updateTimeline(timeline: Timeline): Timeline {
            timelineRepository.updateTimeline(timeline)
            return timeline
        }

        override suspend fun deleteTimeline(timeline: Timeline) {
            timelineRepository.deleteTimeline(timeline)
        }

        override suspend fun createTimelineReview(
            content: SagaContent,
            timelineContent: TimelineContent,
        ): RequestResult<Exception, Unit> =
            try {
                val userMessages =
                    timelineContent.messages.map { it.joinMessage(showType = true).formatToString() }

                val emotionalReview = emotionalUseCase.generateEmotionalReview(userMessages).getSuccess()!!

                timelineRepository
                    .updateTimeline(
                        timelineContent.data.copy(
                            emotionalReview = emotionalReview,
                        ),
                    ).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
    }
