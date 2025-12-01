package com.ilustris.sagai.features.settings.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.playthrough.PlaythroughUseCase
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import com.ilustris.sagai.features.settings.domain.StorageBreakdown
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
        private val backupService: BackupService,
        private val sagaRepository: SagaRepository,
        private val playthroughUseCase: PlaythroughUseCase,
        @ApplicationContext private val context: Context,
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

        private val _playthroughCardPrompt = MutableStateFlow<DynamicSagaPrompt?>(null)
        val playthroughCardPrompt = _playthroughCardPrompt.asStateFlow()

        init {
            loadMemoryUsage()
            checkUserPro()
            loadStorageBreakdown()
            loadPlaythroughCardPrompt()
        }

        private fun loadPlaythroughCardPrompt() {
            viewModelScope.launch {
                when (val result = playthroughUseCase.getPlaythroughCardPrompt()) {
                    is RequestResult.Success -> {
                        _playthroughCardPrompt.value = result.value
                    }
                    is RequestResult.Error -> {
                        _playthroughCardPrompt.value = DynamicSagaPrompt(
                            context.getString(R.string.settings_your_playthrough_title),
                            context.getString(R.string.settings_your_playthrough_subtitle)
                        )
                    }
                }
            }
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
                settingsUseCase.restoreSaga(uri)
            }
        }

        fun exportAllSagas(destinationUri: Uri) {
            viewModelScope.launch {
                isLoading.value = true
                loadingMessage.emit("Exporting all sagas...")
                val sagas = sagaRepository.getChats().first()
                val backupName = "SagaAI_Full_Backup_${System.currentTimeMillis()}" // Consider using this as internal name
                backupService.createFullBackup(destinationUri, backupName, sagas).onSuccessAsync {
                    loadingMessage.emit("All sagas exported successfully!")
                }.onFailureAsync {
                    loadingMessage.emit("Error exporting sagas: ${it.message}")
                }
                delay(2.seconds)
                isLoading.value = false
                loadingMessage.emit(null)
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
