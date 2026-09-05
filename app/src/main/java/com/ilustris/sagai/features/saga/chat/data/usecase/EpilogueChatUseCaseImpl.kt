package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.EpiloguePrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueReply
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class EpilogueChatUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
    ) : EpilogueChatUseCase {
        override fun openConversation(
            saga: SagaContent,
            character: CharacterContent,
            arcs: List<CharacterArc>,
        ): Flow<StreamingState<EpilogueReply?>> =
            generateTurn(saga, character, arcs, conversationSoFar = emptyList(), userMessage = null)

        override fun reply(
            saga: SagaContent,
            character: CharacterContent,
            arcs: List<CharacterArc>,
            conversationSoFar: List<EpilogueMessage>,
            userMessage: String,
        ): Flow<StreamingState<EpilogueReply?>> = generateTurn(saga, character, arcs, conversationSoFar, userMessage)

        private fun generateTurn(
            saga: SagaContent,
            character: CharacterContent,
            arcs: List<CharacterArc>,
            conversationSoFar: List<EpilogueMessage>,
            userMessage: String?,
        ): Flow<StreamingState<EpilogueReply?>> =
            flow {
                try {
                    val prompt =
                        EpiloguePrompts.epilogueTurnPrompt(
                            promptService = promptService,
                            saga = saga,
                            character = character,
                            arcs = arcs,
                            conversationSoFar = conversationSoFar,
                            userMessage = userMessage,
                        )

                    val generateStream =
                        gemmaClient.generateStreaming<EpilogueReply>(
                            promptSplit =
                                prompt.mergeInstructions(
                                    genreConfigService.conversationInstructions(saga.data.genre),
                                ),
                            userInteraction = true,
                            requirement = ModelRequirement.HIGH,
                        )

                    emitAll(
                        reasoningSynthesizerService.synthesizeReasoning(
                            generateStream,
                            context = "Reconnecting with ${character.data.fullName()} after their story ended",
                            genre = saga.data.genre,
                            details = userMessage,
                        ),
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(
                        StreamingState.Error(
                            message = e.message ?: "Unknown error",
                            throwable = e,
                        ),
                    )
                }
            }
    }
