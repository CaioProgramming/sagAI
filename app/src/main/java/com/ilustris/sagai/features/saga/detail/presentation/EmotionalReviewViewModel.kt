package com.ilustris.sagai.features.saga.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.services.LoadingService
import com.ilustris.sagai.core.services.LoadingType
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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
        private val promptService: PromptService,
    ) : ViewModel() {
        private val _isGenerating = MutableStateFlow(false)
        val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

        private val _loadingMessage = MutableStateFlow<String?>(null)
        val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()

        val emotionalIllustration = MutableStateFlow<String?>(null)

        fun createEmotionalReview(sagaId: Int) {
            viewModelScope.launch(Dispatchers.IO) {
                val sagaContent = sagaRepository.getSagaById(sagaId).firstOrNull() ?: return@launch
                emotionalIllustration.emit(
                    emotionalUseCase.getEmotionalCard(sagaContent.data).getSuccess(),
                )
                if (sagaContent.data.emotionalReview?.isNotBlank() == true) {
                    return@launch
                }
                _isGenerating.value = true
                genreConfigService.getGenreConfig(sagaContent.data.genre)
                val emotionalTask = promptService.buildPrompt("emotional_loading", emptyMap())
                _loadingMessage.value =
                    loadingService.generateLoadingMessage(
                        LoadingType(emotionalTask),
                        genreConfigService.conversationBlueprint(sagaContent.data.genre),
                    )

                emotionalUseCase.streamEmotionalConclusion(sagaContent).collect { state ->
                    when (state) {
                        is StreamingState.Reasoning -> {
                            _loadingMessage.value = state.chunk
                        }

                        is StreamingState.Success -> {
                            sagaRepository.updateSaga(
                                sagaContent.data.copy(
                                    emotionalProfile = state.data.emotionalProfile,
                                    emotionalReview = state.data.emotionalProfile.emotionalContent,
                                    endMessage = state.data.endingMessage,
                                ),
                            )
                            _isGenerating.value = false
                            _loadingMessage.value = null
                        }

                        is StreamingState.Error -> {
                            _isGenerating.value = false
                            _loadingMessage.value =
                                "Error generating emotional conclusion: ${state.message}"
                        }
                    }
                }
            }
        }

        fun resetEmotionalProfile(sagaId: Int) {
            viewModelScope.launch(Dispatchers.IO) {
                val sagaContent = sagaRepository.getSagaById(sagaId).firstOrNull() ?: return@launch
                emotionalUseCase.generateEmotionalConclusion(sagaContent).onSuccessAsync {
                    sagaRepository.updateSaga(
                        sagaContent.data.copy(
                            emotionalProfile = it.emotionalProfile,
                            emotionalReview = it.emotionalProfile.emotionalContent,
                            endMessage = it.endingMessage,
                        ),
                    )
                }
            }
        }
    }
