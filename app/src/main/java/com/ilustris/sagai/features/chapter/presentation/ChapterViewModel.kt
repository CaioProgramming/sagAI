package com.ilustris.sagai.features.chapter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ChapterViewModel
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val chapterUseCase: ChapterUseCase,
    ) : ViewModel() {
        val saga = MutableStateFlow<SagaContent?>(null)

        val isGenerating = MutableStateFlow(false)
        val reasoningMessage = MutableStateFlow<String?>(null)
        val showPremiumSheet = MutableStateFlow(false)

        fun togglePremiumSheet() {
            showPremiumSheet.value = !showPremiumSheet.value
        }

        fun init(sagaContent: SagaContent?) {
            saga.value = sagaContent
        }

        fun loadSaga(sagaId: String?) {
            if (sagaId == null) return
            viewModelScope.launch(Dispatchers.IO) {
                sagaHistoryUseCase.getSagaById(sagaId.toInt()).collect {
                    saga.value = it
                }
            }
        }

        fun reviewChapter(chapter: ChapterContent) {
            val currentSaga = saga.value ?: return
            viewModelScope.launch(Dispatchers.IO) {
                isGenerating.emit(true)
                chapterUseCase.reviewChapter(currentSaga, chapter)
                isGenerating.emit(false)
            }
        }

        fun generateIcon(
            content: SagaContent,
            chapter: ChapterContent,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                isGenerating.value = true
                chapterUseCase
                    .generateChapterCoverStream(
                        chapter,
                        content,
                    ).collect {
                        when (it) {
                            is StreamingState.Error -> {
                                reasoningMessage.emit("Erro ao gerar capa do capítulo...")
                                delay(3.seconds)
                                reasoningMessage.emit(null)
                                isGenerating.value = false
                            }

                            is StreamingState.Reasoning -> {
                                isGenerating.value = true
                                reasoningMessage.emit(it.chunk)
                            }

                            is StreamingState.Success<*> -> {
                                reasoningMessage.emit("Capa do capítulo gerada com sucesso!")
                                delay(3.seconds)
                                reasoningMessage.emit(null)
                                isGenerating.value = false
                            }
                        }
                    }
            }
        }
    }
