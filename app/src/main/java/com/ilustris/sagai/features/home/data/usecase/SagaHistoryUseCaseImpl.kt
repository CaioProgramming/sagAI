package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.ai.SagaPrompts
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Lore
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
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

        override suspend fun generateLore(
            saga: SagaData?,
            character: Character?,
            loreReference: Int,
            lastMessages: List<String>,
        ): RequestResult<Exception, String> =
            try {
                val newLore =
                    textGenClient.generate<Lore>(
                        SagaPrompts.loreGeneration(
                            saga!!,
                            lastMessages,
                            character!!,
                        ),
                    )
                sagaRepository.updateChat(saga.copy(lore = newLore!!.story, lastLoreReference = loreReference))
                newLore.story.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
    }

private fun processSagaContent(content: List<SagaContent>): List<SagaContent> {
    val mappedSagas =
        content.map { saga ->
            saga.copy(
                messages = saga.messages.sortedByDescending { it.timestamp },
            )
        }

    mappedSagas.sortedByDescending { saga ->
        saga.messages.firstOrNull()?.timestamp ?: 0L
    }

    return mappedSagas
}
