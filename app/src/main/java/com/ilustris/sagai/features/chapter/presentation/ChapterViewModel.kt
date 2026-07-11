package com.ilustris.sagai.features.chapter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.chapter.data.model.ChapterInfo
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
        val chaptersInfo = MutableStateFlow<List<ChapterInfo>>(emptyList())
        val isGenerating = MutableStateFlow(false)
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
            viewModelScope.launch(Dispatchers.IO) {
                chapterUseCase.getChaptersInfoBySaga(sagaId.toInt()).collect {
                    chaptersInfo.value = it
                }
            }
        }

        fun reviewChapter(chapterId: Int) {
            viewModelScope.launch(Dispatchers.IO) {
                isGenerating.emit(true)
                chapterUseCase.reviewChapter(chapterId)
                isGenerating.emit(false)
            }
        }

        fun generateIcon(chapterId: Int) {
            viewModelScope.launch(Dispatchers.IO) {
                chapterUseCase.generateChapterCoverStream(chapterId).collect { }
            }
        }
    }
