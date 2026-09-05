package com.ilustris.sagai.features.saga.milestone.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.services.AdTier
import com.ilustris.sagai.core.services.AdsService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.features.act.BookGenerationService
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.getChapterCovers
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCheck
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeError
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativePhase
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class NarrativeSnapshot(
    val phase: NarrativePhase,
    val lastError: NarrativeError?,
    val milestone: SagaMilestone?,
    val reasoning: String?,
)

@HiltViewModel
class MilestoneViewModel
    @Inject
    constructor(
        private val sagaContentManager: SagaContentManager,
        private val remoteConfigService: RemoteConfigService,
        private val bookGenerationService: BookGenerationService,
        private val settingsUseCase: SettingsUseCase,
        private val adsService: AdsService,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<MilestoneUiState>(MilestoneUiState.Loading())
        val uiState: StateFlow<MilestoneUiState> = _uiState.asStateFlow()

        val genre: StateFlow<Genre?> =
            sagaContentManager.content
                .map { it?.data?.genre }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        val sagaData: StateFlow<Saga?> =
            sagaContentManager.content
                .map { it?.data }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        private val _showOnboarding = MutableStateFlow(false)
        val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

        val chapterCoverImage: StateFlow<String?> =
            combine(uiState, sagaContentManager.content) { state, saga ->
                val chapterId =
                    ((state as? MilestoneUiState.ClosureStep)?.milestone as? SagaMilestone.ChapterFinished)?.chapter?.id
                        ?: return@combine null
                saga
                    ?.acts
                    ?.flatMap { it.chapters }
                    ?.find { it.data.id == chapterId }
                    ?.data
                    ?.coverImage
                    ?.takeIf { it.isNotBlank() }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        val actChapterCovers: StateFlow<List<String>> =
            combine(uiState, sagaContentManager.content) { state, saga ->
                val actId =
                    ((state as? MilestoneUiState.ClosureStep)?.milestone as? SagaMilestone.ActFinished)?.act?.id
                        ?: return@combine emptyList()
                saga
                    ?.acts
                    ?.find { it.data.id == actId }
                    ?.getChapterCovers()
                    ?: emptyList()
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val bookGenerationState: StateFlow<BookGenerationUiState> = bookGenerationService.uiState

        private val _finished = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
        val finished: SharedFlow<Unit> = _finished

        private var currentSagaId: Int? = null
        private var driveJob: Job? = null
        private var finishCheckJob: Job? = null
        private var chainStepTotal = 0
        private var chainStepIndex = 0

        // Only the chain's terminal closure step shows an ad (see the tier-selection branch
        // below) — this guards against a second show if a step past the original estimate is
        // also deemed terminal (chainStepTotal is a projection, not a guarantee).
        private var adShownThisChain = false

        fun start(sagaId: Int) {
            if (currentSagaId == sagaId) return
            currentSagaId = sagaId
            driveJob?.cancel()
            finishCheckJob?.cancel()
            chainStepTotal = 0
            chainStepIndex = 0
            adShownThisChain = false
            _uiState.value = MilestoneUiState.Loading()
            _showOnboarding.value = false

            driveJob =
                viewModelScope.launch {
                    // Cleans up any leftover orphaned timeline from a past CreateTimeline race
                    // (see SagaContentManagerImpl's progressionMutex fix) before this chain even
                    // starts deciding what's next — the current chapter is exactly where such an
                    // orphan would sit, so the Milestone screen opening is a natural checkpoint
                    // for it, not just a place that reacts to what's already valid.
                    sagaContentManager.pruneOrphanTimelines()

                    val saga = sagaContentManager.content.value
                    saga?.let {
                        chainStepTotal = NarrativeCheck.computeClosureChainLength(it, remoteConfigService.getNarrativeRules())
                    }
                    if (saga?.acts?.isEmpty() == true && settingsUseCase.getShowTutorials().first()) {
                        _showOnboarding.value = true
                    }

                    // Preloaded here so the ad has the whole generation wait to fetch — by the
                    // time a closure milestone actually resolves, the request has had as long as
                    // the LLM call itself took. Both tiers are speculative: only one (at most)
                    // ends up shown, chosen once we know which milestone actually closed the chain.
                    viewModelScope.launch { adsService.preload(AdTier.EVENT) }
                    if (chainStepTotal > 1) {
                        viewModelScope.launch { adsService.preload(AdTier.CHAPTER_OR_ACT) }
                    }

                    var hasEngaged = false
                    combine(
                        sagaContentManager.narrativeUiState,
                        sagaContentManager.milestoneUpdate,
                        sagaContentManager.contentReasoning,
                    ) { narrativeState, milestone, reasoning ->
                        NarrativeSnapshot(narrativeState.phase, narrativeState.lastError, milestone, reasoning)
                    }.collect { (phase, lastError, milestone, reasoning) ->
                        when {
                            milestone is SagaMilestone.Introduction -> {
                                hasEngaged = true
                                _uiState.value = MilestoneUiState.IntroductionStep(milestone)
                            }

                            milestone is SagaMilestone.NewEvent ||
                                milestone is SagaMilestone.ChapterFinished ||
                                milestone is SagaMilestone.ActFinished -> {
                                hasEngaged = true
                                chainStepIndex++
                                val stepTotal = maxOf(chainStepTotal, chainStepIndex)
                                val closureState =
                                    MilestoneUiState.ClosureStep(
                                        milestone = milestone,
                                        stepIndex = chainStepIndex,
                                        stepTotal = stepTotal,
                                    )
                                // Event -> chapter -> act closures can only ever emit in that
                                // order within one chain (verified against SagaContentManagerImpl's
                                // drive loop), so the terminal step's own milestone type is always
                                // the highest-severity one that closed — an event mid-chain (a
                                // chapter or act still coming) never shows an ad, only the chain's
                                // last step does, tiered by what actually closed.
                                val isTerminalStep = chainStepIndex >= stepTotal
                                if (!adShownThisChain && isTerminalStep) {
                                    adShownThisChain = true
                                    val tier =
                                        if (milestone is SagaMilestone.NewEvent) AdTier.EVENT else AdTier.CHAPTER_OR_ACT
                                    viewModelScope.launch {
                                        adsService.showIfReady(tier) { _uiState.value = closureState }
                                    }
                                } else {
                                    _uiState.value = closureState
                                }
                            }

                            // A step that already failed once sits back in AwaitingAdvance
                            // with lastError set (same pendingAction, so a plain retry just
                            // re-attempts it) — auto-advancing here regardless used to retry
                            // silently forever against e.g. no network overnight, which read
                            // from the outside as being stuck loading with no indication
                            // anything was wrong. Surface it and wait for an explicit tap
                            // instead; only auto-advance a step that hasn't failed yet.
                            phase is NarrativePhase.AwaitingAdvance && lastError != null -> {
                                hasEngaged = true
                                _uiState.value = MilestoneUiState.Error(lastError.message, lastError.canRetry)
                            }

                            phase is NarrativePhase.AwaitingAdvance -> {
                                hasEngaged = true
                                _uiState.value = MilestoneUiState.Loading(reasoning)
                                // Detached on purpose: this is a suspend call that streams AI
                                // generation, and it mutates the very flows this collector
                                // observes. Awaiting it inline here would let collect's own
                                // upstream recomposition race it; this way it runs to
                                // completion on its own.
                                viewModelScope.launch { sagaContentManager.advanceNarrative() }
                            }

                            phase is NarrativePhase.Processing -> {
                                hasEngaged = true
                                _uiState.value =
                                    MilestoneUiState.Loading(reasoning, isAutomaticStep = phase.isAutomatic)
                            }

                            phase is NarrativePhase.BackgroundProcessing -> {
                                hasEngaged = true
                                _uiState.value = MilestoneUiState.Loading(reasoning)
                            }

                            phase is NarrativePhase.Playing && hasEngaged -> {
                                // Don't trust a single Playing tick — continueMilestone()'s
                                // Introduction branch re-checks progression in a separate
                                // coroutine (dismissMilestone() and that re-check aren't
                                // atomic together), so this collector could observe a
                                // transient "nothing pending yet" moment right before the
                                // real answer lands. Settle briefly and confirm against the
                                // live flows before actually leaving.
                                //
                                // Debounced on purpose: contentReasoning/milestoneUpdate can
                                // tick more than once while the chain is genuinely finished,
                                // and each tick used to spawn its own independent delayed
                                // check — if the chain really was done, every one of those
                                // would pass and each would emit _finished, which made
                                // onFinished() (navigator.goBack()) fire more than once and
                                // pop both the Milestone screen and the Chat screen beneath
                                // it. Cancelling the previous check keeps only the latest
                                // tick's verdict alive.
                                finishCheckJob?.cancel()
                                finishCheckJob =
                                    viewModelScope.launch {
                                        delay(400)
                                        if (sagaContentManager.narrativeUiState.value.phase is NarrativePhase.Playing &&
                                            sagaContentManager.milestoneUpdate.value == null
                                        ) {
                                            _finished.emit(Unit)
                                        }
                                    }
                            }

                            else -> {
                                _uiState.value = MilestoneUiState.Loading(reasoning)
                            }
                        }
                    }
                }
        }

        fun onContinue() {
            viewModelScope.launch { sagaContentManager.continueMilestone() }
        }

        /** advanceNarrative() re-reads narrativeCoordinator's own pendingAction — still the same
         * failed action, since onActionCompleted's Failure branch keeps it around — and
         * onUserAdvanceRequested() clears lastError as part of kicking it off again, so this is
         * a real retry of the exact step that failed, not just a generic re-check. */
        fun retryFailedStep() {
            viewModelScope.launch { sagaContentManager.advanceNarrative() }
        }

        fun dismissOnboarding() {
            _showOnboarding.value = false
        }

        fun generateBook(act: Act) {
            viewModelScope.launch {
                val sagaContent = sagaContentManager.getSagaContent() ?: return@launch
                val actContent = sagaContent.acts.find { it.data.id == act.id } ?: return@launch
                bookGenerationService.generate(sagaContent, actContent)
            }
        }
    }
