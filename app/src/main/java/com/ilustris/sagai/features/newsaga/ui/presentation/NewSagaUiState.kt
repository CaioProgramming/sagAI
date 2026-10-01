package com.ilustris.sagai.features.newsaga.ui.presentation

import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.chapter.data.model.GeneratedPlayerChoices
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.UniverseEcho
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook

data class NewSagaUiState(
    val phase: NewSagaScreenPhase = NewSagaScreenPhase.Suggestions,
    val statusMessage: String? = null,
    val universeEchoes: List<Pair<UniverseEcho, GenreVisualConfig>> = emptyList(),
    val libraryBooks: List<Pair<SagaBook, GenreVisualConfig>> = emptyList(),
    val lockedSaga: SagaDraft? = null,
    val lockedCharacter: CharacterInfo? = null,
    val isGenerating: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSaving: Boolean = false,
    val isLoadingQuiz: Boolean = false,
    val quiz: GeneratedPlayerChoices? = null,
    /** The option index picked for each quiz card, null until answered. */
    val quizAnswers: List<Int?> = emptyList(),
    val quizIndex: Int = 0,
    val focusedIndex: Int = 0,
    val isReadyToSave: Boolean = false,
    val error: String? = null,
    val savingError: String? = null,
    val currentVisualConfig: GenreVisualConfig? = null,
    val genresVisuals: List<Pair<Genre, GenreVisualConfig?>>? = null,
) {
    val currentQuizAnswer: Int?
        get() = quizAnswers.getOrNull(quizIndex)

    /** The genre of the story centered in the pager, until one is chosen. */
    val focusedGenre: Genre?
        get() =
            if (phase == NewSagaScreenPhase.Creation && lockedSaga == null) {
                libraryBooks.getOrNull(focusedIndex)?.first?.draft?.genre
            } else {
                null
            }

    val hasMoreGenres: Boolean
        get() = libraryBooks.map { it.first.draft.genre }.toSet().size < Genre.entries.size
}
