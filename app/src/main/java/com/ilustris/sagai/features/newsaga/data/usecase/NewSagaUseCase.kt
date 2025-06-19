package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import java.io.File

interface NewSagaUseCase {
    suspend fun saveSaga(
        sagaData: SagaData,
        characterDescription: String,
    ): RequestResult<Exception, Pair<SagaData, Character>>

    suspend fun generateSaga(sagaForm: SagaForm): RequestResult<Exception, SagaData>

    suspend fun generateSagaIcon(
        sagaForm: SagaData,
        character: Character,
    ): RequestResult<Exception, SagaData>

}
