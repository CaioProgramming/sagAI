package com.ilustris.sagai.features.chapter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChapterViewModel
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val chapterUseCase: ChapterUseCase,
    ) : ViewModel() {
        val saga = MutableStateFlow<SagaContent?>(null)

        val isGenerating = MutableStateFlow(false)

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
                chapterUseCase.generateChapterCover(
                    chapter,
                    content,
                )
                isGenerating.value = false
            }
        }
    }
