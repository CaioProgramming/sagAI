package com.ilustris.sagai.features.milestone.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.milestone.domain.MilestoneUseCase
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MilestoneViewModel
    @Inject
    constructor(
        private val milestoneUseCase: MilestoneUseCase,
    ) : ViewModel() {
        private val _congratsMessage = MutableStateFlow<String?>(null)
        val congratsMessage: StateFlow<String?> = _congratsMessage.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        fun generateCongratsMessage(
            milestone: SagaMilestone,
            saga: SagaContent,
        ) {
            viewModelScope.launch {
                _isLoading.value = true

                milestoneUseCase
                    .generateCongratsMessage(milestone, saga)
                    .onSuccess { message ->
                        _congratsMessage.value = message
                    }.onFailure {
                        // Keep null, UI will show loading state or skip
                        _congratsMessage.value = null
                    }

                _isLoading.value = false
            }
        }

        fun clear() {
            _congratsMessage.value = null
            _isLoading.value = false
        }
    }
