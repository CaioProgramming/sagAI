package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.SagaForm

interface NewSagaUseCase {
    suspend fun saveSaga(
        saga: Saga,
        characterDescription: Character,
    ): RequestResult<Exception, Pair<Saga, Character>>

    suspend fun generateSaga(sagaForm: SagaForm): RequestResult<Exception, Saga>

    suspend fun generateSagaIcon(
        sagaForm: Saga,
        character: Character,
    ): RequestResult<Exception, Saga>
}
