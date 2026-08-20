package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.EpiloguePrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueReply
import javax.inject.Inject

class EpilogueChatUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
    ) : EpilogueChatUseCase {
        override suspend fun openConversation(
            saga: SagaContent,
            character: CharacterContent,
            arcs: List<CharacterArc>,
        ): RequestResult<EpilogueReply?> =
            executeRequest {
                generateTurn(saga, character, arcs, conversationSoFar = emptyList(), userMessage = null)
            }

        override suspend fun reply(
            saga: SagaContent,
            character: CharacterContent,
            arcs: List<CharacterArc>,
            conversationSoFar: List<EpilogueMessage>,
            userMessage: String,
        ): RequestResult<EpilogueReply?> =
            executeRequest {
                generateTurn(saga, character, arcs, conversationSoFar, userMessage)
            }

        private suspend fun generateTurn(
            saga: SagaContent,
            character: CharacterContent,
            arcs: List<CharacterArc>,
            conversationSoFar: List<EpilogueMessage>,
            userMessage: String?,
        ): EpilogueReply? {
            val prompt =
                EpiloguePrompts.epilogueTurnPrompt(
                    promptService = promptService,
                    saga = saga,
                    character = character,
                    arcs = arcs,
                    conversationSoFar = conversationSoFar,
                    userMessage = userMessage,
                )

            return gemmaClient.generate<EpilogueReply>(
                promptSplit =
                    prompt.mergeInstructions(
                        genreConfigService.conversationInstructions(saga.data.genre),
                    ),
                userInteraction = true,
                requirement = ModelRequirement.MEDIUM,
            )
        }
    }
