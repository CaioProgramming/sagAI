package com.ilustris.sagai.features.newsaga.data.usecase

import androidx.compose.ui.text.capitalize
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.generationConfig
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.iconPrompt
import com.ilustris.sagai.core.ai.sagaPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.toJsonSchema
import com.ilustris.sagai.features.chat.repository.SagaRepository
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import java.util.Locale
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val textGenClient: TextGenClient,
        private val imageGenClient: ImagenClient,
        private val sagaRepository: SagaRepository,
    ) : NewSagaUseCase {
        override suspend fun saveSaga(sagaData: SagaData): RequestResult<Exception, Long> =
            try {
                val chatId =
                    sagaRepository.saveChat(
                        sagaData.copy(
                            id = 0,
                            createdAt = System.currentTimeMillis(),
                        ),
                    )
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

        override suspend fun generateSagaIcon(sagaForm: SagaForm): RequestResult<Exception, ByteArray> {
            return try {
                val prompt = generateSagaIconPrompt(sagaForm)
                val request = imageGenClient.generateImage(prompt)
                val image = request!!.data

                return RequestResult.Success(image)
            } catch (e: Exception) {
                e.printStackTrace()
                return RequestResult.Error(e)
            }
        }

        private fun generateSagaIconPrompt(form: SagaForm) = form.genre.iconPrompt(form.description)
    }

private fun generateSagaPrompt(sagaForm: SagaForm): String =
    sagaPrompt(
        sagaForm.title,
        sagaForm.description,
        sagaForm.genre.name.capitalize(Locale.getDefault()),
    )
