package com.ilustris.sagai.features.playthrough

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.PlaythroughPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaythroughUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val textGenClient: GemmaClient,
    ) : PlaythroughUseCase {
        override fun availableSagas() =
            sagaRepository.getChats().map {
                it.filter { saga -> saga.data.playTimeMs > 0L || saga.data.isEnded }
            }

        override suspend fun invoke(): RequestResult<PlayThroughData> =
            executeRequest {
                val sagas = availableSagas().first()
                if (sagas.isEmpty()) error("No playthroughs available")

                val prompt = PlaythroughPrompts.extractPlaythroughReview(sagas)
                textGenClient.generate<PlayThroughData>(prompt)!!
            }
    }
