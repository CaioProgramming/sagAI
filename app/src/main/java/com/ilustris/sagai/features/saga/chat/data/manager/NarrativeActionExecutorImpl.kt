package com.ilustris.sagai.features.saga.chat.data.manager

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeActionExecutor
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeExecutionEnvironment
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeExecutionResult
import com.ilustris.sagai.features.saga.datasource.MessageDao
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class NarrativeActionExecutorImpl
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val chapterUseCase: ChapterUseCase,
        private val timelineUseCase: TimelineUseCase,
        private val actUseCase: ActUseCase,
        private val genreConfigService: GenreConfigService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
        private val messageDao: MessageDao,
    ) : NarrativeActionExecutor {
        override suspend fun execute(
            action: NarrativeAction,
            environment: NarrativeExecutionEnvironment,
        ): NarrativeExecutionResult {
            val result =
                when (action) {
                    NarrativeAction.CreateAct -> {
                        createAct(environment)
                    }

                    is NarrativeAction.GenerateActIntro -> {
                        generateActIntroduction(
                            action.act,
                            environment,
                        )
                    }

                    is NarrativeAction.CreateChapter -> {
                        startChapter(action.act, environment)
                    }

                    is NarrativeAction.GenerateChapter -> {
                        updateChapter(action.chapter, environment)
                    }

                    is NarrativeAction.GenerateChapterIntro -> {
                        generateChapterIntroduction(action.chapter, environment)
                    }

                    is NarrativeAction.CreateTimeline -> {
                        startTimeline(action.chapter, environment)
                    }

                    is NarrativeAction.EnsureTimelineSceneSummary -> {
                        ensureTimelineSceneSummary(action.timeline, environment)
                    }

                    is NarrativeAction.EvolveTimeline -> {
                        updateTimeline(action.timeline, environment)
                    }

                    is NarrativeAction.CloseTimeline -> {
                        endTimeline(action.chapter)
                    }

                    is NarrativeAction.GenerateAct -> {
                        updateAct(action.act, environment)
                    }

                    is NarrativeAction.GenerateEnding -> {
                        createEndingMessage(
                            action.saga.data.id,
                            environment,
                        )
                    }
                }
            return result.toNarrativeExecutionResult()
        }

        private fun RequestResult<Any>.toNarrativeExecutionResult(): NarrativeExecutionResult =
            when (this) {
                is RequestResult.Success -> {
                    NarrativeExecutionResult.Success(
                        value = value,
                        shouldEmitMilestone = true,
                    )
                }

                is RequestResult.Error -> {
                    NarrativeExecutionResult.Failure(
                        message = value.message ?: "Unknown error",
                        canRetry = true,
                    )
                }
            }

        private suspend fun createAct(environment: NarrativeExecutionEnvironment) =
            executeRequest {
                val saga = environment.getSagaMetadata() ?: error("Saga not available")
                val fullSaga = environment.getSagaContent() ?: error("Saga content not available")
                environment.getMessageCount(saga.data.id)
                val rules = environment.fetchNarrativeRules()
                val lastAct = fullSaga.currentActInfo
                if (lastAct?.isComplete(rules)?.not() == true) {
                    sagaHistoryUseCase.updateSaga(
                        saga.data.copy(currentActId = lastAct.data.id),
                    )
                    error("Act is already set at this saga")
                }
                val newAct =
                    actUseCase.saveAct(
                        Act(
                            sagaId = saga.data.id,
                        ),
                    )
                sagaHistoryUseCase.updateSaga(
                    saga.data.copy(currentActId = newAct.id),
                )
                newAct
            }

        private suspend fun generateActIntroduction(
            currentAct: ActContent,
            environment: NarrativeExecutionEnvironment,
        ) = executeRequest {
            val saga = environment.getSagaMetadata() ?: error("Saga not available")
            var finalAct: GeneratedContent<Act>? = null
            actUseCase
                .generateActIntroductionStream(saga, currentAct.data)
                .let { flow ->
                    reasoningSynthesizerService.synthesizeReasoning(
                        sourceFlow = flow,
                        context = "Generating act introduction for ${saga.data.title}",
                        conversationStyle = genreConfigService.conversationBlueprint(saga.data.genre),
                        genre = saga.data.genre.name,
                    )
                }.collect { state ->
                    when (state) {
                        is StreamingState.Reasoning -> environment.onReasoningChunk(state.chunk)
                        is StreamingState.Success -> finalAct = state.data
                        is StreamingState.Error -> throw Exception(state.message)
                    }
                }
            environment.onReasoningChunk(null)
            finalAct!!
        }

        private suspend fun startChapter(
            act: ActContent,
            environment: NarrativeExecutionEnvironment,
        ) = executeRequest {
            val fullSaga = environment.getSagaContent() ?: error("Saga content not available")
            val latestAct = fullSaga.acts.find { it.data.id == act.data.id } ?: act
            val rules = environment.fetchNarrativeRules()
            val lastChapter = latestAct.chapters.lastOrNull()
            if (lastChapter?.isComplete(rules)?.not() == true) {
                actUseCase.updateAct(latestAct.data.copy(currentChapterId = lastChapter.data.id))
                throw IllegalArgumentException("Chapter is already set at this act")
            }
            chapterUseCase.saveChapter(Chapter(actId = latestAct.data.id))
        }

        private suspend fun updateChapter(
            chapter: ChapterContent,
            environment: NarrativeExecutionEnvironment,
        ) = executeRequest {
            val saga = environment.getSagaMetadata() ?: error("Saga not available")
            environment.dismissMilestone()
            var generated: GeneratedContent<Chapter>? = null
            chapterUseCase.synthesizeChapterEvolutionStream(chapter.data.id).collect { state ->
                when (state) {
                    is StreamingState.Reasoning -> {
                        environment.onReasoningChunk(state.chunk)
                    }

                    is StreamingState.Success -> {
                        generated = state.data
                        environment.onReasoningChunk(null)
                    }

                    is StreamingState.Error -> {
                        environment.onReasoningChunk(null)
                        if (state.isFlowCancellation()) {
                            throw CancellationException(state.message)
                        }
                        error(state.message)
                    }
                }
            }
            generated ?: error("Failed to generate chapter synthesis")
        }

        private suspend fun generateChapterIntroduction(
            currentChapter: ChapterContent,
            environment: NarrativeExecutionEnvironment,
        ) = executeRequest {
            val saga = environment.getSagaMetadata() ?: error("Saga not available")
            var finalChapter: GeneratedContent<Chapter>? = null
            chapterUseCase
                .generateChapterIntroductionStream(currentChapter.data.id)
                .let { flow ->
                    reasoningSynthesizerService.synthesizeReasoning(
                        sourceFlow = flow,
                        context = "Generating chapter introduction for ${currentChapter.data.title}",
                        conversationStyle = genreConfigService.conversationBlueprint(saga.data.genre),
                        genre = saga.data.genre.name,
                    )
                }.collect { state ->
                    when (state) {
                        is StreamingState.Reasoning -> environment.onReasoningChunk(state.chunk)
                        is StreamingState.Success -> finalChapter = state.data
                        is StreamingState.Error -> throw Exception(state.message)
                    }
                }
            environment.onReasoningChunk(null)
            finalChapter!!
        }

        private suspend fun ensureTimelineSceneSummary(
            timeline: TimelineContent,
            environment: NarrativeExecutionEnvironment,
        ) = executeRequest {
            val saga = environment.getSagaMetadata() ?: error("Saga not available")
            when (val objectiveResult = timelineUseCase.getTimelineObjective(saga, timeline.data)) {
                is RequestResult.Success -> objectiveResult.value
                is RequestResult.Error -> throw objectiveResult.value
            }
        }

        private suspend fun startTimeline(
            currentChapter: ChapterContent,
            environment: NarrativeExecutionEnvironment,
        ) = executeRequest {
            val rules = environment.fetchNarrativeRules()
            val lastTimeline = currentChapter.events.lastOrNull()
            if (lastTimeline?.isComplete(rules)?.not() == true) {
                chapterUseCase.updateChapter(
                    currentChapter.data.copy(
                        currentEventId = lastTimeline.data.id,
                    ),
                )
                throw IllegalArgumentException("Timeline already set at this chapter")
            }

            val savedTimeline =
                timelineUseCase.saveTimeline(
                    Timeline(
                        chapterId = currentChapter.data.id,
                    ),
                )
            chapterUseCase.updateChapter(
                currentChapter.data.copy(currentEventId = savedTimeline.id),
            )

            val saga = environment.getSagaMetadata() ?: error("Saga not available")
            when (val objectiveResult = timelineUseCase.getTimelineObjective(saga, savedTimeline)) {
                is RequestResult.Success -> objectiveResult.value
                is RequestResult.Error -> throw objectiveResult.value
            }
        }

        private suspend fun updateTimeline(
            timeline: TimelineContent,
            environment: NarrativeExecutionEnvironment,
        ) = executeRequest {
            val saga = environment.getSagaMetadata() ?: error("Saga not available")
            val rules = environment.fetchNarrativeRules()
            environment.getMessageCount(saga.data.id)
            if (timeline.isComplete(rules)) {
                error("Timeline already completed")
            } else {
                environment.dismissMilestone()
                var generated: GeneratedContent<Timeline>? = null
                timelineUseCase.generateFullLoreUpdateStream(saga, timeline.data).collect { state ->
                    when (state) {
                        is StreamingState.Reasoning -> {
                            environment.onReasoningChunk(state.chunk)
                        }

                        is StreamingState.Success -> {
                            generated = state.data
                            environment.onReasoningChunk(null)
                        }

                        is StreamingState.Error -> {
                            environment.onReasoningChunk(null)
                            if (state.isFlowCancellation()) {
                                throw CancellationException(state.message)
                            }
                            error(state.message)
                        }
                    }
                }
                generated ?: error("Failed to generate timeline update")
            }
        }

        private suspend fun endTimeline(currentChapter: ChapterContent) =
            executeRequest {
                chapterUseCase.updateChapter(
                    currentChapter.data.copy(
                        currentEventId = null,
                    ),
                )
            }

        private suspend fun updateAct(
            currentAct: ActContent,
            environment: NarrativeExecutionEnvironment,
        ) = executeRequest {
            val saga = environment.getSagaMetadata() ?: error("Saga not available")
            Timber.d("updating act(${currentAct.data.id})")
            if (environment.isDebugMode()) {
                GeneratedContent(
                    Act(
                        id = currentAct.data.id,
                        title = "Updated Act ${saga.acts.size}",
                        content = "This act was updated in debug mode.",
                        sagaId = saga.data.id,
                    ),
                    "Fake act finished!",
                )
            } else {
                environment.dismissMilestone()
                var generated: GeneratedContent<Act>? = null
                val fullSaga = environment.getSagaContent() ?: error("Saga content not available")
                actUseCase.synthesizeActEvolutionStream(fullSaga, currentAct).collect { state ->
                    when (state) {
                        is StreamingState.Reasoning -> {
                            environment.onReasoningChunk(state.chunk)
                        }

                        is StreamingState.Success -> {
                            generated = state.data
                            environment.onReasoningChunk(null)
                        }

                        is StreamingState.Error -> {
                            environment.onReasoningChunk(null)
                            if (state.isFlowCancellation()) {
                                throw CancellationException(state.message)
                            }
                            error(state.message)
                        }
                    }
                }
                generated ?: error("Failed to generate act synthesis")
            }
        }

        private suspend fun createEndingMessage(
            sagaId: Int,
            environment: NarrativeExecutionEnvironment,
        ) = executeRequest {
            val saga = environment.getSagaMetadata() ?: error("Saga not available")
            if (environment.isDebugMode()) {
                sagaHistoryUseCase.updateSaga(
                    saga.data.copy(
                        endMessage = "Congratulations on completing this saga!",
                        isEnded = true,
                        endedAt = System.currentTimeMillis(),
                    ),
                )
            } else {
                environment.dismissMilestone()
                val fullSaga =
                    sagaHistoryUseCase.getSagaById(sagaId).first()
                        ?: error("Saga not found")
                val contextString = "Concluding your legend and weaving the final threads of fate..."
                val style = genreConfigService.conversationBlueprint(saga.data.genre)
                var generated: GeneratedContent<com.ilustris.sagai.features.home.data.model.SagaEnding>? =
                    null
                reasoningSynthesizerService
                    .synthesizeReasoning(
                        sourceFlow = sagaHistoryUseCase.generateSagaEndingStream(fullSaga),
                        context = contextString,
                        conversationStyle = style,
                        genre = saga.data.genre.name,
                    ).collect { state ->
                        when (state) {
                            is StreamingState.Reasoning -> {
                                environment.onReasoningChunk(state.chunk)
                            }

                            is StreamingState.Success -> {
                                generated =
                                    state.data as? GeneratedContent<com.ilustris.sagai.features.home.data.model.SagaEnding>
                                environment.onReasoningChunk(null)
                            }

                            is StreamingState.Error -> {
                                environment.onReasoningChunk(null)
                                error(state.message)
                            }
                        }
                    }
                val ending = generated?.data ?: error("Failed to generate ending")
                sagaHistoryUseCase.updateSaga(
                    saga.data.copy(
                        endMessage = ending.endingMessage,
                        isEnded = true,
                        endedAt = System.currentTimeMillis(),
                        emotionalProfile = ending.emotionalProfile,
                        emotionalReview = ending.emotionalProfile.emotionalContent,
                    ),
                )
            }
        }
    }
