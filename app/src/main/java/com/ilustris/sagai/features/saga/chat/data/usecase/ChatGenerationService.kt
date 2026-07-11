package com.ilustris.sagai.features.saga.chat.data.usecase

import MessageStatus
import com.ilustris.sagai.core.ai.GuardrailsException
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.globalshell.ChatGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.saga.chat.data.model.ChatGenerationOutcome
import com.ilustris.sagai.features.saga.chat.data.model.ChatGenerationUiState
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs chat-reply generation in a singleton-scoped coroutine, keyed per saga, so
 * switching sagas mid-reply no longer cancels the in-flight generation — mirroring
 * [com.ilustris.sagai.features.act.BookGenerationService]'s pattern. Multiple sagas can
 * generate concurrently here (unlike book gen's single slot): the actual network calls
 * are already serialized per-model by [com.ilustris.sagai.core.ai.GeminiAIClient]'s
 * request mutex, so this is safe.
 */
@Singleton
class ChatGenerationService
    @Inject
    constructor(
        private val messageUseCase: MessageUseCase,
        private val globalShellService: GlobalShellService,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private val _activeGenerations = MutableStateFlow<Map<Int, ChatGenerationUiState.Generating>>(emptyMap())
        val activeGenerations: StateFlow<Map<Int, ChatGenerationUiState.Generating>> = _activeGenerations.asStateFlow()

        private val _outcomes = MutableSharedFlow<ChatGenerationOutcome>(extraBufferCapacity = 4)
        val outcomes = _outcomes.asSharedFlow()

        private val jobs = ConcurrentHashMap<Int, Job>()

        fun generate(
            saga: SagaMetadata,
            message: MessageContent,
            sceneSummary: SceneSummary?,
        ) {
            val sagaId = saga.data.id
            if (jobs[sagaId]?.isActive == true) return

            val job =
                scope.launch {
                    _activeGenerations.update {
                        it +
                            (
                                sagaId to
                                    ChatGenerationUiState.Generating(
                                        sagaId = sagaId,
                                        sagaTitle = saga.data.title,
                                        genre = saga.data.genre,
                                        speakerName = message.character?.fullName(),
                                        reasoning = null,
                                    )
                            )
                    }
                    globalShellService.post(
                        ChatGenerationWorkEffect(
                            sagaId = sagaId,
                            sagaTitle = saga.data.title,
                            genre = saga.data.genre,
                            message = saga.data.title,
                            deepLink = "saga://chat/$sagaId/false",
                        ),
                    )

                    messageUseCase.generateMessage(saga, message).collect { state ->
                        when (state) {
                            is StreamingState.Reasoning -> {
                                _activeGenerations.update { map ->
                                    map[sagaId]?.let { current ->
                                        map + (sagaId to current.copy(reasoning = state.chunk))
                                    } ?: map
                                }
                            }

                            is StreamingState.Success -> {
                                _activeGenerations.update { it - sagaId }
                                val reply = state.data
                                if (reply != null) {
                                    _outcomes.tryEmit(ChatGenerationOutcome.Success(sagaId, reply))
                                }
                            }

                            is StreamingState.Error -> {
                                _activeGenerations.update { it - sagaId }
                                if (state.throwable is GuardrailsException) {
                                    messageUseCase.deleteMessage(message.message.id.toLong())
                                    _outcomes.tryEmit(
                                        ChatGenerationOutcome.GuardrailBlocked(sagaId, message.message),
                                    )
                                } else {
                                    messageUseCase.updateMessage(
                                        message.message.copy(status = MessageStatus.ERROR),
                                    )
                                    _outcomes.tryEmit(
                                        ChatGenerationOutcome.Error(sagaId, message.message, state.throwable),
                                    )
                                }
                            }
                        }
                    }
                }
            jobs[sagaId] = job
        }

        fun cancel(sagaId: Int) {
            jobs[sagaId]?.cancel()
            jobs.remove(sagaId)
            _activeGenerations.update { it - sagaId }
        }
    }
