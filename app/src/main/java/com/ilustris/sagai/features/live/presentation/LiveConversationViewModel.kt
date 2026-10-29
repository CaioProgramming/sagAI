package com.ilustris.sagai.features.live.presentation

import MessageStatus
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.AudioGenClient
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.live.data.LiveAudioPlayer
import com.ilustris.sagai.features.live.data.LiveBackgroundWork
import com.ilustris.sagai.features.live.data.LiveSessionTracker
import com.ilustris.sagai.features.live.data.VoiceRecorder
import com.ilustris.sagai.features.live.data.VoiceRecording
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.data.model.ChatGenerationOutcome
import com.ilustris.sagai.features.saga.chat.data.model.InputMode
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.usecase.ChatGenerationService
import com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.data.voicing.MessageVoicingUseCase
import com.ilustris.sagai.features.saga.chat.data.voicing.VoiceCastingUseCase
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.repository.ReactionRepository
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The saga's live conversation: the turn's choreography is [BaseLiveViewModel]'s; this sends the
 * player's voice through the same pipeline and messages table as the chat ([ChatGenerationService],
 * reply saved in the background), voices the reply onto its message, and keeps the saga's extras —
 * the speaker carousel, milestones, reactions and pre-casting the scene.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LiveConversationViewModel
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val messageUseCase: MessageUseCase,
        private val chatGenerationService: ChatGenerationService,
        private val messageVoicingUseCase: MessageVoicingUseCase,
        private val voiceCastingUseCase: VoiceCastingUseCase,
        private val reactionRepository: ReactionRepository,
        private val sagaContentManager: SagaContentManager,
        private val fileHelper: FileHelper,
        private val sessionTracker: LiveSessionTracker,
        audioGenClient: AudioGenClient,
        permissionService: PermissionService,
        recorder: VoiceRecorder,
        player: LiveAudioPlayer,
        backgroundWork: LiveBackgroundWork,
        @ApplicationContext context: Context,
    ) : BaseLiveViewModel(permissionService, recorder, player, audioGenClient, backgroundWork, context) {
        private var sagaId: Int? = null
        private val sessionStart = System.currentTimeMillis()
        private val sessionMessageIds = MutableStateFlow<Set<Int>>(emptySet())
        private val seenReactionIds = mutableSetOf<Int>()
        private var pendingUserMessage: Message? = null
        private var failedUserMessage: Message? = null

        fun start(sagaId: Int) {
            if (this.sagaId == sagaId) return
            this.sagaId = sagaId
            observeSaga(sagaId)
            observeOutcomes(sagaId)
            observeReasoning(sagaId)
            observeMilestones()
            observeReactions()
            startSharedObservers()
        }

        // region Turn

        override suspend fun send(recording: VoiceRecording) {
            val saga = _state.value.saga ?: return toIdle(LiveHint.REPLY_FAILED)
            val speaker = _state.value.selectedSpeaker ?: return toIdle(LiveHint.REPLY_FAILED)
            val timeline = saga.getCurrentTimeLine()
            if (timeline == null) {
                recording.file.delete()
                sagaContentManager.checkNarrativeProgression(saga)
                return toIdle(LiveHint.MILESTONE)
            }

            enterThinking(speaker)

            val message =
                withContext(Dispatchers.IO) {
                    val saved =
                        messageUseCase
                            .saveMessage(
                                saga,
                                Message(
                                    text = "",
                                    senderType = SenderType.USER,
                                    speakerName = speaker.name,
                                    characterId = speaker.id,
                                    sagaId = saga.data.id,
                                    timelineId = timeline.data.id,
                                    status = MessageStatus.LOADING,
                                    inputMode = InputMode.VOICE,
                                    audible = true,
                                ),
                                isFromUser = true,
                            ).getSuccess() ?: return@withContext null
                    val audio =
                        fileHelper.saveBinaryFile(
                            recording.file.readBytes(),
                            path = "sagas/${saga.data.id}/audios",
                            fileName = "message_${saved.id}_audio",
                            extension = "wav",
                        )
                    recording.file.delete()
                    val withAudio = saved.copy(audioPath = audio?.absolutePath)
                    messageUseCase.updateMessage(withAudio).getSuccess() ?: withAudio
                }
            if (message == null) {
                setTurnInFlight(false)
                return toIdle(LiveHint.REPLY_FAILED)
            }

            generate(saga, message, speaker)
        }

        private fun generate(
            saga: SagaMetadata,
            message: Message,
            speaker: Character?,
        ) {
            pendingUserMessage = message
            sessionMessageIds.update { it + message.id }
            chatGenerationService.generate(
                saga,
                MessageContent(message = message, character = speaker, reactions = emptyList()),
                saga.getCurrentTimeLine()?.data?.sceneSummary,
            )
        }

        override fun retry() {
            val saga = _state.value.saga ?: return
            val failed = failedUserMessage ?: return
            failedUserMessage = null
            enterThinking(saga.findCharacter(failed.characterId))
            _state.update { it.copy(playerLinePending = failed.text.isBlank()) }
            viewModelScope.launch(Dispatchers.IO) {
                val retrying = failed.copy(status = MessageStatus.LOADING)
                messageUseCase.updateMessage(retrying)
                generate(saga, retrying, saga.findCharacter(failed.characterId))
            }
        }

        private fun onReply(outcome: ChatGenerationOutcome.Success) {
            val pending = pendingUserMessage ?: return
            if (outcome.userMessage.id != pending.id) return
            pendingUserMessage = null
            val saga = _state.value.saga ?: return
            val reply = outcome.reply.message
            sessionMessageIds.update { it + reply.id }

            val replyFocus =
                LiveFocus(
                    reply.characterId
                        ?.takeIf { reply.senderType != SenderType.NARRATOR }
                        ?.let { id -> saga.findCharacter(id) },
                )
            performReply(
                playerLine = outcome.userMessage.text,
                replyText = reply.text,
                replyFocus = replyFocus,
            ) { messageVoicingUseCase.voice(saga, reply)?.clip }
        }

        // Dropping the pending message makes onReply ignore the reply when it lands, so it can't
        // start playing while the app is in the background.
        override fun abandonPendingTurn() {
            pendingUserMessage = null
        }

        override fun onTurnInFlight(inFlight: Boolean) {
            sagaId?.let { sessionTracker.setTurnInFlight(it, inFlight) }
        }

        // endregion

        // region Observers

        private fun observeSaga(sagaId: Int) {
            viewModelScope.launch {
                var castDone = false
                sagaRepository.getSagaMetadata(sagaId).filterNotNull().collect { saga ->
                    val speakers = centeredSpeakers(saga)
                    _state.update {
                        it.copy(
                            saga = saga,
                            speakers = speakers,
                            selectedSpeakerId = it.selectedSpeakerId ?: saga.mainCharacter?.id,
                        )
                    }
                    if (!castDone) {
                        castDone = true
                        // Cast the scene before the first turn, so voicing rarely waits on it.
                        backgroundWork.scope.launch {
                            val present =
                                saga
                                    .getCurrentTimeLine()
                                    ?.data
                                    ?.sceneSummary
                                    ?.charactersPresent
                                    ?.mapNotNull { saga.findCharacter(it.name) }
                                    .orEmpty()
                            runCatching { voiceCastingUseCase.narratorVoice(saga) }
                            voiceCastingUseCase.castAll(saga, present.filter { it.id != saga.mainCharacter?.id })
                        }
                    }
                }
            }
        }

        /**
         * The main character in the middle and the rest split around it, like Instagram's filter
         * carousel: the selector opens centered on the main character with others on both sides,
         * instead of everyone piled to its right.
         */
        private fun centeredSpeakers(saga: SagaMetadata): List<Character> {
            val main = saga.mainCharacter
            val others = saga.characters.filter { it.id != main?.id }.sortedBy { it.joinedAt }
            if (main == null) return others
            val left = others.filterIndexed { index, _ -> index % 2 == 1 }.reversed()
            val right = others.filterIndexed { index, _ -> index % 2 == 0 }
            return left + main + right
        }

        private fun observeOutcomes(sagaId: Int) {
            viewModelScope.launch {
                chatGenerationService.outcomes.collect { outcome ->
                    when (outcome) {
                        is ChatGenerationOutcome.Success -> {
                            if (outcome.sagaId == sagaId) onReply(outcome)
                        }

                        is ChatGenerationOutcome.Error -> {
                            if (outcome.sagaId != sagaId || pendingUserMessage == null) return@collect
                            failedUserMessage = pendingUserMessage
                            pendingUserMessage = null
                            failTurn()
                        }

                        is ChatGenerationOutcome.GuardrailBlocked -> {
                            if (outcome.sagaId != sagaId || pendingUserMessage == null) return@collect
                            pendingUserMessage = null
                            setTurnInFlight(false)
                            toIdle(LiveHint.GUARDRAIL)
                        }
                    }
                }
            }
        }

        private fun observeReasoning(sagaId: Int) {
            viewModelScope.launch {
                chatGenerationService.activeGenerations.collect { active ->
                    val reasoning = active[sagaId]?.reasoning
                    if (_state.value.phase == LivePhase.Thinking) _state.update { it.copy(reasoning = reasoning) }
                }
            }
        }

        private fun observeMilestones() {
            viewModelScope.launch {
                sagaContentManager.milestoneUpdate.collect { milestone ->
                    val blocking =
                        milestone is SagaMilestone.Loading ||
                            milestone is SagaMilestone.NewEvent ||
                            milestone is SagaMilestone.ChapterFinished ||
                            milestone is SagaMilestone.ActFinished
                    _state.update {
                        it.copy(
                            inputBlocked = blocking,
                            hint = if (blocking && it.phase == LivePhase.Idle) LiveHint.MILESTONE else it.hint,
                        )
                    }
                }
            }
        }

        private fun observeReactions() {
            viewModelScope.launch {
                sessionMessageIds
                    .flatMapLatest { ids ->
                        if (ids.isEmpty()) emptyFlow() else reactionRepository.observeReactions(ids.toList(), sessionStart)
                    }.collectLatest { reactions ->
                        reactions
                            .filter { seenReactionIds.add(it.data.id) }
                            .forEach { reaction ->
                                _reactions.tryEmit(
                                    LiveReaction(
                                        id = reaction.data.id,
                                        emoji = reaction.data.emoji,
                                        thought = reaction.data.thought,
                                        character = reaction.character,
                                    ),
                                )
                            }
                    }
            }
        }

        // endregion
    }
