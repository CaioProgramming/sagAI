package com.ilustris.sagai.features.debug.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.act.data.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.TimelineMetadata
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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
    val sagaMetadata: SagaMetadata? = null,
    val isLoading: Boolean = false,
    val reasoning: String? = null,
    val error: String? = null,
    val generatingSections: Set<String> = emptySet(),
    val isFixing: Boolean = false,
    val fixItemsCount: Int = 0,
    val currentFixItem: Int = 0,
    val showFixConfirmation: Boolean = false,
)

@HiltViewModel
class LoreDebugViewModel
    @Inject
    constructor(
        private val sagaUseCase: SagaHistoryUseCase,
        private val actUseCase: ActUseCase,
        private val chapterUseCase: ChapterUseCase,
        private val timelineUseCase: TimelineUseCase,
        private val remoteConfigService: RemoteConfigService,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoreDebugUiState())
        val uiState: StateFlow<LoreDebugUiState> = _uiState.asStateFlow()

        fun loadSaga(sagaId: Int) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                sagaUseCase.getSagaMetadata(sagaId).collectLatest { saga ->
                    if (saga != null) {
                        _uiState.update { it.copy(sagaMetadata = saga, isLoading = false) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Saga not found") }
                    }
                }
            }
        }

        fun regenerateActIntroduction(act: ActMetadata) {
            val sagaMetadata = _uiState.value.sagaMetadata ?: return
            val sectionId = "act_intro_${act.data.id}"
            viewModelScope.launch {
                startGenerating(sectionId)
                actUseCase
                    .generateActIntroductionStream(sagaMetadata, act.data)
                    .collectLatest { state ->
                        handleStreamingState(state)
                    }
            }
        }

        fun regenerateActConclusion(act: ActMetadata) {
            val sagaMetadata = _uiState.value.sagaMetadata ?: return
            val sectionId = "act_conclusion_${act.data.id}"
            viewModelScope.launch {
                startGenerating(sectionId)
                actUseCase.synthesizeActEvolutionStream(sagaMetadata, act).collectLatest { state ->
                    handleStreamingState(state)
                }
            }
        }

        fun regenerateChapterIntroduction(chapter: ChapterMetadata) {
            _uiState.value.sagaMetadata ?: return
            val sectionId = "chapter_intro_${chapter.data.id}"
            viewModelScope.launch {
                startGenerating(sectionId)
                chapterUseCase
                    .generateChapterIntroductionStream(chapter.data.id)
                    .collectLatest { state ->
                        handleStreamingState(state)
                    }
            }
        }

        fun regenerateChapterConclusion(chapter: ChapterMetadata) {
            _uiState.value.sagaMetadata ?: return
            val sectionId = "chapter_conclusion_${chapter.data.id}"
            viewModelScope.launch {
                startGenerating(sectionId)
                chapterUseCase
                    .synthesizeChapterEvolutionStream(chapter.data.id)
                    .collectLatest { state ->
                        handleStreamingState(state)
                    }
            }
        }

        fun regenerateTimeline(timeline: TimelineMetadata) {
            val sagaMetadata = _uiState.value.sagaMetadata ?: return
            val sectionId = "timeline_${timeline.data.id}"
            viewModelScope.launch {
                startGenerating(sectionId)
                timelineUseCase
                    .generateFullLoreUpdateStream(sagaMetadata, timeline.data)
                    .collectLatest { state ->
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

        fun toggleFixConfirmation() {
            _uiState.update { it.copy(showFixConfirmation = !it.showFixConfirmation) }
        }

        fun fixStory() {
            val sagaId =
                _uiState.value.sagaMetadata
                    ?.data
                    ?.id ?: return
            viewModelScope.launch {
                val rules =
                    remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: NarrativeRules()
                val currentSaga = sagaUseCase.getSagaById(sagaId).first() ?: return@launch

                val actsToFix =
                    currentSaga.acts.filter {
                        it.isFull(rules.actUpdateLimit, rules) &&
                            (
                                it.data.emotionalReview.isNullOrEmpty() || it.data.narrativeGuide.isNullOrEmpty() ||
                                    it.data.content.isEmpty()
                            )
                    }
                val chaptersToFix =
                    currentSaga.flatChapters().filter {
                        it.isFull(rules.chapterUpdateLimit, rules) &&
                            (
                                it.data.emotionalReview.isNullOrEmpty() ||
                                    it.data.narrativeGuide.isNullOrEmpty() ||
                                    it.data.overview.isEmpty()
                            )
                    }
                val timelinesToFix =
                    currentSaga.flatEvents().filter {
                        it.isComplete(rules) && (it.data.emotionalReview.isNullOrEmpty() || it.data.narrativeGuide.isNullOrEmpty())
                    }

                val totalItems = actsToFix.size + chaptersToFix.size + timelinesToFix.size
                if (totalItems == 0) {
                    _uiState.update { it.copy(showFixConfirmation = false) }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        isFixing = true,
                        fixItemsCount = totalItems,
                        currentFixItem = 0,
                        showFixConfirmation = false,
                    )
                }

                timelinesToFix.forEach { timeline ->
                    val sagaId =
                        _uiState.value.sagaMetadata
                            ?.data
                            ?.id ?: return@forEach
                    updateFixProgress()
                    val sagaMetadata = sagaUseCase.getSagaMetadata(sagaId).first() ?: return@forEach
                    timelineUseCase
                        .generateFullLoreUpdateStream(sagaMetadata, timeline.data)
                        .collect { state ->
                            handleStreamingState(state)
                        }
                }

                chaptersToFix.forEach { chapter ->
                    sagaUseCase.getSagaById(sagaId).first() ?: return@forEach
                    updateFixProgress()
                    chapterUseCase
                        .synthesizeChapterEvolutionStream(chapter.data.id)
                        .collect { state ->
                            handleStreamingState(state)
                        }
                }

                actsToFix.forEach { act ->
                    val sagaId =
                        _uiState.value.sagaMetadata
                            ?.data
                            ?.id ?: return@forEach
                    updateFixProgress()
                    val sagaMetadata = sagaUseCase.getSagaMetadata(sagaId).first() ?: return@forEach
                    val actMetadata =
                        sagaMetadata.acts.find { it.data.id == act.data.id } ?: return@forEach
                    actUseCase
                        .synthesizeActEvolutionStream(sagaMetadata, actMetadata)
                        .collect { state ->
                            handleStreamingState(state)
                        }
                }

                _uiState.update { it.copy(isFixing = false, currentFixItem = 0) }
            }
        }

        private fun updateFixProgress() {
            _uiState.update { it.copy(currentFixItem = it.currentFixItem + 1) }
        }

        fun regenerateData(
            sectionId: String,
            content: Any,
            section: DebugSection,
        ) {
            viewModelScope.launch {
                _uiState.update { it.copy(generatingSections = it.generatingSections + sectionId) }
                when (content) {
                    is ActMetadata -> {
                        if (section == DebugSection.ACT_INTRODUCTION) {
                            regenerateActIntroduction(content)
                        } else {
                            regenerateActConclusion(content)
                        }
                    }

                    is ChapterMetadata -> {
                        if (section == DebugSection.CHAPTER_INTRODUCTION) {
                            regenerateChapterIntroduction(content)
                        } else {
                            regenerateChapterConclusion(content)
                        }
                    }

                    is TimelineMetadata -> {
                        regenerateTimeline(content)
                    }
                }
            }
        }
    }
