package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm

enum class SagaProcess {
    CREATING_SAGA,
    CREATING_CHARACTER,
    FINALIZING,
    SUCCESS,
    LISTENING,
}

interface NewSagaUseCase {
    suspend fun createSaga(saga: Saga): RequestResult<Saga>

    suspend fun updateSaga(saga: Saga): RequestResult<Saga>

    suspend fun generateSaga(
        sagaForm: SagaForm,
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

    suspend fun generateIntroduction(): RequestResult<SagaCreationGen>

    suspend fun generateCharacterIntroduction(sagaContext: SagaDraft?): RequestResult<SagaCreationGen>

    suspend fun generateCharacterSavedMark(
        character: Character,
        saga: Saga,
    ): RequestResult<String>

    suspend fun generateProcessMessage(
        process: SagaProcess,
        sagaDescription: String,
        characterDescription: String,
    ): RequestResult<String>
}
