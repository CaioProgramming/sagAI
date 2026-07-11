package com.ilustris.sagai.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

data class GenreThemeServices(
    val visualConfigService: GenreVisualConfigService,
    val fontService: GenreFontService,
)

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GenreThemeEntryPoint {
    fun genreVisualConfigService(): GenreVisualConfigService

    fun genreFontService(): GenreFontService
}

/**
 * Returns null in Compose Preview (`LocalInspectionMode`) — [EntryPointAccessors.fromApplication]
 * requires the real Hilt-generated `Application`, which layoutlib's preview context isn't.
 * Callers must treat a null result as "no remote genre theming available" and fall back to
 * plain defaults, exactly as they already do while a real config/font fetch is in flight.
 */
@Composable
fun rememberGenreThemeServices(): GenreThemeServices? {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    return remember(context, isPreview) {
        if (isPreview) {
            null
        } else {
            val entryPoint =
                EntryPointAccessors.fromApplication(context, GenreThemeEntryPoint::class.java)
            GenreThemeServices(
                visualConfigService = entryPoint.genreVisualConfigService(),
                fontService = entryPoint.genreFontService(),
            )
        }
    }
}
