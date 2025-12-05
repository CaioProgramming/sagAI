package com.ilustris.sagai.features.home.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.core.file.backup.filterBackups
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val homeUseCase: HomeUseCase,
    private val backupService: BackupService,
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

    private val _briefingCache = mutableMapOf<Int, StoryDailyBriefing>()

    private val _selectedSaga = MutableStateFlow<SagaContent?>(null)
    val selectedSaga = _selectedSaga.asStateFlow()

    private val _storyBriefing = MutableStateFlow<StoryDailyBriefing?>(null)
    val storyBriefing = _storyBriefing.asStateFlow()

    private val _isGeneratingBriefing = MutableStateFlow(false)
    val isGeneratingBriefing = _isGeneratingBriefing.asStateFlow()

    init {
        checkDebug()
        getDynamicPrompts()
    }

    fun getBriefing(saga: SagaContent) {
        viewModelScope.launch {
            _selectedSaga.emit(saga)
            if (_briefingCache.containsKey(saga.data.id)) {
                _storyBriefing.emit(_briefingCache[saga.data.id])
            } else {
                _isGeneratingBriefing.emit(true)
                val result = homeUseCase.generateStoryBriefing(saga)
                if (result is RequestResult.Success) {
                    _briefingCache[saga.data.id] = result.data
                    _storyBriefing.emit(result.data)
                }
                _isGeneratingBriefing.emit(false)
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
                    it.filterBackups(availableSagas.map { it.data }).isNotEmpty()
                )
            }
        }
    }



    private fun getDynamicPrompts() {
        viewModelScope.launch {
            _dynamicNewSagaTexts.emit(homeUseCase.requestDynamicCall().getSuccess())
        }
    }

    private fun checkDebug() {
        viewModelScope.launch {
            _showDebugButton.value = homeUseCase.checkDebugBuild()
        }
    }

    fun createFakeSaga() {
        viewModelScope.launch(Dispatchers.IO) {
            homeUseCase.createFakeSaga().onSuccessAsync {
                _startDebugSaga.emit(it)
                delay(3.seconds)
                _startDebugSaga.emit(null)
            }
        }
    }
}
