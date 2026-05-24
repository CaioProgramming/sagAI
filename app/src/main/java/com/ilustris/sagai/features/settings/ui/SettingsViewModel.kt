package com.ilustris.sagai.features.settings.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.utils.restartApp
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import com.ilustris.sagai.features.settings.domain.StorageBreakdown
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
        private val visualConfigService: GenreVisualConfigService,
        private val stringHelper: StringResourceHelper,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        val notificationsEnabled = settingsUseCase.getNotificationsEnabled()

        val smartSuggestionsEnabled = settingsUseCase.getSmartSuggestionsEnabled()

        val messageEffectsEnabled = settingsUseCase.getMessageEffectsEnabled()
        val showTutorials = settingsUseCase.getShowTutorials()
        val musicEnabled = settingsUseCase.getMusicEnabled()
        val backupEnabled = settingsUseCase.backupEnabled()

        private val _memoryUsage = MutableStateFlow<Long?>(null)
        val memoryUsage = _memoryUsage.asStateFlow()

        private val _isUserPro = MutableStateFlow<Boolean>(false)
        val isUserPro = _isUserPro.asStateFlow()

        val sagaStorageInfo = settingsUseCase.getSagas()

        private val _visualConfigs = MutableStateFlow<Map<Genre, GenreVisualConfig>>(emptyMap())
        val visualConfigs = _visualConfigs.asStateFlow()

        private val _storageBreakdown = MutableStateFlow(StorageBreakdown(0L, 0L, 0L))
        val storageBreakdown = _storageBreakdown.asStateFlow()
        val isLoading = MutableStateFlow(false)
        val loadingMessage = MutableStateFlow<String?>(null)

        private val _hasSagasWithChapters = MutableStateFlow<Boolean?>(null)
        val hasSagasWithChapters = _hasSagasWithChapters.asStateFlow()

        init {
            loadMemoryUsage()
            checkUserPro()
            loadStorageBreakdown()
            checkHasSagasWithChapters()
            loadVisualConfigs()
        }

        private fun loadVisualConfigs() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaStorageInfo.collect { storageList ->
                    val genres = storageList.map { it.data.genre }.distinct()
                    val configs = _visualConfigs.value.toMutableMap()
                    genres.forEach { genre ->
                        if (!configs.containsKey(genre)) {
                            visualConfigService.getVisualConfig(genre)?.let {
                                configs[genre] = it
                                _visualConfigs.emit(configs.toMap())
                            }
                        }
                    }
                }
            }
        }

        fun checkHasSagasWithChapters() {
            viewModelScope.launch {
                _hasSagasWithChapters.value = settingsUseCase.hasSagasWithChapters()
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

        fun setShowTutorials(enabled: Boolean) {
            viewModelScope.launch {
                settingsUseCase.setShowTutorials(enabled)
            }
        }

        fun setMusicEnabled(enabled: Boolean) {
            viewModelScope.launch {
                settingsUseCase.setMusicEnabled(enabled)
            }
        }

        fun wipeAppData() {
            viewModelScope.launch {
                isLoading.value = true
                loadingMessage.emit(stringHelper.getString(R.string.settings_wipe_loading))
                settingsUseCase.wipeAppData()
                loadingMessage.emit(stringHelper.getString(R.string.settings_wipe_success))
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

        fun disableBackup() {
            viewModelScope.launch {
                settingsUseCase.disableBackup()
            }
        }

        fun exportDatabase(destinationUri: Uri) {
            viewModelScope.launch {
                isLoading.value = true
                loadingMessage.emit(stringHelper.getString(R.string.settings_export_database_loading))
                settingsUseCase
                    .exportDatabase(destinationUri)
                    .onSuccessAsync {
                        loadingMessage.emit(stringHelper.getString(R.string.settings_export_database_success))
                        delay(3.seconds)
                        isLoading.value = false
                        loadingMessage.emit(null)
                    }.onFailureAsync {
                        loadingMessage.emit(stringHelper.getString(R.string.settings_export_database_failed))
                        delay(3.seconds)
                        isLoading.value = false
                        loadingMessage.emit(null)
                    }
            }
        }

        fun importDatabase(sourceUri: Uri) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        sourceUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                } catch (_: SecurityException) {
                    // OpenDocument grants one-shot read; tree URIs may not support persistable.
                }
                isLoading.value = true
                loadingMessage.emit(stringHelper.getString(R.string.backup_loading_restoring_database))
                settingsUseCase
                    .importDatabase(sourceUri)
                    .onSuccessAsync {
                        loadingMessage.emit(stringHelper.getString(R.string.import_database_success))
                        delay(2.seconds)
                        context.restartApp()
                    }.onFailureAsync {
                        loadingMessage.emit(stringHelper.getString(R.string.import_database_failed))
                        delay(3.seconds)
                        isLoading.value = false
                        loadingMessage.emit(null)
                    }
            }
        }

        val totalPlaytime =
            settingsUseCase
                .getSagasOnly()
                .map { sagas ->
                    sagas.sumOf { it.playTimeMs }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

        fun clearPreferences() {
            viewModelScope.launch {
                settingsUseCase.clearPreferences()
            }
        }
    }

data class SagaStorageInfo(
    val data: Saga,
    val sizeBytes: Long,
)
