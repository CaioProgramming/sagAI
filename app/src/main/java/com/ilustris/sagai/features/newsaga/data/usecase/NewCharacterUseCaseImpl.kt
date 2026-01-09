package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.CharacterFormFields
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import kotlinx.coroutines.delay
import javax.inject.Inject

class NewCharacterUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) : NewCharacterUseCase {
        override suspend fun generateCharacterIntroduction(sagaContext: SagaDraft?): RequestResult<SagaCreationGen> =
            executeRequest {
                gemmaClient.generate(CharacterPrompts.characterIntroPrompt(sagaContext))!!
            }

        override suspend fun replyCharacterForm(
            currentMessages: List<ChatMessage>,
            latestMessage: String?,
            currentCharacterInfo: CharacterInfo,
            sagaContext: SagaDraft,
        ): RequestResult<SagaCreationGen> =
            executeRequest {
                val delayDefaultTime = 700L

                val userInput = currentMessages.last().text

                val extractedDataPrompt =
                    gemmaClient.generate<CharacterInfo>(
                        CharacterPrompts.extractCharacterDataPrompt(
                            currentCharacterInfo = currentCharacterInfo,
                            userInput = userInput,
                            lastMessage = latestMessage!!,
                            sagaContext = sagaContext,
                        ),
                        requireTranslation = true,
                    )!!

                delay(delayDefaultTime)

                val identifyNextFieldPrompt =
                    gemmaClient
                        .generate<String>(
                            CharacterPrompts.identifyNextCharacterFieldPrompt(extractedDataPrompt),
                            requireTranslation = false,
                        )!!
                        .replace("\n", "")

                val field = CharacterFormFields.getByKey(identifyNextFieldPrompt)!!

                delay(delayDefaultTime)

                val nextQuestion =
                    gemmaClient.generate<SagaCreationGen>(
                        CharacterPrompts.generateCharacterQuestionPrompt(
                            field,
                            extractedDataPrompt,
                            sagaContext,
                        ),
                    )!!

                // Return with updated character data in callback
                nextQuestion.copy(
                    callback =
                        nextQuestion.callback?.copy(
                            data =
                                nextQuestion.callback.data?.copy(
                                    character = extractedDataPrompt,
                                ),
                        ),
                )
            }
    }
