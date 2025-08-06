package com.ilustris.sagai.features.newsaga.data.usecase

import android.util.Log
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.ai.prompts.NewSagaPrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.CharacterInfo
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val textGenClient: TextGenClient,
        private val imageGenClient: ImagenClient,
        private val sagaRepository: SagaRepository,
        private val characterUseCase: CharacterUseCase,
        private val gemmaClient: GemmaClient,
        private val fileHelper: FileHelper,
    ) : NewSagaUseCase {
        override suspend fun saveSaga(
            sagaData: Saga,
            characterDescription: CharacterInfo?,
        ): RequestResult<Exception, Pair<Saga, Character>> =
            try {
                val saga =
                    sagaRepository.saveChat(
                        sagaData.copy(
                            id = 0,
                            mainCharacterId = null,
                        ),
                    )

                val genCharacter =
                    characterUseCase
                        .generateCharacter(
                            SagaContent(data = saga),
                            characterDescription.toJsonFormat(),
                        ).success.value

                val updatedSaga =
                    sagaRepository.updateChat(
                        sagaData.copy(
                            id = saga.id,
                            mainCharacterId = genCharacter.id,
                        ),
                    )
                Log.i(javaClass.simpleName, "saveSaga: Saga operation complete -> $updatedSaga")
                (updatedSaga to genCharacter).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateSaga(sagaForm: SagaForm): RequestResult<Exception, Saga> =
            try {
                val saga =
                    textGenClient.generate<Saga>(
                        generateSagaPrompt(sagaForm),
                        true,
                    )
                saga!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateSagaIcon(
            sagaForm: Saga,
            character: Character,
        ): RequestResult<Exception, Saga> =
            try {
                val metaPromptCover =
                    gemmaClient.generate<String>(
                        SagaPrompts.iconDescription(
                            sagaForm,
                            character,
                        ),
                        requireTranslation = false,
                    )
                val sagaIconPrompt =
                    generateSagaIconPrompt(
                        saga = sagaForm,
                        mainCharacter = character,
                        description = metaPromptCover!!,
                    )

                withContext(Dispatchers.IO) {
                    characterUseCase.generateCharacterImage(
                        character = character,
                        saga = sagaForm,
                    )
                }

                val file =
                    fileHelper.saveFile(
                        fileName = sagaForm.title,
                        data = imageGenClient.generateImage(sagaIconPrompt)!!,
                        path = "${sagaForm.id}",
                    )

                sagaRepository
                    .updateChat(sagaForm.copy(icon = file!!.absolutePath))
                    .asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun replyAiForm(
            currentMessages: List<ChatMessage>,
            currentFormData: SagaForm,
        ): RequestResult<Exception, SagaCreationGen> =
            try {
                val prompt =
                    NewSagaPrompts.formReplyPrompt(
                        currentFormData,
                        currentMessages.map {
                            if (it.isUser) {
                                "USER" to it.text
                            } else {
                                "AI" to it.text
                            }
                        },
                    )

                val aiRequest =
                    gemmaClient.generate<SagaCreationGen>(
                        prompt,
                        requireTranslation = true,
                    )

                aiRequest!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateIntroduction(): RequestResult<Exception, SagaCreationGen> =
            try {
                val prompt = NewSagaPrompts.formIntroductionPrompt()

                val aiRequest =
                    gemmaClient.generate<SagaCreationGen>(
                        prompt,
                        requireTranslation = true,
                    )

                aiRequest!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateCharacterSavedMark(
            character: Character,
            saga: Saga,
        ): RequestResult<Exception, String> =
            try {
                val prompt =
                    NewSagaPrompts.characterCreatedPrompt(
                        character,
                        saga,
                    )

                val aiRequest =
                    gemmaClient.generate<String>(
                        prompt,
                        requireTranslation = true,
                    )

                aiRequest!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private fun generateSagaIconPrompt(
            saga: Saga,
            mainCharacter: Character,
            description: String,
        ) = ImagePrompts.wallpaperGeneration(
            saga,
            description,
        )

        private fun generateSagaPrompt(sagaForm: SagaForm): String =
            SagaPrompts.sagaGeneration(
                sagaForm,
            )
    }
