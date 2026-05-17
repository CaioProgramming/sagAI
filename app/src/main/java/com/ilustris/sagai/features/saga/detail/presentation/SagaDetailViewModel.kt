package com.ilustris.sagai.features.saga.detail.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.theme.SagaThemeManager
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.detail.data.model.SagaDetailResume
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.SagaDetailUIMapper
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.TimelineDrawer
import com.ilustris.sagai.features.saga.detail.ui.DetailAction
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
        private val sagaDetailUIMapper: SagaDetailUIMapper,
        private val sagaThemeManager: SagaThemeManager,
    ) : ViewModel() {
        private val _state = MutableStateFlow<State>(State.Success(Unit))
        val state: StateFlow<State> = _state.asStateFlow()
        val sagaResume = MutableStateFlow<SagaDetailResume?>(null)
        val isGenerating = MutableStateFlow(false)
        val showIntro = MutableStateFlow(false)
        private val _loadingMessage = MutableStateFlow<String?>(null)
        val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()
        val backupEnabled = sagaDetailUseCase.getBackupEnabled()

        val showPremiumSheet = MutableStateFlow(false)

        private val _initialSection = MutableStateFlow<DetailSectionView.InitialSection?>(null)
        val initialSection = _initialSection.asStateFlow()

        val detailDrawer = MutableStateFlow<TimelineDrawer?>(null)

        private var cachedSegmentedImage: Pair<Bitmap, Bitmap>? = null
        private var cachedIconPath: String? = null
        private var initialSectionJob: kotlinx.coroutines.Job? = null

        fun togglePremiumSheet() {
            showPremiumSheet.value = !showPremiumSheet.value
        }

        fun loadInitialSection() {
            val resume = sagaResume.value ?: return
            initialSectionJob?.cancel()
            initialSectionJob =
                viewModelScope.launch {
                    val saga = resume.saga
                    if (cachedIconPath != saga.icon) {
                        cachedIconPath = saga.icon
                        cachedSegmentedImage = null
                    }

                    sagaDetailUIMapper
                        .buildSection(
                            resume = resume,
                            section = RequestSection.START,
                            existingSegmentedImage = cachedSegmentedImage,
                        ).onSuccess { mappedSection ->
                            val section = mappedSection as DetailSectionView.InitialSection
                            _initialSection.value =
                                if (cachedSegmentedImage != null) {
                                    section.copy(segmentedImage = cachedSegmentedImage)
                                } else {
                                    cachedSegmentedImage = section.segmentedImage
                                    section
                                }
                            if (_state.value !is State.Success) {
                                playSoundFx()
                            }

                            _state.value = State.Success(resume.saga)
                        }.onFailureAsync {
                            _state.emit(State.Error(emptyString()))
                        }
                }
        }

        private fun playSoundFx() {
            viewModelScope.launch {
                sagaThemeManager.playVfx()
            }
        }

        fun handleAction(detailAction: DetailAction) {
            viewModelScope.launch {
                when (detailAction) {
                    DetailAction.Delete -> deleteSaga(sagaResume.value?.saga)
                    DetailAction.RegenerateIcon -> regenerateIcon()
                    else -> doNothing()
                }
            }
        }

        private var fetchJob: kotlinx.coroutines.Job? = null

        fun fetchSagaDetails(sagaId: Int) {
            if (sagaResume.value?.saga?.id == sagaId) {
                return
            }
            fetchJob?.cancel()
            sagaResume.value = null
            cachedIconPath = null
            cachedSegmentedImage = null
            _initialSection.value = null
            showIntro.value = true
            fetchJob =
                viewModelScope.launch {
                    sagaDetailUseCase.getSagaResume(sagaId).collectLatest { resume ->
                        resume.let { data ->
                            this@SagaDetailViewModel.sagaResume.value = data
                            sagaThemeManager.updateTheme(data.saga.genre)

                            loadInitialSection()
                            detailDrawer.value =
                                sagaDetailUIMapper.buildDrawer(
                                    data.saga,
                                    data.fullChapters,
                                    data.completedActsCount,
                                )
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
            val currentSagaId = sagaResume.value?.saga?.id ?: return
            isGenerating.value = true
            _loadingMessage.value = "Regenerating saga icon..."
            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase
                    .regenerateSagaIconStream(currentSagaId)
                    .collect { state ->
                        when (state) {
                            is StreamingState.Reasoning -> {
                                _loadingMessage.value = state.chunk
                            }

                            is StreamingState.Success -> {
                                isGenerating.value = false
                                _loadingMessage.value = null
                            }

                            is StreamingState.Error -> {
                                isGenerating.value = false
                                _loadingMessage.value = "Error regenerating icon: ${state.message}"
                            }
                        }
                    }
            }
        }

        private fun launchIntroSequence() {
            viewModelScope.launch {
                delay(2.seconds)
                showIntro.value = false
            }
        }
    }
