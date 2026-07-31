package com.ilustris.sagai.features.saga.milestone.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCheck
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativePhase
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The single, explicit driver of a narrative chain (event close -> maybe chapter close -> maybe
 * act close -> next act's intro) while the Milestone screen owns it. Unscoped/shared
 * `hiltViewModel()` like every other screen in this app (no entry-scoped ViewModel store is
 * wired into Nav3 here) — so it follows the same guard-and-reset-on-id pattern as
 * `ChatViewModel.initChat()` / `CharacterDetailsViewModel` rather than relying on the framework
 * to hand it a fresh instance per saga.
 *
 * Deliberately thin: no local cache of milestone content survives a saga switch, it only ever
 * relays what [SagaContentManager] already exposes reactively, plus one local counter for the
 * closure stepper. That's what keeps two different sagas' milestones from ever flashing into
 * each other.
 */
@HiltViewModel
class MilestoneViewModel
    @Inject
    constructor(
        private val sagaContentManager: SagaContentManager,
        private val remoteConfigService: RemoteConfigService,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<MilestoneUiState>(MilestoneUiState.Loading())
        val uiState: StateFlow<MilestoneUiState> = _uiState.asStateFlow()

        val genre: StateFlow<Genre?> =
            sagaContentManager.content
                .map { it?.data?.genre }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        private val _finished = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
        val finished: SharedFlow<Unit> = _finished

        private var currentSagaId: Int? = null
        private var driveJob: Job? = null
        private var chainStepTotal = 0
        private var chainStepIndex = 0

        fun start(sagaId: Int) {
            if (currentSagaId == sagaId) return
            currentSagaId = sagaId
            driveJob?.cancel()
            chainStepTotal = 0
            chainStepIndex = 0
            _uiState.value = MilestoneUiState.Loading()

            driveJob =
                viewModelScope.launch {
                    sagaContentManager.content.value?.let { saga ->
                        chainStepTotal = NarrativeCheck.computeClosureChainLength(saga, remoteConfigService.getNarrativeRules())
                    }

                    var hasEngaged = false
                    combine(
                        sagaContentManager.narrativeUiState,
                        sagaContentManager.milestoneUpdate,
                        sagaContentManager.contentReasoning,
                    ) { narrativeState, milestone, reasoning -> Triple(narrativeState.phase, milestone, reasoning) }
                        .collect { (phase, milestone, reasoning) ->
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
                                    _uiState.value =
                                        MilestoneUiState.ClosureStep(
                                            milestone = milestone,
                                            stepIndex = chainStepIndex,
                                            stepTotal = maxOf(chainStepTotal, chainStepIndex),
                                        )
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

                                phase is NarrativePhase.Processing || phase is NarrativePhase.BackgroundProcessing -> {
                                    hasEngaged = true
                                    _uiState.value = MilestoneUiState.Loading(reasoning)
                                }

                                phase is NarrativePhase.Playing && hasEngaged -> {
                                    _finished.emit(Unit)
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
    }
