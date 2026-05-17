package com.ilustris.sagai.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun rememberGenreThemeServices(): GenreThemeServices {
    val context = LocalContext.current
    return remember(context) {
        val entryPoint =
            EntryPointAccessors.fromApplication(context, GenreThemeEntryPoint::class.java)
        GenreThemeServices(
            visualConfigService = entryPoint.genreVisualConfigService(),
            fontService = entryPoint.genreFontService(),
        )
    }
}
