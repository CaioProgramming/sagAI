package com.ilustris.sagai.features.saga.chat.data.mapper

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.toSagaInfo
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChapterDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.TimelineDisplayData
import com.ilustris.sagai.features.timeline.domain.TimelineMapper
import javax.inject.Inject

class SagaMetadataUIMapper
    @Inject
    constructor(
        private val timelineMapper: TimelineMapper,
    ) {
        suspend fun mapToActDisplayData(
            saga: SagaMetadata,
            rules: NarrativeRules,
        ): List<ActDisplayData> =
            saga.acts.asReversed().map { actContentDomain ->

                ActDisplayData(
                    content = actContentDomain,
                    isComplete = actContentDomain.isComplete(rules),
                    chapters =
                        actContentDomain.chapters.asReversed().map { chapterContentDomain ->
                            ChapterDisplayData(
                                chapter = chapterContentDomain,
                                isComplete = chapterContentDomain.isComplete(rules),
                                timelineSummaries =
                                    chapterContentDomain.events.asReversed().map {
                                        TimelineDisplayData(
                                            isComplete = it.isComplete(rules),
                                            timeline =
                                                timelineMapper.buildTimeline(
                                                    saga = saga.toSagaInfo(),
                                                    timelineContent =
                                                        it.copy(
                                                            messages =
                                                                it.messages.sortedByDescending {
                                                                    it.message.timestamp
                                                                },
                                                        ),
                                                ),
                                        )
                                    },
                            )
                        },
                )
            }
    }
