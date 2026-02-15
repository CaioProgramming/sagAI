package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.CharacterCreationGen
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import javax.inject.Inject

class NewCharacterUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) : NewCharacterUseCase {
        override suspend fun generateCharacterIntroduction(sagaContext: SagaDraft?): RequestResult<CharacterCreationGen> =
            executeRequest {
                gemmaClient.generate(CharacterPrompts.characterIntroPrompt(sagaContext))!!
            }

        override suspend fun replyCharacterForm(
            currentMessages: List<ChatMessage>,
            currentCharacterInfo: CharacterInfo,
            sagaContext: SagaDraft,
        ): RequestResult<CharacterCreationGen> =
            executeRequest {
                // Limit conversation history to last 10 messages for context
                val recentMessages = currentMessages.takeLast(10)

                val userInput = currentMessages.last().text

                // Single AI call to extract, enhance, and provide helpful suggestions
                val response =
                    gemmaClient.generate<CharacterCreationGen>(
                        CharacterPrompts.conversationalCharacterReply(
                            currentCharacterInfo = currentCharacterInfo,
                            userInput = userInput,
                            conversationHistory = recentMessages,
                            sagaContext = sagaContext,
                        ),
                        requireTranslation = true,
                    )!!

                response
            }

        override suspend fun adaptCharacterToGenre(
            characterInfo: CharacterInfo,
            newGenre: String,
        ): RequestResult<CharacterCreationGen> =
            executeRequest {
                gemmaClient.generate(
                    CharacterPrompts.characterAdaptationPrompt(
                        characterInfo,
                        newGenre,
                    ),
                )!!
            }

        override suspend fun refineCharacterDraft(
            rawInput: String,
            sagaContext: SagaDraft?,
        ): RequestResult<CharacterCreationGen> =
            executeRequest {
                gemmaClient.generate(
                    CharacterPrompts.refineCharacterDraftPrompt(rawInput, sagaContext),
                    requireTranslation = true,
            )!!
        }
    }
