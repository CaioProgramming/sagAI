package com.ilustris.sagai.features.timeline.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.LorePrompts
import com.ilustris.sagai.core.ai.prompts.TimelinePrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import javax.inject.Inject

class TimelineUseCaseImpl
    @Inject
    constructor(
        private val timelineRepository: TimelineRepository,
        private val emotionalUseCase: EmotionalUseCase,
        private val gemmaClient: GemmaClient,
    ) : TimelineUseCase {
        override suspend fun getAllTimelines() = timelineRepository.getAllTimelines()

        override suspend fun getTimeline(id: String) = timelineRepository.getTimeline(id)

        override suspend fun generateTimeline(
            saga: SagaContent,
            currentTimeline: TimelineContent,
        ) = executeRequest {
            val newLore =
                gemmaClient
                    .generate<Timeline>(
                        LorePrompts.loreGeneration(
                            saga,
                            currentTimeline,
                        ),
                        skipRunning = true,
                        describeOutput = false,
                    )!!

            updateTimeline(
                currentTimeline.data.copy(
                    title = newLore.title,
                    content = newLore.content,
                ),
            )
        }

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
        ): RequestResult<Timeline> =
            executeRequest {
                val userMessages =
                    timelineContent.messages.map { it.joinMessage(showType = true).formatToString() }

                val emotionalToneRanking: Map<String, Int> =
                    timelineContent.messages
                        .filter {
                            it.message.senderType == SenderType.USER ||
                                it.message.characterId == content.mainCharacter?.data?.id
                        }.groupBy { it.message.emotionalTone.toString() }
                        .mapValues { entry -> entry.value.size }

                val emotionalReview =
                    emotionalUseCase
                        .generateEmotionalReview(
                            userMessages,
                            emotionalToneRanking,
                        ).getSuccess()!!

                updateTimeline(
                    timelineContent.data.copy(
                        emotionalReview = emotionalReview,
                    ),
                )
            }

        override suspend fun getTimelineObjective(saga: SagaContent): RequestResult<String> =
            executeRequest {
                val objectivePrompt =
                    ChatPrompts.sceneSummarizationPrompt(
                        saga,
                        saga.flatMessages().takeLast(UpdateRules.LORE_UPDATE_LIMIT).map {
                            it.joinMessage(true).formatToString(true)
                        },
                    )
                gemmaClient.generate<SceneSummary>(objectivePrompt, skipRunning = true)!!.immediateObjective!!
            }
    }
