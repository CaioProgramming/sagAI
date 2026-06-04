package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.data.RequestResult
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
    ): Flow<StreamingState<LibraryPitchesResponse>>

    suspend fun provideInitialEchoes(): RequestResult<UniverseSuggestions>

    fun sealSacredContract(
        sagaDraft: SagaDraft,
        characterInfo: CharacterInfo,
    ): Flow<SagaCreationState>
}
