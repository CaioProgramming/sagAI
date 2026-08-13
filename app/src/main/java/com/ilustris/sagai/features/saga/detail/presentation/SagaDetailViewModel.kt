package com.ilustris.sagai.features.saga.detail.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.data.model.ImagePalette
import com.ilustris.sagai.core.theme.SagaImmersiveSession
import com.ilustris.sagai.core.theme.SagaThemeManager
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.detail.data.model.SagaDetailResume
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.SagaDetailUIMapper
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.TimelineDrawer
import com.ilustris.sagai.features.saga.detail.review.domain.ReviewGenerationCoordinator
import com.ilustris.sagai.features.saga.detail.review.domain.ReviewGenerationState
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
        private val sagaImmersiveSession: SagaImmersiveSession,
        private val stringResourceHelper: StringResourceHelper,
        private val reviewGenerationCoordinator: ReviewGenerationCoordinator,
    ) : ViewModel() {
        private val _state = MutableStateFlow<State>(State.Success(Unit))
        val state: StateFlow<State> = _state.asStateFlow()
        val sagaResume = MutableStateFlow<SagaDetailResume?>(null)
        val showIntro = MutableStateFlow(false)
        val backupEnabled = sagaDetailUseCase.getBackupEnabled()

        val showPremiumSheet = MutableStateFlow(false)

        private val _initialSection = MutableStateFlow<DetailSectionView.InitialSection?>(null)
        val initialSection = _initialSection.asStateFlow()

        val imagePalette = MutableStateFlow<ImagePalette?>(null)

        val detailDrawer = MutableStateFlow<TimelineDrawer?>(null)

        private val _reviewGenerationState =
            MutableStateFlow<ReviewGenerationState>(ReviewGenerationState.Idle)
        val reviewGenerationState = _reviewGenerationState.asStateFlow()

        private var reviewGenerationJob: kotlinx.coroutines.Job? = null

        private var cachedSegmentedImage: Pair<Bitmap, Bitmap>? = null
        private var cachedIconPath: String? = null
        private var initialSectionJob: kotlinx.coroutines.Job? = null

        /** One entry SFX per detail visit — cleared when the screen is hidden. */
        private var pendingEntryVfxSagaId: Int? = null

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
                            _state.value = State.Success(resume.saga)
                            extractPalette()
                        }.onFailureAsync {
                            _state.emit(State.Error(emptyString()))
                        }
                }
        }

        private fun extractPalette() {
            if (imagePalette.value != null) return
            val originalBitmap = cachedSegmentedImage?.first ?: return
            viewModelScope.launch(Dispatchers.IO) {
                val paletteBitmap =
                    if (originalBitmap.config == Bitmap.Config.HARDWARE) {
                        originalBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return@launch
                    } else {
                        originalBitmap
                    }
                imagePalette.value = ImagePalette.fromBitmap(paletteBitmap)
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
            imagePalette.value = null
            showIntro.value = true
            fetchJob =
                viewModelScope.launch {
                    sagaDetailUseCase.getSagaResume(sagaId).collectLatest { resume ->
                        resume.let { data ->
                            this@SagaDetailViewModel.sagaResume.value = data
                            if (sagaImmersiveSession.isOwnerOnTop("saga_detail")) {
                                val playEntryVfx = pendingEntryVfxSagaId == sagaId
                                sagaThemeManager.updateTheme(
                                    data.saga.genre,
                                    playEntryVfx = playEntryVfx,
                                )
                                if (playEntryVfx) {
                                    pendingEntryVfxSagaId = null
                                }
                            }

                            loadInitialSection()
                            observeReviewGeneration(sagaId)
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
            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase.regenerateSagaIconStream(currentSagaId).collect { }
            }
        }

        private fun observeReviewGeneration(sagaId: Int) {
            reviewGenerationJob?.cancel()
            reviewGenerationJob =
                viewModelScope.launch {
                    reviewGenerationCoordinator.stateFor(sagaId).collect { state ->
                        _reviewGenerationState.value = state
                    }
                }
            if (sagaResume.value?.saga?.isEnded == true) {
                reviewGenerationCoordinator.enqueue(sagaId)
            }
        }

        fun onDetailScreenVisible(sagaId: Int) {
            sagaImmersiveSession.push("saga_detail", sagaId)
            pendingEntryVfxSagaId = sagaId
        }

        fun onDetailScreenHidden() {
            sagaImmersiveSession.pop("saga_detail")
            pendingEntryVfxSagaId = null
        }

        private fun launchIntroSequence() {
            viewModelScope.launch {
                delay(2.seconds)
                showIntro.value = false
            }
        }

        override fun onCleared() {
            pendingEntryVfxSagaId = null
            reviewGenerationJob?.cancel()
            super.onCleared()
            sagaImmersiveSession.pop("saga_detail")
        }
    }
