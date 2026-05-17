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
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
        private val genreConfigService: com.ilustris.sagai.core.ai.services.GenreConfigService,
    ) : NewCharacterUseCase {
        override suspend fun generateCharacterIntroduction(sagaContext: SagaDraft?): RequestResult<CharacterCreationGen> =
            executeRequest {
                gemmaClient.generate(
                    CharacterPrompts.characterIntroPrompt(
                        promptService,
                        sagaContext,
                    ),
                blueprintKey = CharacterPrompts.CONVERSATIONAL_CHARACTER_REPLY_BLUEPRINT)!!
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
                            promptService = promptService,
                            currentCharacterInfo = currentCharacterInfo,
                            userInput = userInput,
                            conversationHistory = recentMessages,
                            sagaContext = sagaContext,
                        ),
                        requireTranslation = true,
                    blueprintKey = CharacterPrompts.CHARACTER_GENERATION_BLUEPRINT)!!

                response
            }

        override suspend fun adaptCharacterToGenre(
            characterInfo: CharacterInfo,
            newGenre: String,
        ): RequestResult<CharacterCreationGen> =
            executeRequest {
                gemmaClient.generate(
                    CharacterPrompts.characterAdaptationPrompt(
                        promptService,
                        characterInfo,
                        newGenre,
                    ),
                blueprintKey = CharacterPrompts.CONVERSATIONAL_CHARACTER_REPLY_BLUEPRINT)!!
            }

        override suspend fun refineCharacterDraft(
            rawInput: String,
            sagaContext: SagaDraft?,
        ): RequestResult<CharacterCreationGen> =
            executeRequest {
                val appearanceGuidelines =
                    sagaContext?.let {
                        genreConfigService
                            .getGenreConfig(
                                it.genre,
                            ).appearanceGuidelines
                    } ?: ""
                gemmaClient.generate(
                    CharacterPrompts.refineCharacterDraftPrompt(
                        promptService,
                        rawInput,
                        sagaContext,
                        appearanceGuidelines,
                    ),
                    requireTranslation = true,
                blueprintKey = CharacterPrompts.CONVERSATIONAL_CHARACTER_REPLY_BLUEPRINT)!!
            }
    }
