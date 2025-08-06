package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.CharacterInfo

interface NewSagaUseCase {
    suspend fun saveSaga(
        saga: Saga,
        characterDescription: CharacterInfo?,
    ): RequestResult<Exception, Pair<Saga, Character>>

    suspend fun generateSaga(sagaForm: SagaForm): RequestResult<Exception, Saga>

    suspend fun generateSagaIcon(
        sagaForm: Saga,
        character: Character,
    ): RequestResult<Exception, Saga>

    suspend fun replyAiForm(
        currentMessages: List<ChatMessage>,
        currentFormData: SagaForm,
    ): RequestResult<Exception, SagaCreationGen>

    suspend fun generateIntroduction(): RequestResult<Exception, SagaCreationGen>

    suspend fun generateCharacterSavedMark(
        character: Character,
        saga: Saga,
    ): RequestResult<Exception, String>
}
