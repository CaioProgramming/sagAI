package com.ilustris.sagai.features.saga.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.services.LoadingService
import com.ilustris.sagai.core.services.LoadingType
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmotionalReviewViewModel
    @Inject
    constructor(
        private val emotionalUseCase: EmotionalUseCase,
        private val loadingService: LoadingService,
        private val genreConfigService: GenreConfigService,
        private val sagaRepository: SagaRepository,
    ) : ViewModel() {
        private val _isGenerating = MutableStateFlow(false)
        val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

        private val _loadingMessage = MutableStateFlow<String?>(null)
        val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()

        val emotionalIllustration = MutableStateFlow<String?>(null)

        fun createEmotionalReview(sagaContent: SagaContent) {
            viewModelScope.launch(Dispatchers.IO) {
                emotionalIllustration.emit(emotionalUseCase.getEmotionalCard(sagaContent).getSuccess())
                if (sagaContent.data.emotionalReview?.isNotBlank() == true) {
                    return@launch
                }
                _isGenerating.value = true
                val genreConfig = genreConfigService.getGenreConfig(sagaContent.data.genre)
                _loadingMessage.value =
                    loadingService.generateLoadingMessage(
                        LoadingType("emotional_loading"),
                        genreConfig.conversationDirective,
                    )

                emotionalUseCase.generateEmotionalConclusion(sagaContent).onSuccessAsync {
                    sagaRepository.updateChat(
                        sagaContent.data.copy(
                            emotionalReview = it,
                        ),
                    )
                }
                _isGenerating.value = false
                _loadingMessage.value = null
            }
        }
    }
