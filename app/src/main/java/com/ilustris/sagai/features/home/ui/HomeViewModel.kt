package com.ilustris.sagai.features.home.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.core.file.backup.filterBackups
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.usecase.HomeUseCase
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



        init {
            checkDebug()
            getDynamicPrompts()
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
