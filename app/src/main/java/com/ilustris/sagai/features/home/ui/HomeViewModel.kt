package com.ilustris.sagai.features.home.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.usecase.HomeUseCase
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val homeUseCase: HomeUseCase,
        private val backupService: BackupService,
        private val sagaBackupService: SagaBackupService,
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

        private val _showRecoverSheet = MutableStateFlow(false)
        val showRecoverSheet = _showRecoverSheet.asStateFlow()

        private val _importUri = MutableStateFlow<Uri?>(null)
        val importUri = _importUri.asStateFlow()

        val billingState = homeUseCase.billingState



        init {
            checkDebug()
            getDynamicPrompts()
        }



        fun handleImportUri(uri: Uri) {
            viewModelScope.launch {
                _importUri.emit(uri)
            }
        }

        fun clearImportUri() {
            viewModelScope.launch {
                _importUri.emit(null)
            }
        }

        fun importFromUri(uri: Uri) {
            viewModelScope.launch(Dispatchers.IO) {
                _isLoading.emit(true)
                loadingMessage.emit("Importing backup...")
                val metadata = backupService.getFileMetadata(uri)
                val result = when (val extension = metadata.name.substringAfterLast(".")) {
                    "saga" -> sagaBackupService.restoreSagaFromUri(uri)
                    "sagas", "zip" -> backupService.restoreFullBackup(uri)
                    else -> error("Unsupported file type: $extension")
                }
                result.onSuccessAsync {
                    loadingMessage.emit("Backup imported successfully!")
                }.onFailureAsync {
                    loadingMessage.emit("Error importing backup: ${it.message}")
                }
                delay(2.seconds)
                _isLoading.emit(false)
                loadingMessage.emit(null)
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
