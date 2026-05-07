package com.ilustris.sagai.features.debug.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DebugSection {
    ACT_INTRODUCTION,
    ACT_CONCLUSION,
    CHAPTER_INTRODUCTION,
    CHAPTER_CONCLUSION,
    TIMELINE,
}

data class LoreDebugUiState(
    val sagaContent: SagaContent? = null,
    val isLoading: Boolean = false,
    val reasoning: String? = null,
    val error: String? = null,
    val generatingSections: Set<String> = emptySet(),
)

@HiltViewModel
class LoreDebugViewModel
    @Inject
    constructor(
        private val sagaUseCase: SagaHistoryUseCase,
        private val actUseCase: ActUseCase,
        private val chapterUseCase: ChapterUseCase,
        private val timelineUseCase: TimelineUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoreDebugUiState())
        val uiState: StateFlow<LoreDebugUiState> = _uiState.asStateFlow()

        fun loadSaga(sagaId: Int) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                sagaUseCase.getSagaById(sagaId).collectLatest { result ->
                    result?.let {
                        _uiState.update { it.copy(sagaContent = result, isLoading = false) }
                    } ?: run {
                        _uiState.update { it.copy(isLoading = false, error = "Saga not found") }
                    }
                }
            }
        }

        fun regenerateActIntroduction(act: ActContent) {
            val saga = _uiState.value.sagaContent ?: return
            val sectionId = "act_intro_${act.data.id}"
            viewModelScope.launch {
                startGenerating(sectionId)
                actUseCase.generateActIntroductionStream(saga, act.data).collectLatest { state ->
                    handleStreamingState(state)
                }
            }
        }

        fun regenerateActConclusion(act: ActContent) {
            val saga = _uiState.value.sagaContent ?: return
            val sectionId = "act_conclusion_${act.data.id}"
            viewModelScope.launch {
                startGenerating(sectionId)
                actUseCase.synthesizeActEvolutionStream(saga, act).collectLatest { state ->
                    handleStreamingState(state)
                }
            }
        }

        fun regenerateChapterIntroduction(
            chapter: ChapterContent,
            act: ActContent,
        ) {
            val saga = _uiState.value.sagaContent ?: return
            val sectionId = "chapter_intro_${chapter.data.id}"
            viewModelScope.launch {
                startGenerating(sectionId)
                chapterUseCase
                    .generateChapterIntroductionStream(saga, chapter.data, act)
                    .collectLatest { state ->
                        handleStreamingState(state)
                    }
            }
        }

        fun regenerateChapterConclusion(chapter: ChapterContent) {
            val saga = _uiState.value.sagaContent ?: return
            val sectionId = "chapter_conclusion_${chapter.data.id}"
            viewModelScope.launch {
                startGenerating(sectionId)
                chapterUseCase.synthesizeChapterEvolutionStream(saga, chapter).collectLatest { state ->
                    handleStreamingState(state)
                }
            }
        }

        fun regenerateTimeline(timeline: TimelineContent) {
            val saga = _uiState.value.sagaContent ?: return
            val sectionId = "timeline_${timeline.data.id}"
            viewModelScope.launch {
                startGenerating(sectionId)
                timelineUseCase.generateFullLoreUpdateStream(saga, timeline).collectLatest { state ->
                    handleStreamingState(state)
                }
            }
        }

        private fun startGenerating(sectionId: String) {
            _uiState.update { it.copy(generatingSections = it.generatingSections + sectionId) }
        }

        private fun stopGenerating() {
            _uiState.update {
                it.copy(
                    generatingSections = emptySet(),
                    isLoading = false,
                    reasoning = null,
                )
            }
        }

        private fun <T> handleStreamingState(state: StreamingState<T>) {
            when (state) {
                is StreamingState.Success -> {
                    stopGenerating()
                }

                is StreamingState.Error -> {
                    stopGenerating()
                    _uiState.update { it.copy(error = state.message, isLoading = false) }
                }

                is StreamingState.Reasoning -> {
                    _uiState.update { it.copy(reasoning = state.chunk) }
                }
            }
        }

        fun regenerateData(
            sectionId: String,
            content: Any,
            section: DebugSection,
        ) {
            val saga = _uiState.value.sagaContent ?: return
            _uiState.update { it.copy(generatingSections = it.generatingSections + sectionId) }
            when (content) {
                is ActContent -> {
                    if (section == DebugSection.ACT_INTRODUCTION) {
                        regenerateActIntroduction(content)
                    } else {
                        regenerateActConclusion(content)
                    }
                }

                is ChapterContent -> {
                    val act = saga.findAct(content.data.actId) ?: return
                    if (section == DebugSection.CHAPTER_INTRODUCTION) {
                        regenerateChapterIntroduction(content, act)
                    } else {
                        regenerateChapterConclusion(content)
                    }
                }

                is TimelineContent -> {
                    regenerateTimeline(content)
                }
            }
        }
    }
