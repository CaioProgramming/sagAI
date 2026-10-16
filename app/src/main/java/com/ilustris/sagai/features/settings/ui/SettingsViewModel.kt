package com.ilustris.sagai.features.settings.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.database.backup.ArchiveManifest
import com.ilustris.sagai.core.database.backup.SagasArchiveService
import com.ilustris.sagai.core.services.AdTier
import com.ilustris.sagai.core.services.AdsService
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.restartApp
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import com.ilustris.sagai.features.settings.domain.StorageBreakdown
import com.ilustris.sagai.ui.components.island.ChatIslandService
import com.ilustris.sagai.ui.components.island.IslandContent
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
        private val chatIslandService: ChatIslandService,
        val debugImageFallbackService: DebugImageFallbackService,
        private val adsService: AdsService,
        private val archiveService: SagasArchiveService,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        val notificationsEnabled = settingsUseCase.getNotificationsEnabled()

        val smartSuggestionsEnabled = settingsUseCase.getSmartSuggestionsEnabled()

        val messageEffectsEnabled = settingsUseCase.getMessageEffectsEnabled()
        val showProgression = settingsUseCase.getShowProgression()
        val showTutorials = settingsUseCase.getShowTutorials()
        val musicEnabled = settingsUseCase.getMusicEnabled()

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
        val loadingProgress = MutableStateFlow<Float?>(null)
        val loadingMessage = MutableStateFlow<String?>(null)

        init {
            loadMemoryUsage()
            checkUserPro()
            loadStorageBreakdown()
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

        fun setShowProgression(enabled: Boolean) {
            viewModelScope.launch {
                settingsUseCase.setShowProgression(enabled)
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

        /** A picked `.sagas` file waiting for the user to confirm the restore. */
        data class PendingRestore(
            val uri: Uri,
            val manifest: ArchiveManifest,
        )

        val pendingRestore = MutableStateFlow<PendingRestore?>(null)

        /** The archive callbacks fire per chunk; only a whole-percent step is worth a recomposition. */
        private fun MutableStateFlow<Float?>.updatePercent(progress: Float) {
            val current = value ?: 0f
            if ((progress * 100).toInt() != (current * 100).toInt()) value = progress
        }

        fun exportArchive(destinationUri: Uri) {
            viewModelScope.launch {
                isLoading.value = true
                loadingMessage.emit(stringHelper.getString(R.string.backup_loading_creating))
                loadingProgress.value = 0f
                archiveService
                    .export(destinationUri) { loadingProgress.updatePercent(it) }
                    .onSuccessAsync {
                        loadingProgress.value = null
                        loadingMessage.emit(stringHelper.getString(R.string.archive_export_success))
                    }.onFailureAsync {
                        loadingProgress.value = null
                        loadingMessage.emit(stringHelper.getString(R.string.archive_export_failed))
                    }
                delay(3.seconds)
                isLoading.value = false
                loadingMessage.emit(null)
            }
        }

        fun previewArchive(sourceUri: Uri) {
            viewModelScope.launch {
                archiveService
                    .peek(sourceUri)
                    .onSuccessAsync { pendingRestore.value = PendingRestore(sourceUri, it) }
                    .onFailureAsync {
                        isLoading.value = true
                        loadingMessage.emit(stringHelper.getString(R.string.archive_invalid))
                        delay(3.seconds)
                        isLoading.value = false
                        loadingMessage.emit(null)
                    }
            }
        }

        fun dismissPendingRestore() {
            pendingRestore.value = null
        }

        fun restoreArchive() {
            val pending = pendingRestore.value ?: return
            pendingRestore.value = null
            viewModelScope.launch {
                isLoading.value = true
                loadingMessage.emit(stringHelper.getString(R.string.backup_loading_restoring_database))
                loadingProgress.value = 0f
                archiveService
                    .restore(pending.uri) { loadingProgress.updatePercent(it) }
                    .onSuccessAsync {
                        loadingProgress.value = null
                        loadingMessage.emit(stringHelper.getString(R.string.import_database_success))
                        delay(2.seconds)
                        context.restartApp()
                    }.onFailureAsync {
                        loadingProgress.value = null
                        loadingMessage.emit(stringHelper.getString(R.string.archive_restore_failed))
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

        /** Fires a sample [IslandContent] at the global shell so its layout can be validated
         * without needing to reach the real feature (chapter advance, image generation, etc.) live. */
        fun testIsland(content: IslandContent?) {
            chatIslandService.setTop(content)
        }

        private val _adTestLoading = MutableStateFlow<AdTier?>(null)
        val adTestLoading = _adTestLoading.asStateFlow()

        /** Debug-only: loads and shows an ad tier directly, bypassing the real milestone flow's
         * premium/remote-config/consent gating, to verify the AdMob unit IDs are wired correctly. */
        fun testAd(tier: AdTier) {
            viewModelScope.launch {
                _adTestLoading.value = tier
                adsService.debugShowTestAd(tier) {
                    _adTestLoading.value = null
                }
            }
        }
    }

data class SagaStorageInfo(
    val data: Saga,
    val sizeBytes: Long,
)
