package com.ilustris.sagai.features.progression.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.progression.domain.CharacterProgressionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Offers to redo the evaluations that failed or never ran, once per opening of a saga. */
@HiltViewModel
class ProgressionReviewViewModel
    @Inject
    constructor(
        private val progressionUseCase: CharacterProgressionUseCase,
    ) : ViewModel() {
        val pending = MutableStateFlow(0)
        val running = MutableStateFlow(false)
        private var checkedSagaId: Int? = null

        fun check(sagaId: Int) {
            if (checkedSagaId == sagaId) return
            checkedSagaId = sagaId
            viewModelScope.launch { pending.value = progressionUseCase.pendingCount(sagaId) }
        }

        fun dismiss() {
            pending.value = 0
        }

        /** Runs what is missing in dependency order, without replaying the past as announcements. */
        fun reviewNow(sagaId: Int) {
            if (running.value) return
            viewModelScope.launch {
                running.value = true
                progressionUseCase.retryPending(sagaId)
                pending.value = progressionUseCase.pendingCount(sagaId)
                running.value = false
            }
        }
    }
