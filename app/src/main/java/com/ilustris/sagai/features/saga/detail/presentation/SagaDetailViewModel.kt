package com.ilustris.sagai.features.saga.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SagaDetailViewModel
    @Inject
    constructor(
        private val sagaDetailUseCase: SagaDetailUseCase,
        private val remoteConfig: FirebaseRemoteConfig,
    ) : ViewModel() {
        private val _state = MutableStateFlow<State>(State.Loading)
        val state: StateFlow<State> = _state.asStateFlow()
        val saga = MutableStateFlow<SagaContent?>(null)
        val isGenerating = MutableStateFlow(false)
        val emotionalCardReference = MutableStateFlow<String>(emptyString())

        fun fetchEmotionalCardReference() {
            viewModelScope.launch(Dispatchers.IO) {
                remoteConfig.fetchAndActivate()
                emotionalCardReference.value = remoteConfig.getString(EMOTIONAL_CARD_CONFIG)
            }
        }

        fun fetchSagaDetails(sagaId: String) {
            viewModelScope.launch(Dispatchers.IO) {
                emotionalCardReference.value = remoteConfig.getString(EMOTIONAL_CARD_CONFIG)
                _state.value = State.Loading
                sagaDetailUseCase.fetchSaga(sagaId.toInt()).collectLatest { saga ->
                    saga?.let { data ->
                        _state.value = State.Success(data)
                        if (data.data.isEnded) {
                            fetchEmotionalCardReference()
                        }
                        this@SagaDetailViewModel.saga.value = data
                    }
                }
            }
        }

        fun deleteSaga(saga: Saga) {
            viewModelScope.launch {
                sagaDetailUseCase.deleteSaga(saga)
            }
        }

        fun createReview() {
            if (isGenerating.value) {
                return
            }
            val currentSaga = saga.value ?: return
            if (currentSaga.data.isEnded.not()) {
                isGenerating.value = false
                return
            }
            isGenerating.value = true
            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase
                    .createReview(currentSaga)
                isGenerating.value = false
            }
        }

        fun regenerateIcon() {
            val currentSaga = saga.value ?: return
            isGenerating.value = true
            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase.regenerateSagaIcon(
                    currentSaga,
                )
                isGenerating.value = false
            }
        }

        fun resetReview() {
            val currentSaga = saga.value ?: return

            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase.resetReview(currentSaga)
                delay(500)
                createReview()
            }
        }

        fun createEmotionalReview(timelineContent: TimelineContent) {
            val currentSaga = saga.value ?: return
            viewModelScope.launch {
                isGenerating.value = true
                sagaDetailUseCase.createTimelineReview(currentSaga, timelineContent)
                isGenerating.value = false
            }
        }

        fun createSagaEmotionalReview() {
            val currentSaga = saga.value ?: return
            viewModelScope.launch {
                isGenerating.value = true
                sagaDetailUseCase.createSagaEmotionalReview(currentSaga)
                isGenerating.value = false
            }
        }
    }

private const val EMOTIONAL_CARD_CONFIG = "mental_card_icon"
