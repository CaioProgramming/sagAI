package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.LorePrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.timeline.data.model.LoreGen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SagaHistoryUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val textGenClient: TextGenClient,
    ) : SagaHistoryUseCase {
        override fun getSagas(): Flow<List<SagaContent>> =
            sagaRepository.getChats().map { content ->

                processSagaContent(content)
            }

        override suspend fun getSagaById(sagaId: Int): Flow<SagaContent?> = sagaRepository.getSagaById(sagaId)

        override suspend fun updateSaga(saga: SagaData) = sagaRepository.updateChat(saga)

        override suspend fun generateLore(
            saga: SagaContent,
            loreReference: Int,
            lastMessages: List<String>,
        ): RequestResult<Exception, LoreGen> =
            try {
                textGenClient
                    .generate<LoreGen>(
                        LorePrompts.loreGeneration(
                            saga,
                            lastMessages.map { it.toJsonFormat() },
                        ),
                        customSchema = LoreGen.toSchema(),
                    )!!
                    .asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun createFakeSaga(): RequestResult<Exception, SagaData> =
            try {
                sagaRepository
                    .saveChat(
                        SagaData(
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
            try {
                textGenClient
                    .generate<String>(
                        SagaPrompts.endCredits(saga),
                    )!!
                    .asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
    }

private fun processSagaContent(content: List<SagaContent>): List<SagaContent> {
    val mappedSagas =
        content.map { saga ->
            saga.copy(
                messages = saga.messages.sortedByDescending { it.message.timestamp },
            )
        }

    mappedSagas.sortedByDescending { saga ->
        saga.messages
            .firstOrNull()
            ?.message
            ?.timestamp ?: 0L
    }

    return mappedSagas
}
