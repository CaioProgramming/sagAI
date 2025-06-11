package com.ilustris.sagai.features.newsaga.data.usecase

import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.SagaPrompts
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.characterPrompt
import com.ilustris.sagai.core.ai.iconPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val textGenClient: TextGenClient,
        private val imageGenClient: ImagenClient,
        private val sagaRepository: SagaRepository,
        private val characterRepository: CharacterRepository,
    ) : NewSagaUseCase {
        override suspend fun saveSaga(sagaData: SagaData): RequestResult<Exception, Long> =
            try {
                val chatId =
                    sagaRepository.saveChat(
                        sagaData.copy(
                            id = 0,
                            createdAt = System.currentTimeMillis(),
                            mainCharacterId = null,
                        ),
                    )

                generateCharacter(sagaData)
                    ?.copy(
                        image = emptyString(),
                        sagaId = chatId.toInt(),
                    )?.let {
                        characterRepository.insertCharacter(it).also { character ->
                            sagaRepository.updateChat(
                                sagaData.copy(
                                    id = chatId.toInt(),
                                    mainCharacterId = character.id,
                                ),
                            )
                        }
                    }
                RequestResult.Success(chatId)
            } catch (e: Exception) {
                RequestResult.Error(e)
            }

        private suspend fun generateCharacter(sagaData: SagaData): Character? =
            textGenClient.generate<Character>(
                sagaData.characterPrompt(),
            )

        override suspend fun generateSaga(sagaForm: SagaForm): RequestResult<Exception, SagaData> =
            try {
                val saga =
                    textGenClient.generate<SagaData>(
                        generateSagaPrompt(sagaForm),
                        true,
                    )
                RequestResult.Success(
                    saga!!,
                )
            } catch (e: Exception) {
                e.printStackTrace()
                RequestResult.Error(e)
            }

        override suspend fun generateSagaIcon(sagaForm: SagaForm): RequestResult<Exception, ByteArray> =
            try {
                val prompt = generateSagaIconPrompt(sagaForm)
                val request = imageGenClient.generateImage(prompt)
                val image = request!!.data

                RequestResult.Success(image)
            } catch (e: Exception) {
                e.printStackTrace()
                RequestResult.Error(e)
            }

        private fun generateSagaIconPrompt(form: SagaForm) = form.genre.iconPrompt(form.description)
    }

private fun generateSagaPrompt(sagaForm: SagaForm): String =
    SagaPrompts.sagaGeneration(
        sagaForm,
    )
