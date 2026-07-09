package com.ilustris.sagai.features.saga.detail.review.domain

import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeProcessingGate
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.core.globalshell.ReviewReadyEffect
import com.ilustris.sagai.features.saga.detail.data.model.completedStepCount
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.data.usecase.ReviewState
import com.ilustris.sagai.features.saga.detail.review.domain.model.ReviewSteps
import com.ilustris.sagai.features.saga.detail.review.domain.model.isPresentIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class ReviewGenerationCoordinator
    @Inject
    constructor(
        private val reviewUseCase: SagaReviewUseCase,
        private val sagaRepository: SagaRepository,
        private val narrativeProcessingGate: NarrativeProcessingGate,
        private val globalShellService: GlobalShellService,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val jobs = ConcurrentHashMap<Int, Job>()
        private val states = ConcurrentHashMap<Int, MutableStateFlow<ReviewGenerationState>>()
        private val globalStepMutex = Mutex()

        fun stateFor(sagaId: Int): StateFlow<ReviewGenerationState> =
            states.getOrPut(sagaId) { MutableStateFlow(ReviewGenerationState.Idle) }.asStateFlow()

        fun enqueue(sagaId: Int) {
            if (jobs[sagaId]?.isActive == true) return
            jobs[sagaId] =
                scope.launch {
                    try {
                        runGeneration(sagaId)
                    } finally {
                        jobs.remove(sagaId)
                    }
                }
        }

        fun cancel(sagaId: Int) {
            jobs.remove(sagaId)?.cancel()
            states[sagaId]?.value = ReviewGenerationState.Idle
        }

        private suspend fun runGeneration(sagaId: Int) {
            val stateFlow = states.getOrPut(sagaId) { MutableStateFlow(ReviewGenerationState.Idle) }
            var content = sagaRepository.getSagaById(sagaId).first() ?: return
            val existingReview = content.data.review

            if (existingReview.isComplete()) {
                stateFlow.value = ReviewGenerationState.Complete
                globalShellService.post(
                    ReviewReadyEffect(
                        sagaId = sagaId,
                        sagaTitle = content.data.title,
                        genre = content.data.genre,
                        deepLink = "saga://saga_detail/$sagaId",
                    ),
                )
                return
            }

            stateFlow.value =
                ReviewGenerationState.Generating(
                    step = null,
                    reasoning = null,
                    completedCount = existingReview?.completedStepCount() ?: 0,
                )

            try {
                globalStepMutex.withLock {
                    for (step in ReviewSteps.entries) {
                        content = sagaRepository.getSagaById(sagaId).first() ?: return@withLock
                        if (step.isPresentIn(content.data.review)) continue

                        waitForNarrativeIdle()

                        reviewUseCase
                            .generateStep(content, step, content.data.review)
                            .collectLatest { reviewState ->
                                when (reviewState) {
                                    is ReviewState.Loading -> {
                                        stateFlow.value =
                                            ReviewGenerationState.Generating(
                                                step = reviewState.step,
                                                reasoning = reviewState.message,
                                                completedCount =
                                                    content.data.review?.completedStepCount() ?: 0,
                                            )
                                    }

                                    is ReviewState.StepComplete -> {
                                        content =
                                            sagaRepository.getSagaById(sagaId).first()
                                                ?: content.copy(data = reviewState.saga)
                                        stateFlow.value =
                                            ReviewGenerationState.Generating(
                                                step = reviewState.step,
                                                reasoning = null,
                                                completedCount =
                                                    reviewState.saga.review?.completedStepCount() ?: 0,
                                            )
                                    }

                                    is ReviewState.Success -> {
                                        stateFlow.value = ReviewGenerationState.Complete
                                    }

                                    is ReviewState.Error -> {
                                        stateFlow.value =
                                            ReviewGenerationState.Error(
                                                message = reviewState.message,
                                                step = reviewState.step,
                                            )
                                        error(reviewState.message)
                                    }
                                }
                            }
                    }
                }

                val finalReview =
                    sagaRepository
                        .getSagaById(sagaId)
                        .first()
                        ?.data
                        ?.review
                stateFlow.value =
                    if (finalReview.isComplete()) {
                        ReviewGenerationState.Complete
                    } else {
                        ReviewGenerationState.Idle
                    }

                    if (stateFlow.value == ReviewGenerationState.Complete) {
                        globalShellService.post(
                            ReviewReadyEffect(
                                sagaId = sagaId,
                                sagaTitle = content.data.title,
                                genre = content.data.genre,
                                deepLink = "saga://saga_detail/$sagaId",
                            ),
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "Review generation failed for saga $sagaId")
                stateFlow.value =
                    ReviewGenerationState.Error(
                        message = e.message ?: "Unknown error",
                        step = null,
                    )
            }
        }

        private suspend fun waitForNarrativeIdle() {
            while (narrativeProcessingGate.isNarrativeProcessing.value) {
                delay(250.milliseconds)
            }
        }
    }
