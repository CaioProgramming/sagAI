package com.ilustris.sagai.features.saga.detail.review.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.detail.data.model.hasViewablePages
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.domain.ReviewGenerationCoordinator
import com.ilustris.sagai.features.saga.detail.review.domain.ReviewGenerationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SagaReviewViewModel
    @Inject
    constructor(
        private val reviewGenerationCoordinator: ReviewGenerationCoordinator,
        private val sagaRepository: SagaRepository,
    ) : ViewModel() {
        private val _isGenerating = MutableStateFlow(false)
        val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

        private val _loadingMessage = MutableStateFlow<String?>(null)
        val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()

        private val _sagaContent = MutableStateFlow<SagaContent?>(null)
        val sagaContent: StateFlow<SagaContent?> = _sagaContent.asStateFlow()

        private val _generationState =
            MutableStateFlow<ReviewGenerationState>(ReviewGenerationState.Idle)
        val generationState: StateFlow<ReviewGenerationState> = _generationState.asStateFlow()

        private var loadedSagaId: Int? = null

        fun loadSaga(sagaId: Int) {
            if (loadedSagaId == sagaId) return
            loadedSagaId = sagaId

            viewModelScope.launch(Dispatchers.IO) {
                reviewGenerationCoordinator.stateFor(sagaId).collect { state ->
                    _generationState.value = state
                    when (state) {
                        is ReviewGenerationState.Generating -> {
                            _loadingMessage.value = state.reasoning
                        }

                        else -> {
                            if (_sagaContent.value
                                    ?.data
                                    ?.review
                                    ?.hasViewablePages() == true
                            ) {
                                _loadingMessage.value = null
                            }
                        }
                    }
                    updateGeneratingFlag()
                }
            }

            viewModelScope.launch(Dispatchers.IO) {
                sagaRepository.getSagaById(sagaId).collectLatest { content ->
                    _sagaContent.value = content
                    updateGeneratingFlag()
                    content?.let {
                        if (!it.data.review.isComplete()) {
                            reviewGenerationCoordinator.enqueue(sagaId)
                        }
                    }
                }
            }
        }

    fun ensureGeneration(sagaId: Int) {
            reviewGenerationCoordinator.enqueue(sagaId)
        }

        fun resetReview(saga: SagaContent) {
            viewModelScope.launch(Dispatchers.IO) {
                reviewGenerationCoordinator.cancel(saga.data.id)
                sagaRepository.updateSaga(saga.data.copy(review = null))
                reviewGenerationCoordinator.enqueue(saga.data.id)
            }
        }

        private fun updateGeneratingFlag() {
            val content = _sagaContent.value
            val state = _generationState.value
            val hasPages = content?.data?.review.hasViewablePages()
            _isGenerating.value =
                hasPages != true &&
                (
                    state is ReviewGenerationState.Generating ||
                                    (state is ReviewGenerationState.Idle && content?.data?.review == null)
                            )
        }
    }
