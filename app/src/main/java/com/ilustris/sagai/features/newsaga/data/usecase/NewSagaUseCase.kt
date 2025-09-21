package com.ilustris.sagai.features.newsaga.data.usecase

import SagaGen
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaForm

interface NewSagaUseCase {
    suspend fun saveSaga(
        saga: Saga,
        characterDescription: CharacterInfo?,
    ): RequestResult<Pair<Saga, Character>>

    suspend fun generateSaga(
        sagaForm: SagaForm,
        miniChatContent: List<ChatMessage>,
    ): RequestResult<SagaGen>

    suspend fun generateSagaIcon(
        sagaForm: Saga,
        character: Character,
    ): RequestResult<Saga>

    suspend fun replyAiForm(
        currentMessages: List<ChatMessage>,
        latestMessage: String,
        currentFormData: SagaForm,
    ): RequestResult<SagaCreationGen>

    suspend fun generateIntroduction(): RequestResult<SagaCreationGen>

    suspend fun generateCharacterSavedMark(
        character: Character,
        saga: Saga,
    ): RequestResult<String>
}
