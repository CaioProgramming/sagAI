package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm

interface NewSagaUseCase {
    suspend fun saveSaga(sagaData: SagaData): RequestResult<Exception, Long>

    suspend fun generateSaga(sagaForm: SagaForm): RequestResult<Exception, SagaData>

    suspend fun generateSagaIcon(sagaForm: SagaForm): RequestResult<Exception, ByteArray>
}
