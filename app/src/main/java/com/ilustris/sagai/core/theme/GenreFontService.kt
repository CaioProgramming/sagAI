package com.ilustris.sagai.core.theme

import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.file.FileCacheService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenreFontService
    @Inject
    constructor(
        private val fileCacheService: FileCacheService,
    ) {
        private val fontStates = ConcurrentHashMap<Genre, MutableStateFlow<ResolvedGenreFonts?>>()
        private val loadMutex = ConcurrentHashMap<Genre, Mutex>()
        private val downloadSemaphore = Semaphore(MAX_CONCURRENT_DOWNLOADS)

        fun fontsFor(genre: Genre): StateFlow<ResolvedGenreFonts?> = fontStates.getOrPut(genre) { MutableStateFlow(null) }.asStateFlow()

        suspend fun ensureLoaded(
            genre: Genre,
            config: GenreVisualConfig?,
        ) {
            if (config == null) return
            if (fontsFor(genre).value != null) return

            val mutex = loadMutex.getOrPut(genre) { Mutex() }
            mutex.withLock {
                if (fontsFor(genre).value != null) return

                val headerUrl = config.headerFontUrl.trim()
                val bodyUrl = config.bodyFontUrl.trim()
                if (headerUrl.isEmpty() && bodyUrl.isEmpty()) return

                val headerFamily =
                    if (headerUrl.isNotEmpty()) {
                        loadFontFamily(headerUrl, genre, "header")
                    } else {
                        null
                    }
                val bodyFamily =
                    if (bodyUrl.isNotEmpty()) {
                        loadFontFamily(bodyUrl, genre, "body")
                    } else {
                        null
                    }

                if (headerFamily == null && bodyFamily == null) return

                val resolved =
                    ResolvedGenreFonts(
                        header = headerFamily ?: bodyFamily ?: FontFamily.Default,
                        body = bodyFamily ?: headerFamily ?: FontFamily.Default,
                    )
                fontStates.getOrPut(genre) { MutableStateFlow(null) }.value = resolved
                Timber.d("GenreFontService: fonts ready for $genre")
            }
        }

        suspend fun getTypefaces(
            genre: Genre,
            config: GenreVisualConfig?,
        ): Pair<Typeface, Typeface> =
            withContext(Dispatchers.IO) {
                ensureLoaded(genre, config)
                val headerFile =
                    config?.headerFontUrl?.trim()?.takeIf { it.isNotEmpty() }?.let { url ->
                        fileCacheService.getCachedFile(url, extensionForUrl(url))
                    }
                val bodyFile =
                    config?.bodyFontUrl?.trim()?.takeIf { it.isNotEmpty() }?.let { url ->
                        fileCacheService.getCachedFile(url, extensionForUrl(url))
                    }
                val headerTypeface = headerFile?.let { typefaceFromFile(it) } ?: Typeface.DEFAULT
                val bodyTypeface = bodyFile?.let { typefaceFromFile(it) } ?: headerTypeface
                headerTypeface to bodyTypeface
            }

        private suspend fun loadFontFamily(
            url: String,
            genre: Genre,
            role: String,
        ): FontFamily? =
            withContext(Dispatchers.IO) {
                downloadSemaphore.withPermit {
                    try {
                        val extension = extensionForUrl(url)
                        var file =
                            fileCacheService.getCachedFile(url, extension)
                                ?: fileCacheService.getFile(url, extension)
                        if (file == null) return@withContext null
                        if (!isValidFontFile(file)) {
                            Timber.w(
                                "GenreFontService: invalid font file for $genre $role (${file.length()} bytes), retrying download",
                            )
                            fileCacheService.invalidateCachedFile(url, extension)
                            file = fileCacheService.getFile(url, extension)
                            if (file == null || !isValidFontFile(file)) {
                                file?.let { fileCacheService.invalidateCachedFile(url, extension) }
                                return@withContext null
                            }
                        }
                        FontFamily(Font(file, weight = FontWeight.Normal, style = FontStyle.Normal))
                    } catch (e: Exception) {
                        Timber.e(e, "GenreFontService: failed to load $role font for $genre")
                        null
                    }
                }
            }

        private fun extensionForUrl(url: String): String {
            val pathExt =
                Uri
                    .parse(url)
                    .path
                    ?.substringAfterLast('.', "")
                    ?.lowercase()
            return when (pathExt) {
                "otf", "ttf" -> pathExt
                else -> "ttf"
            }
        }

        private fun isValidFontFile(file: File): Boolean {
            if (!file.exists() || file.length() < 4) return false
            return typefaceFromFile(file) != null
        }

        private fun typefaceFromFile(file: File): Typeface? =
            try {
                Typeface.Builder(file).build()
            } catch (_: Exception) {
                null
            }

        companion object {
            private const val MAX_CONCURRENT_DOWNLOADS = 3
        }
    }
