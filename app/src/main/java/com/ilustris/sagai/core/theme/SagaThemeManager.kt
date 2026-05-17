package com.ilustris.sagai.core.theme

import android.net.Uri
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.file.FileCacheService
import com.ilustris.sagai.core.media.SoundFxService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.vibrationPattern
import com.ilustris.sagai.ui.components.SagaSnackBarMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized singleton that manages the global immersive saga experience.
 * Controls immersive session state: active genre, ambient music, SFX, and VFX.
 * Visual config and fonts are loaded by [com.ilustris.sagai.ui.theme.SagAITheme].
 */
@Singleton
class SagaThemeManager
    @Inject
    constructor(
        private val visualConfigService: GenreVisualConfigService,
        private val soundFxService: SoundFxService,
        private val genreConfigService: GenreConfigService,
        private val fileCacheService: FileCacheService,
        private val remoteConfig: RemoteConfigService,
    ) {
        private val _currentGenre = MutableStateFlow<Genre?>(null)
        val currentGenre: StateFlow<Genre?> = _currentGenre.asStateFlow()

        private val _ambientMusicFile = MutableStateFlow<File?>(null)
        val ambientMusicFile: StateFlow<File?> = _ambientMusicFile.asStateFlow()

        private val _vfxTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val vfxTrigger: SharedFlow<Unit> = _vfxTrigger.asSharedFlow()

        private val _snackBarMessage = MutableStateFlow<SagaSnackBarMessage?>(null)
        val snackBarMessage: StateFlow<SagaSnackBarMessage?> = _snackBarMessage.asStateFlow()

        private var snackBarDismissJob: Job? = null

        private val managerJob = Job()
        private val managerScope = CoroutineScope(Dispatchers.Main + managerJob)

        private var isNeutralScreen: Boolean = true

        /**
         * Updates the neutrality state of the manager.
         * When neutral, any attempt to set a genre is ignored, and the theme is reset.
         */
        fun setNeutral(isNeutral: Boolean) {
            this.isNeutralScreen = isNeutral
            if (isNeutral) {
                resetTheme()
            }
        }

        /** Set the active genre. The root theme will animate to its colors and fetch configurations. */
        fun updateTheme(genre: Genre?) {
            if (isNeutralScreen && genre != null) {
                Timber.w("Theme update ignored: currently on a neutral screen (Home, Profile, etc.)")
                return
            }
            if (_currentGenre.value == genre && genre != null) return

            _currentGenre.value = genre
            if (genre != null) {
                managerScope.launch {
                    fetchConfigs(genre)
                }
            } else {
                resetTheme()
            }
        }

        private suspend fun fetchConfigs(genre: Genre) {
            try {
                getAmbienceMusic(genre)
                getReplySfx(genre)
            } catch (e: Exception) {
                Timber.e(e, "SagaThemeManager: Error fetching media configs for $genre")
            }
        }

        private suspend fun getAmbienceMusic(genre: Genre) {
            val fileUrl = genreConfigService.getGenreConfig(genre).ambientMusicUrl
            if (fileUrl.isEmpty()) return

            withContext(Dispatchers.IO) {
                val extension = Uri.parse(fileUrl).path?.substringAfterLast(".", "mp3") ?: "mp3"
                val newMusicFile = fileCacheService.getFile(fileUrl, extension)
                if (newMusicFile?.absolutePath != _ambientMusicFile.value?.absolutePath) {
                    _ambientMusicFile.value = newMusicFile
                    Timber.d("SagaThemeManager: Ambient music updated for $genre")
                }
            }
        }

        private suspend fun getReplySfx(genre: Genre) {
            withContext(Dispatchers.IO) {
                val sfxMap = remoteConfig.getJson<Map<String, String>>("reply_sfx_config")
                val finalUrl = sfxMap?.get(genre.name) ?: sfxMap?.get("DEFAULT")
                if (finalUrl.isNullOrEmpty()) return@withContext

                val extension = Uri.parse(finalUrl).path?.substringAfterLast(".", "mp3") ?: "mp3"
                val sfxFile = fileCacheService.getFile(finalUrl, extension)
                sfxFile?.let {
                    soundFxService.prepare(it)
                    Timber.d("SagaThemeManager: Reply SFX prepared for $genre")
                }
            }
        }

        /** Triggers the current genre's VFX (sound + vibration pattern from visual config). */
        fun playVfx() {
            val genre = _currentGenre.value ?: return
            managerScope.launch {
                delay(300)
                val visual = visualConfigService.getVisualConfig(genre)
                val pattern = genre.vibrationPattern(visual)
                soundFxService.playWithHaptics(pattern)
                _vfxTrigger.emit(Unit)
            }
        }

        /** Clear the active genre, reverting to the default brand identity. */
        fun resetTheme() {
            _currentGenre.value = null
            _ambientMusicFile.value = null
        }

        fun showSnackBar(
            message: String,
            action: Pair<String, () -> Unit>? = null,
            durationMs: Long = 10_000L,
        ) {
            snackBarDismissJob?.cancel()
            _snackBarMessage.value = SagaSnackBarMessage(message = message, action = action)
            if (durationMs > 0) {
                snackBarDismissJob =
                    managerScope.launch {
                        delay(durationMs)
                        dismissSnackBar()
                    }
            }
        }

        fun dismissSnackBar() {
            snackBarDismissJob?.cancel()
        snackBarDismissJob = null
        _snackBarMessage.value = null
    }
}
