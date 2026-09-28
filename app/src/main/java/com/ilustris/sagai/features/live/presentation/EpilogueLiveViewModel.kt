package com.ilustris.sagai.features.live.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.AudioGenClient
import com.ilustris.sagai.core.ai.GuardrailsException
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.live.data.LiveAudioPlayer
import com.ilustris.sagai.features.live.data.LiveBackgroundWork
import com.ilustris.sagai.features.live.data.VoiceRecorder
import com.ilustris.sagai.features.live.data.VoiceRecording
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.chat.data.model.InputMode
import com.ilustris.sagai.features.saga.chat.data.usecase.EpilogueChatUseCase
import com.ilustris.sagai.features.saga.chat.data.voicing.MessageVoicingUseCase
import com.ilustris.sagai.features.saga.chat.data.voicing.VoiceCastingUseCase
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Live mode for the epilogue: a one-on-one talk with a single character after the saga ended. The
 * player always speaks as their protagonist (no carousel, [canSwitchSpeaker] is off) and the reply
 * always comes from that one character, through [EpilogueChatUseCase] — the same persisted
 * conversation the typed epilogue chat shows, so leaving live mode lands on every line said here.
 * Both voices are kept under the character's epilogue folder, which goes with the saga.
 */
@HiltViewModel
class EpilogueLiveViewModel
    @Inject
    constructor(
        private val epilogueChatUseCase: EpilogueChatUseCase,
        private val characterUseCase: CharacterUseCase,
        private val sagaRepository: SagaRepository,
        private val messageVoicingUseCase: MessageVoicingUseCase,
        private val voiceCastingUseCase: VoiceCastingUseCase,
        private val fileHelper: FileHelper,
        audioGenClient: AudioGenClient,
        permissionService: PermissionService,
        recorder: VoiceRecorder,
        player: LiveAudioPlayer,
        backgroundWork: LiveBackgroundWork,
        @ApplicationContext context: Context,
    ) : BaseLiveViewModel(permissionService, recorder, player, audioGenClient, backgroundWork, context) {
        override val canSwitchSpeaker = false

        private var loadedFor: Pair<Int, Int>? = null
        private var saga: SagaContent? = null
        private var character: CharacterContent? = null
        private var arcs: List<CharacterArc> = emptyList()

        /** The player's saved turn waiting on a reply, with its audio: what a retry resends. */
        private var pending: PendingTurn? = null

        private class PendingTurn(
            val message: EpilogueMessage,
            val wav: ByteArray,
        )

        fun start(
            sagaId: Int,
            characterId: Int,
        ) {
            if (loadedFor == sagaId to characterId) return
            loadedFor = sagaId to characterId
            viewModelScope.launch {
                val content = sagaRepository.getSagaById(sagaId).first() ?: return@launch
                val metadata = sagaRepository.getSagaMetadata(sagaId).first() ?: return@launch
                val loadedCharacter = characterUseCase.getCharacterContent(characterId).first() ?: return@launch
                saga = content
                character = loadedCharacter
                arcs = characterUseCase.getCharacterArcs(characterId).first()

                val protagonist = metadata.mainCharacter
                _state.update {
                    it.copy(
                        saga = metadata,
                        speakers = listOfNotNull(protagonist),
                        selectedSpeakerId = protagonist?.id,
                    )
                }
                // Cast before the first turn, so the first reply rarely waits on it.
                backgroundWork.scope.launch { runCatching { voiceCastingUseCase.voiceFor(metadata, loadedCharacter.data) } }
            }
            startSharedObservers()
        }

        override suspend fun send(recording: VoiceRecording) {
            val content = saga ?: return toIdle(LiveHint.REPLY_FAILED)
            val current = character ?: return toIdle(LiveHint.REPLY_FAILED)
            enterThinking(_state.value.selectedSpeaker)

            val turn =
                withContext(Dispatchers.IO) {
                    val wav = recording.file.readBytes()
                    recording.file.delete()
                    val saved =
                        epilogueChatUseCase.saveMessage(
                            EpilogueMessage(
                                sagaId = content.data.id,
                                characterId = current.data.id,
                                text = "",
                                isUser = true,
                                inputMode = InputMode.VOICE,
                            ),
                        )
                    val audio =
                        fileHelper.saveBinaryFile(
                            wav,
                            path = audioDirectory(content, current),
                            fileName = "message_${saved.id}_audio",
                            extension = "wav",
                        )
                    val withAudio = saved.copy(audioPath = audio?.absolutePath)
                    epilogueChatUseCase.updateMessage(withAudio)
                    PendingTurn(withAudio, wav)
                }
            answer(content, current, turn)
        }

        override fun retry() {
            val content = saga ?: return
            val current = character ?: return
            val turn = pending ?: return
            enterThinking(_state.value.selectedSpeaker)
            viewModelScope.launch { answer(content, current, turn) }
        }

        private suspend fun answer(
            content: SagaContent,
            current: CharacterContent,
            turn: PendingTurn,
        ) {
            pending = turn
            val history =
                withContext(Dispatchers.IO) {
                    epilogueChatUseCase.conversation(current.data.id).filter { it.id != turn.message.id }
                }
            val result = epilogueChatUseCase.voiceTurn(content, current, arcs, history, turn.wav)
            // Left while the reply was being written (see abandonPendingTurn): keep what came back,
            // but nothing plays in the background and no voicing starts.
            val stillHere = pending === turn
            when (result) {
                is RequestResult.Error -> {
                    if (result.value is GuardrailsException) {
                        pending = null
                        withContext(Dispatchers.IO) { epilogueChatUseCase.deleteMessage(turn.message) }
                        if (stillHere) {
                            setTurnInFlight(false)
                            toIdle(LiveHint.GUARDRAIL)
                        }
                    } else if (stillHere) {
                        failTurn()
                    }
                }

                is RequestResult.Success -> {
                    pending = null
                    val voiceTurn = result.value
                    val reply =
                        withContext(Dispatchers.IO) {
                            voiceTurn.playerLine?.let { line -> epilogueChatUseCase.updateMessage(turn.message.copy(text = line)) }
                            epilogueChatUseCase.saveMessage(
                                EpilogueMessage(
                                    sagaId = content.data.id,
                                    characterId = current.data.id,
                                    text = voiceTurn.reply.text,
                                    isUser = false,
                                    emotionalTone = voiceTurn.reply.emotionalTone,
                                ),
                            )
                        }
                    epilogueChatUseCase.compactKnowledge(content, current)
                    if (!stillHere) return

                    val metadata = _state.value.saga ?: return
                    performReply(
                        playerLine = voiceTurn.playerLine.orEmpty(),
                        replyText = reply.text,
                        replyFocus = LiveFocus(current.data),
                    ) {
                        messageVoicingUseCase
                            .voiceText(
                                saga = metadata,
                                character = current.data,
                                text = reply.text,
                                emotionalTone = reply.emotionalTone,
                                directory = audioDirectory(content, current),
                                fileName = "message_${reply.id}_audio",
                            )?.also { clip -> epilogueChatUseCase.updateMessage(reply.copy(audioPath = clip.audioPath)) }
                    }
                }
            }
        }

        override fun abandonPendingTurn() {
            pending = null
        }

        override fun onCleared() {
            // A short live visit still leaves a mark on what the character knows of the player.
            val content = saga
            val current = character
            if (content != null && current != null) epilogueChatUseCase.compactKnowledge(content, current, leaving = true)
            super.onCleared()
        }

        /** Relative to the files dir; same folder [EpilogueChatUseCase.clearConversation] wipes. */
        private fun audioDirectory(
            content: SagaContent,
            current: CharacterContent,
        ) = "sagas/${content.data.id}/epilogue/${current.data.id}"
    }
