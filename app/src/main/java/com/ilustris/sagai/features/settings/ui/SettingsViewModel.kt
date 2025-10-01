package com.ilustris.sagai.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

        private val _memoryUsage = MutableStateFlow<Long?>(null)
        val memoryUsage = _memoryUsage.asStateFlow()

        private val _isUserPro = MutableStateFlow<Boolean>(false)
        val isUserPro = _isUserPro.asStateFlow()

        private val _sagaStorageInfo = MutableStateFlow<List<SagaStorageInfo>>(emptyList())
        val sagaStorageInfo = _sagaStorageInfo.asStateFlow()

        private val _storageBreakdown = MutableStateFlow(SettingsUseCase.StorageBreakdown(0L, 0L, 0L))
        val storageBreakdown = _storageBreakdown.asStateFlow()

        init {
            loadMemoryUsage()
            checkUserPro()
            loadSagaStorageInfo()
            loadStorageBreakdown()
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

        fun setNotificationsEnabled(enabled: Boolean) {
            viewModelScope.launch {
                settingsUseCase.setNotificationsEnabled(enabled)
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

        fun loadSagaStorageInfo() {
            viewModelScope.launch {
                settingsUseCase.getSagas().collect { sagaList ->
                    val sagaInfoList =
                        sagaList.map { sagaContent ->
                            val sagaId = sagaContent.data.id
                            val sagaName = sagaContent.data.title
                            val sagaIcon = sagaContent.data.icon
                            val size = settingsUseCase.getSagaStorageUsage(sagaId)
                            SagaStorageInfo(
                                id = sagaId,
                                name = sagaName,
                                icon = sagaIcon,
                                genre = sagaContent.data.genre,
                                sizeBytes = size,
                            )
                        }
                    _sagaStorageInfo.value = sagaInfoList
                }
            }
        }

        fun loadStorageBreakdown() {
            viewModelScope.launch {
                _storageBreakdown.value = settingsUseCase.getStorageBreakdown()
            }
        }
    }

data class SagaStorageInfo(
    val id: Int,
    val name: String,
    val icon: String,
    val sizeBytes: Long,
    val genre: Genre,
)
