package com.ilustris.sagai.features.home.data.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.HomePrompts // Added import
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
    ) : HomeUseCase {
        override fun getSagas(): Flow<List<SagaContent>> =
            sagaRepository.getChats().map { content ->
                processSagaContent(content)
            }

        override suspend fun fetchDynamicNewSagaTexts(): RequestResult<Exception, DynamicSagaPrompt> =
            try {
                Log.d("HomeUseCaseImpl", "Fetching new dynamic saga texts...")
                val prompt = HomePrompts.dynamicSagaCreationPrompt()

                val result = gemmaClient.generate<DynamicSagaPrompt>(prompt, requireTranslation = true)
                result!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun createFakeSaga(): RequestResult<Exception, Saga> =
            try {
                sagaRepository
                    .saveChat(
                        Saga(
                            title = "Debug Saga",
                            description = "This saga was created for debug purposes only.",
                            genre = Genre.entries.random(),
                            isDebug = true,
                        ),
                    ).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private fun processSagaContent(content: List<SagaContent>): List<SagaContent> {

            return content.sortedByDescending { saga ->
                saga.flatMessages()
                    .firstOrNull()
                    ?.message
                    ?.timestamp ?: 0L
            }
        }
    }
