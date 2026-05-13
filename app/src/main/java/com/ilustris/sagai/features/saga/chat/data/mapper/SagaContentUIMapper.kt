package com.ilustris.sagai.features.saga.chat.data.mapper

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.toSagaInfo
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCheck
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeStep
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChapterDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.TimelineDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.model.PendingAdvance
import com.ilustris.sagai.features.timeline.domain.TimelineMapper
import javax.inject.Inject

class SagaMetadataUIMapper
    @Inject
    constructor(
        private val timelineMapper: TimelineMapper,
    ) {
        fun computePendingAdvance(
            saga: SagaContent,
            rules: NarrativeRules,
        ): PendingAdvance? =
            when (val step = NarrativeCheck.validateProgression(saga, rules)) {
                is NarrativeStep.GenerateTimeLine -> {
                    PendingAdvance.NewEvent(step.timeline)
                }

                is NarrativeStep.GenerateChapter -> {
                    PendingAdvance.NewChapter(step.chapter)
                }

                is NarrativeStep.GenerateAct -> {
                    PendingAdvance.NewAct(step.act)
                }

                is NarrativeStep.StartAct -> {
                    PendingAdvance.StartAct
                }

                is NarrativeStep.GenerateActIntroduction -> {
                    PendingAdvance.NewActIntroduction(step.act)
                }

                is NarrativeStep.GenerateChapterIntroduction -> {
                    PendingAdvance.NewChapterIntroduction(
                        step.chapter,
                    )
                }

                is NarrativeStep.StartChapter -> {
                    PendingAdvance.StartChapter(step.act)
                }

                is NarrativeStep.StartTimeline -> {
                    PendingAdvance.StartStory(step.chapter)
                }

                is NarrativeStep.GenerateSagaEnding -> {
                    PendingAdvance.SagaEnding(step.saga)
                }

                else -> {
                    null
                }
            }

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
