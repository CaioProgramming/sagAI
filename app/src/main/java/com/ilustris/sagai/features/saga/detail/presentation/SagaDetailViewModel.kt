package com.ilustris.sagai.features.saga.detail.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.SagaDetailUIMapper
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.TimelineDrawer
import com.ilustris.sagai.features.saga.detail.ui.DetailAction
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.wiki.data.model.Wiki
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SagaDetailViewModel
    @Inject
    constructor(
        private val sagaDetailUseCase: SagaDetailUseCase,
        private val imageSegmentationHelper: ImageSegmentationHelper,
        private val visualConfigService: GenreVisualConfigService,
        private val sagaDetailUIMapper: SagaDetailUIMapper,
    ) : ViewModel() {
        private val _state = MutableStateFlow<State>(State.Loading)
        val state: StateFlow<State> = _state.asStateFlow()
        val saga = MutableStateFlow<SagaContent?>(null)
        val isGenerating = MutableStateFlow(false)
        val showIntro = MutableStateFlow(false)
        private val _loadingMessage = MutableStateFlow<String?>(null)
        val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()
        val backupEnabled = sagaDetailUseCase.getBackupEnabled()

        val showPremiumSheet = MutableStateFlow(false)

        val originalBitmap = MutableStateFlow<Bitmap?>(null)
        val segmentedBitmap = MutableStateFlow<Bitmap?>(null)

        val sagaResume = MutableStateFlow<String?>(null)
        val visualConfig = MutableStateFlow<GenreVisualConfig?>(null)
        private val _actualSection = MutableStateFlow<DetailSectionView?>(null)
        val actualSection = _actualSection.asStateFlow()

        val detailDrawer = MutableStateFlow<TimelineDrawer?>(null)

        private val sectionCache = mutableMapOf<RequestSection, DetailSectionView>()
        private var currentRequestSection: RequestSection = RequestSection.START
        private var sectionJob: Job? = null
        private val insightJobs = mutableMapOf<RequestSection, Job>()

        fun togglePremiumSheet() {
            showPremiumSheet.value = !showPremiumSheet.value
        }

        fun loadSection(requestSection: RequestSection) {
            currentRequestSection = requestSection
            val currentSaga = saga.value ?: return

            if (sectionCache.containsKey(requestSection)) {
                _actualSection.value = sectionCache[requestSection]
                return
            }

            sectionJob?.cancel()

            sectionJob =
                viewModelScope.launch {
                    sagaDetailUIMapper
                        .buildSection(
                            currentSaga,
                            requestSection,
                        ).onSuccess { mappedSection ->
                            sectionCache[requestSection] = mappedSection
                            _actualSection.value = mappedSection
                            _state.value = State.Success(currentSaga)
                            generateInsight(requestSection, currentSaga)
                        }.onFailureAsync {
                            _state.emit(State.Error(emptyString()))
                        }
                }
        }

        private fun generateInsight(
            section: RequestSection,
            sagaContent: SagaContent,
        ) {
            if (insightJobs.containsKey(section)) return

            insightJobs[section] =
                viewModelScope.launch(Dispatchers.IO) {
                    val result =
                        when (section) {
                            RequestSection.START -> {
                                sagaDetailUseCase.generateSagaResume(sagaContent)
                            }

                            RequestSection.CHARACTERS -> {
                                sagaDetailUseCase.generateCharactersInsight(
                                    sagaContent,
                                )
                            }

                            RequestSection.WIKI -> {
                                sagaDetailUseCase.generateWikiInsight(sagaContent)
                            }

                            RequestSection.EVENTS -> {
                                sagaDetailUseCase.generateTimelineInsight(
                                    sagaContent,
                                )
                            }

                            RequestSection.CHAPTERS -> {
                                sagaDetailUseCase.generateSagaResume(sagaContent)
                            }

                            RequestSection.ACTS -> {
                                sagaDetailUseCase.generateSagaResume(sagaContent)
                            }
                        }

                    result.onSuccess { insight ->
                        updateSectionWithInsight(section, insight)
                    }
                }
        }

        private fun updateSectionWithInsight(
            section: RequestSection,
            insight: String,
        ) {
            val currentSection = sectionCache[section] ?: return
            val updatedSection =
                when (currentSection) {
                    is DetailSectionView.InitialSection -> currentSection.copy(sagaResume = insight)
                    is DetailSectionView.CharacterSection -> currentSection.copy(insight = insight)
                    is DetailSectionView.WikiSection -> currentSection.copy(insight = insight)
                    is DetailSectionView.EventsSection -> currentSection.copy(insight = insight)
                    is DetailSectionView.ChapterSection -> currentSection.copy(insight = insight)
                    is DetailSectionView.ActSection -> currentSection.copy(insight = insight)
                }
            sectionCache[section] = updatedSection
            if (_actualSection.value?.title == currentSection.title) {
                _actualSection.value = updatedSection
            }
        }

        fun handleAction(detailAction: DetailAction) {
            viewModelScope.launch {
                when (detailAction) {
                    DetailAction.Delete -> deleteSaga(saga.value?.data)
                    is DetailAction.OpenSection -> loadSection(detailAction.section)
                    DetailAction.RegenerateIcon -> regenerateIcon()
                    else -> doNothing()
                }
            }
        }

        fun fetchSagaDetails(sagaId: String) {
            showIntro.value = true
            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase.fetchSaga(sagaId.toInt()).collectLatest { saga ->
                    saga?.let { data ->
                        sectionCache.clear()
                        this@SagaDetailViewModel.saga.value = data
                        visualConfig.value = visualConfigService.getVisualConfig(data.data.genre)

                        loadSection(currentRequestSection)
                        detailDrawer.value = sagaDetailUIMapper.buildDrawer(saga)
                        launchIntroSequence()
                    }
                }
            }
        }

        fun deleteSaga(saga: Saga?) {
            viewModelScope.launch {
                if (saga == null) {
                    return@launch
                }
                _state.value = State.Loading
                sagaDetailUseCase.deleteSaga(saga)
                _state.value = State.Deleted
            }
        }

        fun regenerateIcon() {
            val currentSaga = saga.value ?: return
            isGenerating.value = true
            _loadingMessage.value = "Regenerating saga icon..."
            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase
                    .regenerateSagaIcon(
                        currentSaga,
                    )
                isGenerating.value = false
                _loadingMessage.value = null
            }
        }

        fun generateTimelineContent(timelineContent: TimelineContent) {
            val currentSaga = saga.value ?: return
            viewModelScope.launch {
                isGenerating.value = true
                _loadingMessage.value = "Generating timeline content..."
                sagaDetailUseCase.generateTimelineContent(currentSaga, timelineContent)
                isGenerating.value = false
                _loadingMessage.value = null
            }
        }

        private fun launchIntroSequence() {
            viewModelScope.launch {
                delay(2.seconds)
                showIntro.value = false
            }
        }

        fun reviewWiki(wikis: List<Wiki>) {
            val currentsaga = saga.value ?: return
            viewModelScope.launch {
                isGenerating.emit(true)
                _loadingMessage.value = "Reviewing wiki entries..."
                sagaDetailUseCase.reviewWiki(currentsaga, wikis)
                isGenerating.emit(false)
                _loadingMessage.value = null
            }
        }
    }
