package com.ilustris.sagai.features.chapter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.chapter.data.model.Chapter
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

        fun loadSaga(sagaId: String?) {
            if (sagaId == null) return
            viewModelScope.launch {
                sagaHistoryUseCase.getSagaById(sagaId.toInt()).collect {
                    saga.value = it
                }
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
