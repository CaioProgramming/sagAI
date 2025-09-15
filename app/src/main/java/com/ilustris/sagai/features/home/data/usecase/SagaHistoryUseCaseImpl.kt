package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.LorePrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

        override suspend fun generateLore(
            saga: SagaContent,
            currentTimeline: TimelineContent,
        ): RequestResult<Exception, Timeline> =
            executeRequest {
                gemmaClient
                    .generate<Timeline>(
                        LorePrompts.loreGeneration(
                            saga,
                            currentTimeline,
                        ),
                        skipRunning = true,
                    )!!
            }

        override suspend fun createFakeSaga(): RequestResult<Exception, Saga> =
            try {
                sagaRepository
                    .saveChat(
                        Saga(
                            title = "Debug Saga",
                            description = "This saga was created to debug purposes only.",
                            genre = Genre.entries.random(),
                            isDebug = true,
                        ),
                    ).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateEndMessage(saga: SagaContent): RequestResult<Exception, String> =
            executeRequest {
                textGenClient
                    .generate<String>(
                        SagaPrompts.endCredits(saga),
                    )!!
            }
    }
