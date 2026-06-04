package com.ilustris.sagai.features.newsaga.ui.presentation

import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.UniverseEcho
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook

data class NewSagaUiState(
    val statusMessage: String? = null,
    val universeEchoes: List<Pair<UniverseEcho, GenreVisualConfig>> = emptyList(),
    val libraryBooks: List<Pair<SagaBook, GenreVisualConfig>> = emptyList(),
    val lockedSaga: SagaDraft? = null,
    val lockedCharacter: CharacterInfo? = null,
    val isGenerating: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSaving: Boolean = false,
    val isReadyToSave: Boolean = false,
    val error: String? = null,
    val savingError: String? = null,
    val currentVisualConfig: GenreVisualConfig? = null,
    val genresVisuals: List<Pair<Genre, GenreVisualConfig?>>? = null,
) {
    val hasMoreGenres: Boolean
        get() = libraryBooks.map { it.first.draft.genre }.toSet().size < Genre.entries.size
}
