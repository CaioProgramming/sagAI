package com.ilustris.sagai.features.saga.detail.data.usecase

import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import javax.inject.Inject

class SagaDetailUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
    ) : SagaDetailUseCase {
        override suspend fun fetchSaga(sagaId: Int) = sagaRepository.getSagaById(sagaId)
    }
