package com.ilustris.sagai.features.newsaga.ui.presentation

import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.chapter.data.model.GeneratedPlayerChoices
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.UniverseEcho
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook

/** Where the save is, which decides which of the two cards is being worked on. */
enum class SaveStage { Contract, Character, Saga, Done }

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
    val saveStage: SaveStage = SaveStage.Contract,
    /** The protagonist and saga as saved so far: they carry the portrait and cover once made. */
    val savedCharacter: Character? = null,
    val savedSaga: Saga? = null,
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
    /** The save animation runs while saving and holds on the finished cards until the chat opens. */
    val showSaveProgress: Boolean
        get() = isSaving || saveStage == SaveStage.Done

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
