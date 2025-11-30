package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SagaHistoryUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val textGenClient: TextGenClient,
        private val gemmaClient: GemmaClient,
    ) : SagaHistoryUseCase {
        override suspend fun getSagaById(sagaId: Int): Flow<SagaContent?> = sagaRepository.getSagaById(sagaId)

        override suspend fun updateSaga(saga: Saga) = sagaRepository.updateChat(saga)

        override suspend fun createFakeSaga(): RequestResult<Saga> =
            executeRequest {
                sagaRepository
                    .saveChat(
                        Saga(
                            title = "Debug Saga",
                            description = "This saga was created to debug purposes only.",
                            genre = Genre.entries.random(),
                            isDebug = true,
                        ),
                    )
            }

        override suspend fun generateEndMessage(saga: SagaContent): RequestResult<String> =
            executeRequest {
                gemmaClient
                    .generate<String>(
                        SagaPrompts.endCredits(saga),
                    )!!
            }


    }
