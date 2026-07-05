package com.ilustris.sagai.features.debug.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.act.data.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.TimelineMetadata
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class DebugSection {
    ACT_INTRODUCTION,
    ACT_CONCLUSION,
    CHAPTER_INTRODUCTION,
    CHAPTER_CONCLUSION,
    TIMELINE,
}

enum class ImageDebugTargetType {
    SAGA_ICON,
    CHARACTER,
    CHAPTER_COVER,
}

data class ImageDebugTarget(
    val id: String,
    val type: ImageDebugTargetType,
    val label: String,
    val imagePath: String?,
    val character: Character? = null,
    val chapterId: Int? = null,
) {
    val isMissing: Boolean
        get() = imagePath.isNullOrBlank() || !File(imagePath).exists()
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
    val imageTargets: List<ImageDebugTarget> = emptyList(),
    val showImagePager: Boolean = false,
    val pagerInitialIndex: Int = 0,
)

@HiltViewModel
class LoreDebugViewModel
    @Inject
    constructor(
        private val sagaUseCase: SagaHistoryUseCase,
        private val actUseCase: ActUseCase,
        private val chapterUseCase: ChapterUseCase,
        private val characterUseCase: CharacterUseCase,
        private val sagaDetailUseCase: SagaDetailUseCase,
        private val timelineUseCase: TimelineUseCase,
        private val remoteConfigService: RemoteConfigService,
        private val debugImageFallbackService: DebugImageFallbackService,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoreDebugUiState())
        val uiState: StateFlow<LoreDebugUiState> = _uiState.asStateFlow()

        init {
            debugImageFallbackService.bindImageGenerationLoadingPause(viewModelScope) {
                _uiState.update {
                    it.copy(
                        generatingSections = emptySet(),
                        reasoning = null,
                    )
                }
            }
        }

        fun loadSaga(sagaId: Int) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                sagaUseCase.getSagaMetadata(sagaId).collectLatest { saga ->
                    if (saga != null) {
                        _uiState.update {
                            it.copy(
                                sagaMetadata = saga,
                                isLoading = false,
                                imageTargets = buildImageTargets(saga),
                            )
                        }
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
                val fullSaga =
                    sagaUseCase.getSagaById(sagaMetadata.data.id).first() ?: return@launch
                fullSaga.findAct(act.data.id)?.let {
                    actUseCase.synthesizeActEvolutionStream(fullSaga, it).collectLatest { state ->
                        handleStreamingState(state)
                    }
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
                    _uiState.value.sagaMetadata?.let { saga ->
                        _uiState.update { it.copy(imageTargets = buildImageTargets(saga)) }
                    }
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

        fun openImagePager(initialIndex: Int) {
            _uiState.update { it.copy(showImagePager = true, pagerInitialIndex = initialIndex) }
        }

        fun dismissImagePager() {
            _uiState.update { it.copy(showImagePager = false) }
        }

        fun generateImageForTarget(target: ImageDebugTarget) {
            val sagaMetadata = _uiState.value.sagaMetadata ?: return
            viewModelScope.launch {
                startGenerating(target.id)
                when (target.type) {
                    ImageDebugTargetType.SAGA_ICON -> {
                        sagaDetailUseCase
                            .regenerateSagaIconStream(sagaMetadata.data.id)
                            .collectLatest { state -> handleStreamingState(state) }
                    }

                    ImageDebugTargetType.CHARACTER -> {
                        val character = target.character ?: return@launch
                        characterUseCase
                            .generateCharacterImageStream(character, sagaMetadata.data)
                            .collectLatest { state -> handleStreamingState(state) }
                    }

                    ImageDebugTargetType.CHAPTER_COVER -> {
                        val chapterId = target.chapterId ?: return@launch
                        chapterUseCase
                            .generateChapterCoverStream(chapterId)
                            .collectLatest { state -> handleStreamingState(state) }
                    }
                }
            }
        }

        private fun buildImageTargets(saga: SagaMetadata): List<ImageDebugTarget> {
            val targets = mutableListOf<ImageDebugTarget>()

            targets +=
                ImageDebugTarget(
                    id = "image_saga_icon",
                    type = ImageDebugTargetType.SAGA_ICON,
                    label = saga.data.title,
                    imagePath = saga.data.icon.takeIf { it.isNotBlank() },
                )

            saga.characters.forEach { character ->
                targets +=
                    ImageDebugTarget(
                        id = "image_char_${character.id}",
                        type = ImageDebugTargetType.CHARACTER,
                        label = character.name,
                        imagePath = character.image.takeIf { it.isNotBlank() },
                        character = character,
                    )
            }

            saga.flatChapters().forEach { chapter ->
                targets +=
                    ImageDebugTarget(
                        id = "image_chapter_${chapter.data.id}",
                        type = ImageDebugTargetType.CHAPTER_COVER,
                        label = chapter.data.title,
                        imagePath = chapter.data.coverImage.takeIf { it.isNotBlank() },
                        chapterId = chapter.data.id,
                    )
            }

            return targets.sortedBy { !it.isMissing }
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
                                    it.data.content.isEmpty()
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
                    val sagaContent = sagaUseCase.getSagaById(sagaId).first() ?: return@forEach
                    val act = sagaContent.findAct(act.data.id) ?: return@forEach

                    actUseCase
                        .synthesizeActEvolutionStream(sagaContent, act)
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
