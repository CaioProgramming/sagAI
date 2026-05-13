package com.ilustris.sagai.core.theme

import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized singleton that manages the global theme state.
 * ViewModels call [updateTheme] / [resetTheme] to drive
 * genre-specific color, typography, and shape changes
 * observed at the root [SagAITheme] in MainActivity.
 */
@Singleton
class SagaThemeManager @Inject constructor() {
    private val _currentGenre = MutableStateFlow<Genre?>(null)
    val currentGenre: StateFlow<Genre?> = _currentGenre.asStateFlow()

    /** Set the active genre. The root theme will animate to its colors. */
    fun updateTheme(genre: Genre?) {
        _currentGenre.value = genre
    }

    /** Clear the active genre, reverting to the default brand identity. */
    fun resetTheme() {
        _currentGenre.value = null
    }
}
