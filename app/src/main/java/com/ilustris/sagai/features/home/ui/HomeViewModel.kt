package com.ilustris.sagai.features.home.ui

import android.graphics.Bitmap
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.backup.filterBackups
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.usecase.HomeUseCase
import com.ilustris.sagai.features.stories.data.model.StoryDailyBriefing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

data class SagaBriefing(
    val saga: SagaContent,
    val briefing: StoryDailyBriefing,
    val segmentationPair: Pair<Bitmap, Bitmap>? = null,
)

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val homeUseCase: HomeUseCase,
        private val backupService: BackupService,
        private val segmentationHelper: ImageSegmentationHelper,
        private val stringResourceHelper: StringResourceHelper,
    ) : ViewModel() {
        val sagas = homeUseCase.getSagas()

        private val _showDebugButton = MutableStateFlow(false)
        val showDebugButton = _showDebugButton.asStateFlow()

        private val _startDebugSaga = MutableStateFlow<Saga?>(null)
        val startDebugSaga = _startDebugSaga.asStateFlow()

        private val _dynamicNewSagaTexts = MutableStateFlow<DynamicSagaPrompt?>(null)
        val dynamicNewSagaTexts = _dynamicNewSagaTexts.asStateFlow()

        private val _backupAvailable = MutableStateFlow(false)
        val backupAvailable = _backupAvailable.asStateFlow()

        private val _isLoading = MutableStateFlow<Boolean>(false)
        val isLoading = _isLoading.asStateFlow()

        val loadingMessage = MutableStateFlow<String?>(null)

        private val _showRecoverSheet = MutableStateFlow(false)
        val showRecoverSheet = _showRecoverSheet.asStateFlow()

        val billingState = homeUseCase.billingState

        private val _briefingCache = mutableMapOf<Int, SagaBriefing>()

        private val _selectedSaga = MutableStateFlow<SagaContent?>(null)
        val selectedSaga = _selectedSaga.asStateFlow()

        private val _storyBriefing = MutableStateFlow<SagaBriefing?>(null)
        val storyBriefing = _storyBriefing.asStateFlow()

        private val _loadingStoryId = MutableStateFlow<Int?>(null)
        val loadingStoryId = _loadingStoryId.asStateFlow()
        val segmentedImageCache = LruCache<String, Bitmap?>(5 * 1024 * 1024) // 5MB cache

        init {
            checkDebug()
            getDynamicPrompts()
        }

        fun getBriefing(saga: SagaContent) {
            viewModelScope.launch(Dispatchers.IO) {
                _loadingStoryId.emit(saga.data.id)
                if (_briefingCache.containsKey(saga.data.id)) {
                    _storyBriefing.emit(_briefingCache[saga.data.id])
                    _selectedSaga.emit(saga)
                    _loadingStoryId.emit(null)
                } else {
                    homeUseCase.generateStoryBriefing(saga).onSuccessAsync {
                        val iconSegmentation = segmentationHelper.processImage(saga.data.icon)
                        val briefingState = SagaBriefing(saga, it, iconSegmentation.getSuccess())
                        _briefingCache[saga.data.id] = briefingState
                        _storyBriefing.emit(briefingState)
                        _selectedSaga.emit(saga)
                    }
                    _loadingStoryId.emit(null)
                }
            }
        }

        fun clearSelectedSaga() {
            viewModelScope.launch {
                _selectedSaga.emit(null)
                _storyBriefing.emit(null)
            }
        }

        fun checkForBackups() {
            viewModelScope.launch {
                backupService.getBackedUpSagas().onSuccessAsync {
                    val availableSagas = sagas.first()
                    _backupAvailable.emit(
                        it.filterBackups(availableSagas.map { it.data }).isNotEmpty(),
                    )
                }
            }
        }

        private fun getDynamicPrompts() {
            viewModelScope.launch {
                val result =
                    homeUseCase.requestDynamicCall().getSuccess() ?: DynamicSagaPrompt(
                        stringResourceHelper.getString(R.string.home_create_new_saga_title),
                        stringResourceHelper.getString(R.string.home_create_new_saga_subtitle),
                    )
                _dynamicNewSagaTexts.emit(result)
            }
        }

        private fun checkDebug() {
            viewModelScope.launch {
                _showDebugButton.value = homeUseCase.checkDebugBuild()
            }
        }

        fun createFakeSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                val result = homeUseCase.createFakeSaga()
                if (result is RequestResult.Success) {
                    _startDebugSaga.emit(result.value)
                    delay(3.seconds)
                    _startDebugSaga.emit(null)
                }
            }
        }
    }
