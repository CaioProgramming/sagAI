package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.features.chat.repository.SagaRepository
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SagaHistoryUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
    ) : SagaHistoryUseCase {
        override fun getSagas(): Flow<List<SagaData>> = sagaRepository.getChats()

        override suspend fun getSagaById(sagaId: Int): Flow<SagaData?> = sagaRepository.getSagaById(sagaId)
    }
