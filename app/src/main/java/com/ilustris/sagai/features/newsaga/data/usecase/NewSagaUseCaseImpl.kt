package com.ilustris.sagai.features.newsaga.data.usecase

import com.google.firebase.ai.type.generationConfig
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.toJsonSchema
import com.ilustris.sagai.features.chat.repository.SagaRepository
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import javax.inject.Inject

class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val textGenClient: TextGenClient,
        private val sagaRepository: SagaRepository,
    ) : NewSagaUseCase {
        override suspend fun saveSaga(sagaData: SagaData): RequestResult<Exception, Long> =
            try {
                val chatId = sagaRepository.saveChat(sagaData)
                RequestResult.Success(chatId)
            } catch (e: Exception) {
                RequestResult.Error(e)
            }

        override suspend fun generateSaga(sagaForm: SagaForm): RequestResult<Exception, SagaData> =
            try {
                val schema = toJsonSchema(SagaData::class.java)
                val saga =
                    textGenClient.generate(
                        generateSagaPrompt(sagaForm),
                        true,
                        generationConfig =
                            generationConfig {
                                responseMimeType = "application/json"
                                responseSchema = schema
                            },
                    )
                val content = saga!!.text

                val contentData = Gson().fromJson<SagaData>(content, SagaData::class.java)

                RequestResult.Success(
                    contentData,
                )
            } catch (e: Exception) {
                e.printStackTrace()
                RequestResult.Error(e)
            }

        private fun generateSagaPrompt(sagaForm: SagaForm): String =
            """
            Generate a RPG Adventure based on the following details:
            Title: ${sagaForm.title}
            Description: ${sagaForm.description}
            Genre: ${sagaForm.genre.name}
            """.trimIndent()
    }
