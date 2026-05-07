package com.ilustris.sagai.features.saga.detail.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.media.SoundFxService
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.vibrationPattern
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
        private val soundFxService: SoundFxService,
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

        val visualConfig = MutableStateFlow<GenreVisualConfig?>(null)
        private val _initialSection = MutableStateFlow<DetailSectionView.InitialSection?>(null)
        val initialSection = _initialSection.asStateFlow()

        val detailDrawer = MutableStateFlow<TimelineDrawer?>(null)

        fun togglePremiumSheet() {
            showPremiumSheet.value = !showPremiumSheet.value
        }

        fun loadInitialSection() {
            val currentSaga = saga.value ?: return
            viewModelScope.launch {
                sagaDetailUIMapper
                    .buildSection(
                        currentSaga,
                        RequestSection.START,
                    ).onSuccess { mappedSection ->
                        _initialSection.value = mappedSection as DetailSectionView.InitialSection
                        _state.value = State.Success(currentSaga)
                        playSoundFx()
                    }.onFailureAsync {
                        _state.emit(State.Error(emptyString()))
                    }
            }
        }

        private fun playSoundFx() {
            val selectedGenre = saga.value?.data?.genre ?: return
            viewModelScope.launch {
                delay(300)
                val visualConfig = visualConfigService.getVisualConfig(selectedGenre)
                val hapticPattern = selectedGenre.vibrationPattern(visualConfig)
            soundFxService.playWithHaptics(hapticPattern)
            }
        }

        fun handleAction(detailAction: DetailAction) {
            viewModelScope.launch {
                when (detailAction) {
                    DetailAction.Delete -> deleteSaga(saga.value?.data)
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
                        this@SagaDetailViewModel.saga.value = data
                        visualConfig.value = visualConfigService.getVisualConfig(data.data.genre)

                        loadInitialSection()
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
