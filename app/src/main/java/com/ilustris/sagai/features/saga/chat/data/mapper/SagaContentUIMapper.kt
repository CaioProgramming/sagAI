package com.ilustris.sagai.features.saga.chat.data.mapper

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChapterDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.TimelineDisplayData
import com.ilustris.sagai.features.timeline.domain.TimelineMapper
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import javax.inject.Inject

class SagaContentUIMapper
    @Inject
    constructor(
        private val timelineMapper: TimelineMapper,
    ) {
        suspend fun mapToActDisplayData(
            saga: SagaContent,
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
                                                    saga = saga,
                                                    timelineContent =
                                                        it.copy(
                                                            messages =
                                                                it.messages.sortedBy {
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
