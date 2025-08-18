package com.ilustris.sagai.features.saga.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SagaDetailViewModel
    @Inject
    constructor(
        private val sagaDetailUseCase: SagaDetailUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow<State>(State.Loading)
        val state: StateFlow<State> = _state.asStateFlow()
        val saga = MutableStateFlow<SagaContent?>(null)
        val isGenerating = MutableStateFlow(false)

        fun fetchSagaDetails(sagaId: String) {
            viewModelScope.launch(Dispatchers.IO) {
                _state.value = State.Loading
                sagaDetailUseCase.fetchSaga(sagaId.toInt()).collect { saga ->
                    saga?.let { data ->
                        _state.value = State.Success(data)
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
    }
