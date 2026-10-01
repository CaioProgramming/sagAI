package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.GeneratedPlayerChoices
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.LibraryPitchesResponse
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.UniverseSuggestions
import kotlinx.coroutines.flow.Flow

data class SagaBook(
    val draft: SagaDraft = SagaDraft(),
    val characters: List<CharacterInfo> = emptyList(),
)

enum class SagaProcess {
    CREATING_SAGA,
    CREATING_CHARACTER,
    FINALIZING,
    SUCCESS,
    LISTENING,
    SAVED_CHARACTER,
}

sealed class SagaCreationState {
    data class Loading(
        val message: String,
    ) : SagaCreationState()

    /** The contract is written and the saga and protagonist are saved; the portrait is next. */
    data object ContractReady : SagaCreationState()

    /** The protagonist's portrait is done (or gave up), so the saga's cover is next. */
    data class CharacterReady(
        val character: Character,
    ) : SagaCreationState()

    data class Success(
        val saga: Saga,
        val character: Character,
    ) : SagaCreationState()

    data class Error(
        val error: Throwable,
    ) : SagaCreationState()
}

interface NewSagaUseCase {
    fun executePrompt(
        prompt: String,
        excludedGenres: List<Genre> = emptyList(),
    ): Flow<StreamingState<LibraryPitchesResponse?>>

    suspend fun provideInitialEchoes(): RequestResult<UniverseSuggestions>

    /** The opening quiz for [idea], tuned to the player's read from earlier sagas when there is one. */
    suspend fun generateQuiz(idea: String): RequestResult<GeneratedPlayerChoices>

    fun sealSacredContract(
        sagaDraft: SagaDraft,
        characterInfo: CharacterInfo,
    ): Flow<SagaCreationState>
}
