package com.ilustris.sagai.features.settings.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import com.ilustris.sagai.features.settings.domain.StorageBreakdown
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsUseCase: SettingsUseCase,
    ) : ViewModel() {
        val notificationsEnabled = settingsUseCase.getNotificationsEnabled()

        val smartSuggestionsEnabled = settingsUseCase.getSmartSuggestionsEnabled()

        val messageEffectsEnabled = settingsUseCase.getMessageEffectsEnabled()

        val backupEnabled = settingsUseCase.backupEnabled()

        private val _memoryUsage = MutableStateFlow<Long?>(null)
        val memoryUsage = _memoryUsage.asStateFlow()

        private val _isUserPro = MutableStateFlow<Boolean>(false)
        val isUserPro = _isUserPro.asStateFlow()

        val sagaStorageInfo = settingsUseCase.getSagas()

        private val _storageBreakdown = MutableStateFlow(StorageBreakdown(0L, 0L, 0L))
        val storageBreakdown = _storageBreakdown.asStateFlow()
        val isLoading = MutableStateFlow(false)
        val loadingMessage = MutableStateFlow<String?>(null)

        init {
            loadMemoryUsage()
            checkUserPro()
            loadStorageBreakdown()
        }

        fun clearCache() {
            viewModelScope.launch {
                settingsUseCase.clearCache()
                loadMemoryUsage()
                loadStorageBreakdown()
            }
        }

        fun loadMemoryUsage() {
            viewModelScope.launch {
                _memoryUsage.value = settingsUseCase.getAppStorageUsage()
            }
        }

        fun checkUserPro() {
            viewModelScope.launch {
                _isUserPro.value = settingsUseCase.isUserPro()
            }
        }

        fun setSmartSuggestionsEnabled(enabled: Boolean) {
            viewModelScope.launch {
                settingsUseCase.setSmartSuggestionsEnabled(enabled)
            }
        }

        fun setMessageEffectsEnabled(enabled: Boolean) {
            viewModelScope.launch {
                settingsUseCase.setMessageEffectsEnabled(enabled)
            }
    }

        fun wipeAppData() {
            viewModelScope.launch {
                isLoading.value = true
                loadingMessage.emit("Limpando seus universos...")
                settingsUseCase.wipeAppData()
                loadingMessage.emit("Suas histórias foram removidas, hora de recomeçar!")
                loadMemoryUsage()
                delay(2.seconds)
                isLoading.value = false
                loadingMessage.emit(null)
            }
        }

        fun loadStorageBreakdown() {
            viewModelScope.launch {
                _storageBreakdown.value = settingsUseCase.getStorageBreakdown()
            }
        }

        fun importSaga(uri: Uri) {
            viewModelScope.launch {
                isLoading.value = true
                loadingMessage.emit("Recuperando informações...")
                settingsUseCase
                    .restoreSaga(uri)
                    .onSuccessAsync {
                        isLoading.value = true
                        loadingMessage.emit("${it.data.title} restaurado com sucesso!")
                        delay(5.seconds)
                        isLoading.value = false
                    }.onFailureAsync {
                        isLoading.value = true
                        loadingMessage.emit("Falha ao restaurar saga. Sentimos muito por isso...")
                        delay(5.seconds)
                        isLoading.value = false
                    }
            }
        }

        fun disableBackup() {
            viewModelScope.launch {
                settingsUseCase.disableBackup()
            }
        }

        val totalPlaytime =
            settingsUseCase
                .getSagas()
                .map { sagas ->
                    sagas.sumOf { it.data.playTimeMs }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    }

data class SagaStorageInfo(
    val data: Saga,
    val sizeBytes: Long,
)
