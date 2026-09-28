package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.ai.ModelCatalog
import com.ilustris.sagai.core.ai.TranscribeClient
import com.ilustris.sagai.core.ai.model.AudioAttachment
import com.ilustris.sagai.core.ai.model.PromptBlueprint
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueVoiceTurn
import com.ilustris.sagai.features.saga.chat.data.model.InputMode
import timber.log.Timber
import android.content.Context
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.features.characters.data.usecase.CharacterKnowledgeService
import com.ilustris.sagai.features.saga.datasource.EpilogueMessageDao
import dagger.hilt.android.qualifiers.ApplicationContext
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
import java.io.File
import javax.inject.Inject

class EpilogueChatUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
        private val epilogueMessageDao: EpilogueMessageDao,
        private val knowledgeService: CharacterKnowledgeService,
        private val remoteConfigService: RemoteConfigService,
        private val modelCatalog: ModelCatalog,
        private val transcribeClient: TranscribeClient,
        @ApplicationContext private val context: Context,
    ) : EpilogueChatUseCase {
        override fun observeConversation(characterId: Int): Flow<List<EpilogueMessage>> =
            epilogueMessageDao.observeConversation(characterId)

        override suspend fun saveMessage(message: EpilogueMessage): EpilogueMessage =
            message.copy(id = epilogueMessageDao.insert(message).toInt())

        override suspend fun updateMessage(message: EpilogueMessage) = epilogueMessageDao.update(message)

        override suspend fun deleteMessage(message: EpilogueMessage) {
            epilogueMessageDao.delete(message)
            message.audioPath?.let { File(it).delete() }
        }

        override suspend fun conversation(characterId: Int): List<EpilogueMessage> = epilogueMessageDao.getConversation(characterId)

        override suspend fun clearConversation(
            sagaId: Int,
            characterId: Int,
        ) {
            epilogueMessageDao.deleteConversation(characterId)
            audioDirectory(context, sagaId, characterId).deleteRecursively()
        }

        override fun openConversation(
            saga: SagaContent,
            character: CharacterContent,
            arcs: List<CharacterArc>,
            history: List<EpilogueMessage>,
        ): Flow<StreamingState<EpilogueReply?>> =
            generateTurn(saga, character, arcs, conversationSoFar = history, userMessage = null)

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
                            knowledge = knowledgeService.get(character.data.id),
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

        override suspend fun voiceTurn(
            saga: SagaContent,
            character: CharacterContent,
            arcs: List<CharacterArc>,
            conversationSoFar: List<EpilogueMessage>,
            wav: ByteArray,
        ): RequestResult<EpilogueVoiceTurn> =
            executeRequest {
                // Same rule as the saga's voice turns: the reply hears the audio when its model can,
                // otherwise it gets a transcript in its place.
                val canHear = modelCatalog.supportsAudioInput(gemmaClient.modelName(ModelRequirement.HIGH))
                val transcript = if (canHear) null else transcribe(wav) ?: error("Couldn't transcribe the voice turn")

                val prompt =
                    EpiloguePrompts.epilogueTurnPrompt(
                        promptService = promptService,
                        saga = saga,
                        character = character,
                        arcs = arcs,
                        conversationSoFar = conversationSoFar,
                        userMessage = transcript.orEmpty(),
                        knowledge = knowledgeService.get(character.data.id),
                    )
                val playerInputInstructions =
                    ChatPrompts.playerInputInstructions(
                        runCatching { remoteConfigService.getJson<PromptBlueprint>(ChatPrompts.PLAYER_INPUT_BLUEPRINT) }.getOrNull(),
                        InputMode.VOICE,
                    )
                val reply =
                    gemmaClient.generate<EpilogueReply>(
                        promptSplit =
                            prompt.mergeInstructions(
                                genreConfigService.conversationInstructions(saga.data.genre),
                                playerInputInstructions,
                            ),
                        userInteraction = true,
                        requirement = ModelRequirement.HIGH,
                        audio = if (canHear) AudioAttachment(wav) else null,
                        thinkingLevelOverride = gemmaClient.voiceThinkingLevel(ModelRequirement.HIGH),
                    ) ?: error("Epilogue voice reply returned no result")

                val heard =
                    reply.playerInput
                        ?.takeIf { it.understood }
                        ?.correctedText
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                EpilogueVoiceTurn(
                    playerLine = heard ?: transcript ?: transcribe(wav),
                    reply = reply,
                )
            }

        private suspend fun transcribe(wav: ByteArray): String? =
            runCatching { transcribeClient.transcribeWords(wav, languageCode = null).text.trim() }
                .onFailure { Timber.w(it, "Epilogue voice turn transcription failed") }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }

        override fun compactKnowledge(
            saga: SagaContent,
            character: CharacterContent,
            leaving: Boolean,
        ) = knowledgeService.compactIfDue(saga, character, leaving)

        companion object {
            /** Where a character's epilogue audio lives; inside the saga folder, so it goes with the saga. */
            fun audioDirectory(
                context: Context,
                sagaId: Int,
                characterId: Int,
            ): File = context.filesDir.resolve("sagas/$sagaId/epilogue/$characterId")
        }
    }
