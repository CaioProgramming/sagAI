package com.ilustris.sagai.features.newsaga.data.usecase

import SagaGen
import android.util.Log
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.models.ImageReference
import com.ilustris.sagai.core.ai.prompts.ImageGuidelines
import com.ilustris.sagai.core.ai.prompts.NewSagaPrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.GenreReferenceHelper
import com.ilustris.sagai.core.utils.ImageCropHelper
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.CallbackContent
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.SagaFormFields
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val textGenClient: TextGenClient,
        private val sagaRepository: SagaRepository,
        private val characterUseCase: CharacterUseCase,
        private val gemmaClient: GemmaClient,
    ) : NewSagaUseCase {
        override suspend fun saveSaga(
            sagaData: Saga,
            characterDescription: CharacterInfo?,
        ): RequestResult<Pair<Saga, Character>> =
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

        override suspend fun generateSaga(
            sagaForm: SagaForm,
            miniChatContent: List<ChatMessage>,
        ): RequestResult<SagaGen> =
            try {
                val saga =
                    textGenClient.generate<SagaGen>(
                        generateSagaPrompt(sagaForm, miniChatContent),
                        true,
                    )
                saga!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateSagaIcon(
            sagaForm: Saga,
            character: Character,
        ) = executeRequest {
            val characterOperation =
                characterUseCase.generateCharacterImage(
                    character = character,
                    saga = sagaForm,
                )

            sagaRepository
                .generateSagaIcon(
                    sagaForm,
                    characterOperation.getSuccess()!!.first,
                ).getSuccess()!!
        }

        override suspend fun replyAiForm(
            currentMessages: List<ChatMessage>,
            currentFormData: SagaForm,
        ): RequestResult<SagaCreationGen> =
            executeRequest {
                val delayDefaultTime = 700L

                val extractedDataPrompt =
                    gemmaClient.generate<SagaForm>(
                        NewSagaPrompts.extractDataFromUserInputPrompt(
                            currentFormData,
                            currentMessages.last().text,
                        ),
                        requireTranslation = true,
                    )!!

                delay(delayDefaultTime)

                val identifyNextFieldPrompt =
                    gemmaClient
                        .generate<String>(
                            NewSagaPrompts.identifyNextFieldPrompt(extractedDataPrompt),
                            requireTranslation = false,
                        )!!
                        .replace("\n", "")

                val field = SagaFormFields.getByKey(identifyNextFieldPrompt)!!

                delay(delayDefaultTime)

                val nextQuestion =
                    gemmaClient.generate<SagaCreationGen>(
                        NewSagaPrompts.generateCreativeQuestionPrompt(
                            field!!,
                            extractedDataPrompt,
                        ),
                    )!!

                val callBackAction =
                    if (field == SagaFormFields.ALL_FIELDS_COMPLETE) {
                        CallBackAction.SAVE_SAGA
                    } else {
                        CallBackAction.UPDATE_DATA
                    }

                nextQuestion
                    .copy(
                        callback =
                            CallbackContent(
                                action = callBackAction,
                                data = extractedDataPrompt,
                            ),
                    )
            }

        override suspend fun generateIntroduction(): RequestResult<SagaCreationGen> =
            executeRequest {
                val prompt = NewSagaPrompts.formIntroductionPrompt()

                val aiRequest =
                    gemmaClient.generate<SagaCreationGen>(
                        prompt,
                        requireTranslation = true,
                    )

                aiRequest!!
            }

        override suspend fun generateCharacterSavedMark(
            character: Character,
            saga: Saga,
        ): RequestResult<String> =
            executeRequest {
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

                aiRequest!!
            }

        private fun generateSagaPrompt(
            sagaForm: SagaForm,
            miniChatContent: List<ChatMessage>,
        ): String =
            SagaPrompts.sagaGeneration(
                sagaForm,
                miniChatContent,
            )
    }
