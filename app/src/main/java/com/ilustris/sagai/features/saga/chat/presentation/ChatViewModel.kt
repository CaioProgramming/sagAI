package com.ilustris.sagai.features.saga.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.manager.LORE_UPDATE_THRESHOLD
import com.ilustris.sagai.features.saga.chat.domain.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.domain.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.joinMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(PublicPreviewAPI::class)
@HiltViewModel
class ChatViewModel
    @Inject
    constructor(
        private val messageUseCase: MessageUseCase,
        private val sagaContentManager: SagaContentManager,
    ) : ViewModel() {
        val state = MutableStateFlow<ChatState>(ChatState.Empty)

        val content = MutableStateFlow<SagaContent?>(null)
        val messages = MutableStateFlow<List<MessageContent>>(emptyList())
        val isGenerating = MutableStateFlow(false)
        val characters = MutableStateFlow<List<Character>>(emptyList())
        val snackbarMessage = MutableStateFlow<String?>(null)

        private fun sendError(errorMessage: String) {
            snackbarMessage.value = "Ocorreu um erro inesperado $errorMessage"
            isGenerating.value = false
        }

        fun initChat(sagaId: String?) {
            state.value = ChatState.Loading
            observeSaga()
            viewModelScope.launch(Dispatchers.IO) {
                sagaId?.let {
                    sagaContentManager.loadSaga(it)
                } ?: run {
                    sendError(
                        "Saga not found. Please select a valid saga.",
                    )
                }
            }
        }

        private fun observeSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.content.collect {
                    if (it != null) {
                        content.value = it
                        loadSagaMessages(it)
                        characters.value = sortCharactersByMessageCount(it.characters, it.messages)
                    }
                }
            }
        }

        private fun loadSagaMessages(sagaContent: SagaContent) {
            viewModelScope.launch(Dispatchers.IO) {
                if (sagaContent.messages.isEmpty()) {
                    generateIntroduction(sagaContent.data, sagaContent.mainCharacter)
                } else {
                    val mappedMessages =
                        sagaContent.messages
                            .sortedByDescending {
                                it.timestamp
                            }.map {
                                messageUseCase.getMessageDetail(it.id)
                            }
                    this@ChatViewModel.messages.value = mappedMessages.reversed()

                    if (state.value != ChatState.Success) {
                        delay(500)
                        state.value = ChatState.Success
                    }
                }
            }
        }

        private fun finishSaveChapter(chapter: Chapter) {
            viewModelScope.launch(Dispatchers.IO) {
                messageUseCase
                    .saveMessage(
                        Message(
                            text = "Capitulo '${chapter.title}' iniciado.",
                            senderType = SenderType.NEW_CHAPTER,
                            sagaId = content.value!!.data.id,
                            chapterId = chapter.id,
                        ),
                    )
                isGenerating.value = false
            }
        }

        private fun generateIntroduction(
            saga: SagaData,
            character: Character?,
        ) {
            viewModelScope.launch {
                state.value = ChatState.Loading
                messageUseCase
                    .generateIntroMessage(saga, character)
                    .onSuccess {
                        sendMessage(
                            it.copy(
                                senderType = SenderType.NARRATOR,
                                characterId = character?.id,
                            ),
                            true,
                        )
                    }.onFailure {
                        state.value =
                            ChatState.Error("Failed to generate introduction: ${it.message ?: "Unknown error"}")
                    }
            }
        }

        fun sendInput(
            text: String,
            sendType: SenderType,
        ) {
            val message =
                Message(
                    text = text,
                    speakerName = null,
                    senderType = sendType,
                    characterId = content.value?.mainCharacter?.id,
                )
            sendMessage(message, true)
        }

        private fun sendMessage(
            message: Message,
            isFromUser: Boolean = false,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val saga = content.value ?: return@launch
                val mainCharacter = content.value!!.mainCharacter ?: return@launch
                val characters = content.value!!.characters
                val characterReference =
                    characters.find {
                        message.speakerName?.contains(it.name, true) == true ||
                            message.characterId == mainCharacter.id
                    }
                val sendType = message.senderType
                val speakerId =
                    when (sendType) {
                        SenderType.NARRATOR -> message.characterId
                        SenderType.NEW_CHARACTER, SenderType.NEW_CHAPTER -> null
                        SenderType.USER, SenderType.THOUGHT, SenderType.ACTION -> mainCharacter.id
                        else -> characterReference?.id
                    }
                messageUseCase
                    .saveMessage(
                        message.copy(
                            sagaId = saga.data.id,
                            characterId = speakerId,
                        ),
                    ).onSuccess {
                        handleNewMessage(it, isFromUser)
                    }.onFailure {
                        sendError("Ocorreu um erro ao salvar a mensagem")
                    }
            }
        }

        private fun handleNewMessage(
            it: Message,
            isFromUser: Boolean,
        ) {
            when (isFromUser && it.senderType != SenderType.NEW_CHARACTER) {
                true -> {
                    replyMessage(it)
                }

                else -> doNothing()
            }

            if (it.senderType == SenderType.NEW_CHARACTER) {
                generateCharacter(it)
            }
            checkLoreUpdate()
        }

        private fun checkLoreUpdate() {
            viewModelScope.launch(Dispatchers.IO) {
                val currentSaga = content.value ?: return@launch
                val messageList = messages.value
                val lastLoreItem = currentSaga.timelines.maxByOrNull { it.createdAt }
                val lastLoreMessage =
                    messageList.find { it.message.id == lastLoreItem?.messageReference }
                val messageSubList =
                    lastLoreMessage?.let {
                        messageList.subList(messageList.indexOf(it), messageList.size)
                    } ?: messageList

                if (messageSubList.size >= LORE_UPDATE_THRESHOLD && isGenerating.value.not()) {
                    isGenerating.value = true
                    sagaContentManager
                        .updateLore(
                            reference = messageList.last().message,
                            messageSubList
                                .filter { it.message.senderType != SenderType.NEW_CHAPTER }
                                .takeLast(LORE_UPDATE_THRESHOLD),
                        ).onSuccess {
                            notifyLoreUpdate()
                        }.onFailure {
                            sendError("Ocorreu um erro ao atualizar a história.")
                        }
                }
            }
        }

        private fun notifyLoreUpdate() {
            isGenerating.value = false
            viewModelScope.launch(Dispatchers.IO) {
                snackbarMessage.value = "História atualizada"
                delay(10.seconds)
                snackbarMessage.value = null

                sagaContentManager
                    .checkForChapter()
                    .onSuccess {
                        isGenerating.value = true
                        finishSaveChapter(it)
                    }.onFailure {
                        // sendError(it.message ?: "Unknown error")
                        isGenerating.value = false
                    }
            }
        }

        private fun generateCharacter(message: Message) {
            viewModelScope.launch(Dispatchers.IO) {
                isGenerating.value = true
                sagaContentManager
                    .generateCharacter(
                        message,
                    ).also {
                        updateMessage(
                            message.copy(
                                characterId = it?.id,
                            ),
                        )
                    }
                isGenerating.value = false
            }
        }

        private fun updateMessage(message: Message) {
            viewModelScope.launch(Dispatchers.IO) {
                messageUseCase.updateMessage(message)
            }
        }

        private fun replyMessage(message: Message) {
            isGenerating.value = true
            viewModelScope.launch(Dispatchers.IO) {
                val saga = content.value ?: return@launch
                val mainCharacter = content.value!!.mainCharacter ?: return@launch
                val characters = content.value!!.characters
                val newMessage =
                    MessageContent(
                        message = message,
                        character = characters.find { it.id == message.characterId },
                    )

                messageUseCase
                    .generateMessage(
                        saga = saga.data,
                        chapter = content.value?.chapters?.lastOrNull(),
                        message = newMessage.joinMessage(),
                        mainCharacter = mainCharacter,
                        characters = saga.characters,
                        lastMessages =
                            messages.value
                                .takeLast(25)
                                .map { m ->
                                    m.joinMessage()
                                },
                    ).onSuccess { genMessage ->
                        val characterReference =
                            if (genMessage.speakerName == null) {
                                null
                            } else {
                                characters
                                    .find {
                                        it.name.contains(genMessage.speakerName, true)
                                    }?.id
                            }

                        sendMessage(
                            genMessage.copy(
                                characterId = characterReference,
                                chapterId = null,
                            ),
                        )
                        isGenerating.value = false
                    }.onFailure {
                        sendError(it.message ?: "Unknown error")
                    }
            }
        }
    }
