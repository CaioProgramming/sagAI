package com.ilustris.sagai.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.RequestState
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.usecase.HomeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val homeUseCase: HomeUseCase,
    ) : ViewModel() {
        val sagas = homeUseCase.getSagas()

        private val _showDebugButton = MutableStateFlow(false)
        val showDebugButton = _showDebugButton.asStateFlow()

        private val _startDebugSaga = MutableStateFlow<Saga?>(null)
        val startDebugSaga = _startDebugSaga.asStateFlow()

        private val _dynamicNewSagaTexts = MutableStateFlow<DynamicSagaPrompt?>(null)
        val dynamicNewSagaTexts = _dynamicNewSagaTexts.asStateFlow()

        private val _isLoading = MutableStateFlow<Boolean>(false)
        val isLoading = _isLoading.asStateFlow()

        val loadingMessage = MutableStateFlow<String?>(null)

        private val _backups = MutableStateFlow<List<SagaContent>>(emptyList())
        val backups = _backups.asStateFlow()

        val backupEnabled = homeUseCase.backupEnabled()

        private val _showRecoverSheet = MutableStateFlow(false)
        val showRecoverSheet = _showRecoverSheet.asStateFlow()

        val billingState = homeUseCase.billingState

        init {
            checkDebug()
            getDynamicPrompts()
            checkBackups()
        }

        fun updateRecoverSheet(show: Boolean) {
            _showRecoverSheet.update { show }
        }

        private fun checkBackups() {
            viewModelScope.launch {
                val backupsData = homeUseCase.checkBackups().getSuccess() ?: emptyList()
                _backups.emit(backupsData)
                if (backupsData.isNotEmpty()) {
                    _showRecoverSheet.update { true }
                }
            }
        }

        fun recoverSaga(sagaContent: SagaContent) {
            viewModelScope.launch {
                if (isLoading.value.not()) {
                    _isLoading.emit(true)
                }
                loadingMessage.emit("Restaurando ${sagaContent.data.title}.")
                homeUseCase.recoverSaga(sagaContent)
                checkBackups()
                delay(3.seconds)
                _isLoading.emit(false)
                loadingMessage.emit(null)
            }
        }

        fun recoverAllSagas() {
            viewModelScope.launch {
                backups.value.forEach {
                    homeUseCase.recoverSaga(it)
                }
                checkBackups()
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
