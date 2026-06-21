package com.ilustris.sagai.features.brain.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.brain.domain.BrainUseCase
import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.brain.domain.model.BrainLayoutResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrainMiniState(
    val graph: BrainGraph? = null,
    val layout: BrainLayoutResult? = null,
)

@HiltViewModel
class BrainMiniViewModel
    @Inject
    constructor(
        private val brainUseCase: BrainUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(BrainMiniState())
        val state = _state.asStateFlow()

        fun load(sagaId: Int) {
            viewModelScope.launch {
                val content = brainUseCase.loadMiniPreview(sagaId) ?: return@launch
                _state.value =
                    BrainMiniState(
                        graph = content.graph,
                        layout = content.layout,
                    )
            }
        }
    }
