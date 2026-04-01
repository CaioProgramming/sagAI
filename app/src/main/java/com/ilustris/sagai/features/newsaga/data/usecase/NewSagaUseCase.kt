package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.CreationAssist
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.ui.presentation.FlowPages

enum class SagaProcess {
    CREATING_SAGA,
    CREATING_CHARACTER,
    FINALIZING,
    SUCCESS,
    LISTENING,
    SAVED_CHARACTER,
}

interface NewSagaUseCase {
    suspend fun createSaga(saga: Saga): RequestResult<Saga>

    suspend fun updateSaga(saga: Saga): RequestResult<Saga>

    suspend fun deleteSaga(saga: Saga): RequestResult<Unit>

    suspend fun generateSaga(
        sagaForm: SagaDraft,
        miniChatContent: List<ChatMessage>,
    ): RequestResult<Saga>

    suspend fun generateSagaIcon(
        sagaForm: Saga,
        character: Character,
    ): RequestResult<Saga>

    suspend fun replyAiForm(
        currentMessages: List<ChatMessage>,
        latestMessage: String?,
        currentFormData: SagaForm,
    ): RequestResult<SagaCreationGen>

    suspend fun assistCreation(
        flow: FlowPages,
        sagaDraft: SagaDraft?,
        characterInfo: CharacterInfo?,
    ): RequestResult<CreationAssist>

    suspend fun generateIntroduction(): RequestResult<SagaCreationGen>

    suspend fun generateCharacterIntroduction(sagaContext: SagaDraft?): RequestResult<SagaCreationGen>

    suspend fun generateProcessMessage(
        process: SagaProcess,
        saga: SagaForm,
        character: CharacterInfo,
        genre: Genre? = null,
    ): RequestResult<String>

    suspend fun adaptSagaToGenre(sagaDraft: SagaDraft): RequestResult<SagaCreationGen>

    suspend fun generateGenreSuggestions(genre: Genre): RequestResult<SagaCreationGen>

    suspend fun refineDraft(
        rawInput: String,
        genre: Genre,
    ): RequestResult<SagaCreationGen>
}
