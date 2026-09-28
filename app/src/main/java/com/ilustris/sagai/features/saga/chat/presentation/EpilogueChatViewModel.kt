package com.ilustris.sagai.features.saga.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueReply
import com.ilustris.sagai.features.saga.chat.data.usecase.EpilogueChatUseCase
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The epilogue chat with one character. The conversation is persisted per character and observed
 * from the database, so it survives leaving the screen and process death; the character opens the
 * talk on a first meeting or when enough time passed since the last one (a reunion), and every few
 * turns — and on the way out — the conversation is folded into what the character knows about the
 * player. It never touches the saga's own messages.
 */
@HiltViewModel
class EpilogueChatViewModel
    @Inject
    constructor(
        private val epilogueChatUseCase: EpilogueChatUseCase,
        private val characterUseCase: CharacterUseCase,
        private val sagaRepository: SagaRepository,
    ) : ViewModel() {
        private val _messages = MutableStateFlow<List<EpilogueMessage>>(emptyList())
        val messages: StateFlow<List<EpilogueMessage>> = _messages.asStateFlow()

        private val _isReplying = MutableStateFlow(false)
        val isReplying: StateFlow<Boolean> = _isReplying.asStateFlow()

        private val _reasoningChunk = MutableStateFlow<String?>(null)
        val reasoningChunk: StateFlow<String?> = _reasoningChunk.asStateFlow()

        private val _character = MutableStateFlow<CharacterContent?>(null)
        val character: StateFlow<CharacterContent?> = _character.asStateFlow()

        private val _protagonist = MutableStateFlow<CharacterContent?>(null)
        val protagonist: StateFlow<CharacterContent?> = _protagonist.asStateFlow()

        private val _genre = MutableStateFlow<Genre?>(null)
        val genre: StateFlow<Genre?> = _genre.asStateFlow()

        private val _relationshipSubtitle = MutableStateFlow<String?>(null)
        val relationshipSubtitle: StateFlow<String?> = _relationshipSubtitle.asStateFlow()

        private val _error = MutableStateFlow(false)
        val error: StateFlow<Boolean> = _error.asStateFlow()

        private var saga: SagaContent? = null
        private var arcs: List<CharacterArc> = emptyList()
        private var loadedFor: Pair<Int, Int>? = null
        private var loadJob: Job? = null

        /**
         * Nav3 can reuse this screen's backstack entry (and this ViewModel instance) across
         * different characters — e.g. going back to the roster and picking someone else doesn't
         * guarantee a fresh ViewModel. Any change in [sagaId]/[characterId] must fully wipe the
         * previous conversation before loading the new one, and cancel any still-running load so
         * a slow response for the old character can't land after the reset and show up under the
         * new one's name.
         */
        fun load(
            sagaId: Int,
            characterId: Int,
        ) {
            val key = sagaId to characterId
            if (loadedFor == key) return
            loadedFor = key

            loadJob?.cancel()
            compactOnLeave()
            resetState()

            loadJob =
                viewModelScope.launch {
                    val loadedSaga = sagaRepository.getSagaById(sagaId).first() ?: return@launch
                    val loadedCharacter = characterUseCase.getCharacterContent(characterId).first() ?: return@launch
                    val loadedArcs = characterUseCase.getCharacterArcs(characterId).first()

                    saga = loadedSaga
                    arcs = loadedArcs
                    _character.value = loadedCharacter
                    _protagonist.value = loadedSaga.mainCharacter
                    _genre.value = loadedSaga.data.genre
                    _relationshipSubtitle.value =
                        loadedSaga.mainCharacter
                            ?.data
                            ?.id
                            ?.let { protagonistId -> loadedCharacter.findRelationship(protagonistId) }
                            ?.let { relation -> "${relation.data.emoji} ${relation.data.title}".trim() }

                    val history = epilogueChatUseCase.observeConversation(characterId).first()
                    _messages.value = history
                    launch { epilogueChatUseCase.observeConversation(characterId).collect { _messages.value = it } }

                    // First meeting, or a reunion after a while: the character speaks first. A
                    // quick return just picks up where the talk stopped.
                    val lastTalk = history.lastOrNull()?.timestamp
                    if (lastTalk == null || System.currentTimeMillis() - lastTalk > REUNION_GAP_MS) {
                        collectTurn(epilogueChatUseCase.openConversation(loadedSaga, loadedCharacter, loadedArcs, history))
                    }
                }
        }

        private fun resetState() {
            _messages.value = emptyList()
            _error.value = false
            _isReplying.value = false
            _reasoningChunk.value = null
            _character.value = null
            _protagonist.value = null
            _genre.value = null
            _relationshipSubtitle.value = null
            saga = null
            arcs = emptyList()
        }

        fun sendMessage(text: String) {
            val trimmed = text.trim()
            if (trimmed.isEmpty() || _isReplying.value) return
            val currentSaga = saga ?: return
            val currentCharacter = _character.value ?: return

            _error.value = false
            // Shown right away; the database emission that follows carries the same turn.
            val pending = EpilogueMessage(sagaId = currentSaga.data.id, characterId = currentCharacter.data.id, text = trimmed, isUser = true)
            val conversation = _messages.value + pending
            _messages.value = conversation

            viewModelScope.launch {
                epilogueChatUseCase.saveMessage(pending)
                collectTurn(
                    epilogueChatUseCase.reply(
                        saga = currentSaga,
                        character = currentCharacter,
                        arcs = arcs,
                        conversationSoFar = conversation,
                        userMessage = trimmed,
                    ),
                )
                epilogueChatUseCase.compactKnowledge(currentSaga, currentCharacter)
            }
        }

        /** Starts over: the turns go, the character still remembers the player, and greets them again. */
        fun restartConversation() {
            val currentSaga = saga ?: return
            val currentCharacter = _character.value ?: return
            if (_isReplying.value) return
            viewModelScope.launch {
                epilogueChatUseCase.clearConversation(currentSaga.data.id, currentCharacter.data.id)
                collectTurn(epilogueChatUseCase.openConversation(currentSaga, currentCharacter, arcs, emptyList()))
            }
        }

        private suspend fun collectTurn(turnFlow: Flow<StreamingState<EpilogueReply?>>) {
            _isReplying.value = true
            _reasoningChunk.value = null

            turnFlow.collect { state ->
                when (state) {
                    is StreamingState.Reasoning -> {
                        _reasoningChunk.value = state.chunk
                    }

                    is StreamingState.Success -> {
                        _isReplying.value = false
                        _reasoningChunk.value = null
                        val currentSaga = saga
                        val currentCharacter = _character.value
                        val reply = state.data
                        if (reply != null && reply.text.isNotBlank() && currentSaga != null && currentCharacter != null) {
                            epilogueChatUseCase.saveMessage(
                                EpilogueMessage(
                                    sagaId = currentSaga.data.id,
                                    characterId = currentCharacter.data.id,
                                    text = reply.text,
                                    isUser = false,
                                    emotionalTone = reply.emotionalTone,
                                ),
                            )
                        }
                    }

                    is StreamingState.Error -> {
                        _isReplying.value = false
                        _reasoningChunk.value = null
                        if (!state.isFlowCancellation()) {
                            _error.value = true
                        }
                    }
                }
            }
        }
    
        override fun onCleared() {
            compactOnLeave()
            super.onCleared()
        }

        /** A short visit still leaves a mark on what the character knows of the player. */
        private fun compactOnLeave() {
            val currentSaga = saga ?: return
            val currentCharacter = _character.value ?: return
            epilogueChatUseCase.compactKnowledge(currentSaga, currentCharacter, leaving = true)
        }

        companion object {
            /** How long apart two visits must be for the character to greet the player again. */
            private const val REUNION_GAP_MS = 6 * 60 * 60 * 1000L
        }
    }
