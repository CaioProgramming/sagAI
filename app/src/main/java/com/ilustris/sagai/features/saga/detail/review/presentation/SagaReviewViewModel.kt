package com.ilustris.sagai.features.saga.detail.review.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.detail.data.usecase.ReviewState
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.saga.detail.review.domain.SagaReviewUseCase
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
        private val reviewUseCase: SagaReviewUseCase,
        private val sagaRepository: SagaRepository,
    ) : ViewModel() {
        private val _isGenerating = MutableStateFlow(false)
        val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

        private val _loadingMessage = MutableStateFlow<String?>(null)
        val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()

        fun createReview(saga: SagaContent) {
            if (saga.data.review != null) return
            _isGenerating.value = true

            viewModelScope.launch(Dispatchers.IO) {
                reviewUseCase.createReview(saga).collectLatest { state ->
                    when (state) {
                        is ReviewState.Loading -> {
                            _loadingMessage.value = state.message
                        }

                        is ReviewState.Success -> {
                            _isGenerating.value = false
                            _loadingMessage.value = null
                        }

                        is ReviewState.Error -> {
                            _isGenerating.value = false
                            _loadingMessage.value = null
                        }
                    }
                }
            }
        }

        fun resetReview(saga: SagaContent) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaRepository.updateChat(saga.data.copy(review = null))
                createReview(saga)
            }
        }
    }
