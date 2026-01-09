package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.NewSagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.SagaFormFields
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
    ) : NewSagaUseCase {
        override suspend fun createSaga(saga: Saga): RequestResult<Saga> =
            executeRequest {
                sagaRepository.saveChat(saga)
            }

        override suspend fun updateSaga(saga: Saga): RequestResult<Saga> =
            executeRequest {
                sagaRepository.updateChat(saga)
            }

        override suspend fun generateSaga(
            sagaForm: SagaDraft,
            miniChatContent: List<ChatMessage>,
        ): RequestResult<Saga> =
            executeRequest {
                gemmaClient.generate<Saga>(
                    NewSagaPrompts.createSagaPrompt(sagaForm, miniChatContent),
                    filterOutputFields =
                        listOf(
                            "id",
                            "icon",
                            "createdAt",
                            "mainCharacterId",
                            "currentActId",
                            "isEnded",
                            "endedAt",
                            "isDebug",
                            "endMessage",
                            "review",
                            "emotionalReview",
                        ),
                )!!
            }

        override suspend fun generateSagaIcon(
            sagaForm: Saga,
            character: Character,
        ) = executeRequest {
            sagaRepository
                .generateSagaIcon(
                    sagaForm,
                    character,
                ).getSuccess()!!
        }

        override suspend fun replyAiForm(
            currentMessages: List<ChatMessage>,
            latestMessage: String?,
            currentFormData: SagaDraft,
        ): RequestResult<SagaCreationGen> =
            executeRequest {
                val delayDefaultTime = 700L

                // Transcribe audio if provided
                val userInput = currentMessages.last().text

                val extractedDataPrompt =
                    gemmaClient.generate<SagaForm>(
                        NewSagaPrompts.extractDataFromUserInputPrompt(
                            currentSagaForm = currentFormData,
                            userInput = userInput,
                            lastMessage = latestMessage!!,
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
                            field,
                            extractedDataPrompt,
                        ),
                    )!!
                nextQuestion.copy(callback = nextQuestion.callback?.copy(data = extractedDataPrompt))
            }

        override suspend fun generateIntroduction(): RequestResult<SagaCreationGen> =
            executeRequest {
                gemmaClient.generate(NewSagaPrompts.introPrompt())!!
            }

        override suspend fun generateCharacterIntroduction(sagaContext: SagaDraft?): RequestResult<SagaCreationGen> =
            executeRequest {
                gemmaClient.generate(NewSagaPrompts.characterIntroPrompt(sagaContext))!!
            }

        override suspend fun generateCharacterSavedMark(
            character: Character,
            saga: Saga,
        ): RequestResult<String> =
            executeRequest {
                gemmaClient.generate(NewSagaPrompts.characterSavedPrompt(character, saga))!!
            }

        override suspend fun generateProcessMessage(
            process: SagaProcess,
            sagaDescription: String,
            characterDescription: String,
        ): RequestResult<String> =
            executeRequest {
                gemmaClient.generate(
                    NewSagaPrompts.generateProcessPrompt(
                        process,
                        sagaDescription,
                        characterDescription,
                    ),
                )!!
            }
    }
