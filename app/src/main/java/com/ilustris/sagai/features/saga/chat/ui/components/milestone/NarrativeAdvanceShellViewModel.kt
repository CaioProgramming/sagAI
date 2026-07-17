package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCoordinator
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativePhase
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeUiState
import com.ilustris.sagai.ui.components.island.ChatIslandService
import com.ilustris.sagai.ui.components.island.IslandContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NarrativeAdvanceShellViewModel
    @Inject
    constructor(
        narrativeCoordinator: NarrativeCoordinator,
        private val sagaContentManager: SagaContentManager,
        private val chatIslandService: ChatIslandService,
    ) : ViewModel() {
        val narrativeUiState: StateFlow<NarrativeUiState> = narrativeCoordinator.uiState

        val reasoning: StateFlow<String?> =
            sagaContentManager.contentReasoning
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = null,
                )

        val isProcessing: StateFlow<Boolean> =
            narrativeCoordinator.uiState
                .map { state ->
                    state.phase is NarrativePhase.Processing || state.isProcessing
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = false,
                )

        fun advanceNarrative() {
            viewModelScope.launch {
                sagaContentManager.advanceNarrative()
            }
        }

        /** Publishes (or clears) the chat's bottom island contribution to the global overlay. */
        fun publishBottomIsland(content: IslandContent?) {
            chatIslandService.setBottom(content)
        }

        /** Publishes (or clears) the chat's top island contribution to the global overlay. */
        fun publishTopIsland(content: IslandContent?) {
            chatIslandService.setTop(content)
        }
    }
