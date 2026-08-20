package com.ilustris.sagai.features.saga.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.chat.data.usecase.EpilogueChatUseCase
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns the epilogue chat's conversation state entirely in memory: a plain [ViewModel]-scoped
 * [StateFlow] is enough, since the only requirement is that the conversation is wiped on process
 * death, not that it survives across screens or app restarts. Never reads from or writes to
 * [com.ilustris.sagai.features.saga.chat.datasource.MessageDao] — messages here are never
 * persisted.
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

        private val _character = MutableStateFlow<CharacterContent?>(null)
        val character: StateFlow<CharacterContent?> = _character.asStateFlow()

        private val _genre = MutableStateFlow<Genre?>(null)
        val genre: StateFlow<Genre?> = _genre.asStateFlow()

        private var saga: SagaContent? = null
        private var arcs: List<CharacterArc> = emptyList()
        private var loadedFor: Pair<Int, Int>? = null

        fun load(
            sagaId: Int,
            characterId: Int,
        ) {
            val key = sagaId to characterId
            if (loadedFor == key) return
            loadedFor = key

            viewModelScope.launch {
                val loadedSaga = sagaRepository.getSagaById(sagaId).first() ?: return@launch
                val loadedCharacter = characterUseCase.getCharacterContent(characterId).first() ?: return@launch
                val loadedArcs = characterUseCase.getCharacterArcs(characterId).first()

                saga = loadedSaga
                arcs = loadedArcs
                _character.value = loadedCharacter
                _genre.value = loadedSaga.data.genre

                _isReplying.value = true
                val opening = epilogueChatUseCase.openConversation(loadedSaga, loadedCharacter, loadedArcs)
                _isReplying.value = false

                opening?.text?.takeIf { it.isNotBlank() }?.let { openingLine ->
                    _messages.value = listOf(EpilogueMessage(text = openingLine, isUser = false))
                }
            }
        }

        fun sendMessage(text: String) {
            val trimmed = text.trim()
            if (trimmed.isEmpty() || _isReplying.value) return
            val currentSaga = saga ?: return
            val currentCharacter = _character.value ?: return

            _messages.value = _messages.value + EpilogueMessage(text = trimmed, isUser = true)

            viewModelScope.launch {
                _isReplying.value = true
                val reply =
                    epilogueChatUseCase.reply(
                        saga = currentSaga,
                        character = currentCharacter,
                        arcs = arcs,
                        conversationSoFar = _messages.value,
                        userMessage = trimmed,
                    )
                _isReplying.value = false

                reply?.text?.takeIf { it.isNotBlank() }?.let { replyText ->
                    _messages.value = _messages.value + EpilogueMessage(text = replyText, isUser = false)
                }
            }
        }
    }
