package com.ilustris.sagai.features.home.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaSummary
import com.ilustris.sagai.features.home.data.usecase.HomeUseCase
import com.ilustris.sagai.features.stories.data.model.StoryDailyBriefing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/** Retained for [com.ilustris.sagai.features.stories.ui.StorySheet]; no longer used from home. */
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
        private val stringResourceHelper: StringResourceHelper,
    ) : ViewModel() {
        val sagas: MutableStateFlow<List<SagaSummary>> = MutableStateFlow(emptyList())

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

        private val _isStarting = MutableStateFlow<Boolean>(sagas.value.isEmpty())
        val isStarting = _isStarting.asStateFlow()
        val loadingMessage = MutableStateFlow<String?>(null)

        private val _showRecoverSheet = MutableStateFlow(false)
        val showRecoverSheet = _showRecoverSheet.asStateFlow()

        val billingState = homeUseCase.billingState

        init {
            Timber.d("HomeViewModel: init")
            checkDebug()
            getDynamicPrompts()
            loadSagas()
        }

        private fun loadSagas() {
            viewModelScope.launch(Dispatchers.IO) {
                homeUseCase.getSagas().collect { sagaList ->
                    sagas.emit(sagaList)
                }
            }
        }

        override fun onCleared() {
            super.onCleared()
            Timber.d("HomeViewModel: onCleared")
        }

        fun checkForBackups() {
            viewModelScope.launch {
                /*backupService.getBackedUpSagas().onSuccessAsync {
                    val availableSagas = sagas.first()
                    _backupAvailable.emit(
                        it.filterBackups(availableSagas.map { it.data }).isNotEmpty(),
                    )
                }*/
            }
        }

        private fun getDynamicPrompts() {
            viewModelScope.launch {
                homeUseCase
                    .requestDynamicCall()
                    .onSuccessAsync {
                        _dynamicNewSagaTexts.emit(it)
                        if (_isStarting.value) {
                            _isStarting.emit(false)
                        }
                    }.onFailureAsync {
                        _dynamicNewSagaTexts.value =
                            DynamicSagaPrompt(
                                stringResourceHelper.getString(R.string.home_create_new_saga_title),
                                stringResourceHelper.getString(R.string.home_create_new_saga_subtitle),
                            )
                        if (_isStarting.value) {
                            _isStarting.emit(false)
                        }
                    }
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

        private var isBackingUp = false

        fun autoBackup() {
            if (isBackingUp) return
            viewModelScope.launch(Dispatchers.IO) {
                isBackingUp = true
                homeUseCase.autoBackup()
                isBackingUp = false
            }
        }
    }
