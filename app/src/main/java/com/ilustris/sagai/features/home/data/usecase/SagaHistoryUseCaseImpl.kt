package com.ilustris.sagai.features.home.data.usecase

import androidx.compose.animation.core.copy
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.chat.repository.SagaRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SagaHistoryUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
    ) : SagaHistoryUseCase {
        override fun getSagas(): Flow<List<SagaContent>> =
            sagaRepository.getChats().map { content ->

                processSagaContent(content)
            }

        override suspend fun getSagaById(sagaId: Int): Flow<SagaContent?> =
            sagaRepository.getSagaById(sagaId)

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
    }
