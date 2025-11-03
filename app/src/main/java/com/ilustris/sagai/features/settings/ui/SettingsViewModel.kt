package com.ilustris.sagai.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import com.ilustris.sagai.features.settings.domain.StorageBreakdown
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsUseCase: SettingsUseCase,
    ) : ViewModel() {
        val notificationsEnabled = settingsUseCase.getNotificationsEnabled()

        val smartSuggestionsEnabled = settingsUseCase.getSmartSuggestionsEnabled()

        val backupEnabled = settingsUseCase.backupEnabled()

        private val _memoryUsage = MutableStateFlow<Long?>(null)
        val memoryUsage = _memoryUsage.asStateFlow()

        private val _isUserPro = MutableStateFlow<Boolean>(false)
        val isUserPro = _isUserPro.asStateFlow()

        val sagaStorageInfo = settingsUseCase.getSagas()

        private val _storageBreakdown = MutableStateFlow(StorageBreakdown(0L, 0L, 0L))
        val storageBreakdown = _storageBreakdown.asStateFlow()

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

        fun wipeAppData(onComplete: (() -> Unit)? = null) {
            viewModelScope.launch {
                settingsUseCase.wipeAppData()
                loadMemoryUsage()
                onComplete?.invoke()
            }
        }

        fun loadStorageBreakdown() {
            viewModelScope.launch {
                _storageBreakdown.value = settingsUseCase.getStorageBreakdown()
            }
        }
    }

data class SagaStorageInfo(
    val data: Saga,
    val sizeBytes: Long,
)
