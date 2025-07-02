package com.ilustris.sagai.features.saga.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.domain.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.joinMessage
import com.ilustris.sagai.features.timeline.data.model.Timeline
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
        val snackBarMessage = MutableStateFlow<SnackBarState?>(null)

        private fun sendError(errorMessage: String) {
            snackBarMessage.value =
                SnackBarState(
                    title = "Ocorreu um erro inesperado",
                    text = errorMessage,
                    redirectAction = Triple(ChatAction.RESEND, "Ok", null),
                )
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
                        messages.value = it.messages.sortedByDescending { messageContent -> messageContent.message.timestamp }
                        characters.value = sortCharactersByMessageCount(it.characters, it.messages)
                        state.value = ChatState.Success
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
                                it.message.timestamp
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
            viewModelScope.launch(Dispatchers.IO) {
                state.value = ChatState.Loading
                sagaContentManager.createAct().onSuccess {
                    launch(context = this.coroutineContext) {
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
                                sendError(it.message ?: "Unknown error")
                            }
                    }
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
                val sendType =
                    if (characterReference?.id == mainCharacter.id) SenderType.USER else message.senderType
                val speakerId =
                    when (sendType) {
                        SenderType.NARRATOR -> message.characterId
                        SenderType.NEW_CHARACTER, SenderType.NEW_CHAPTER -> null
                        SenderType.USER -> mainCharacter.id
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
            viewModelScope.launch(Dispatchers.IO) {
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

                if (messageSubList.size >= UpdateRules.LORE_UPDATE_LIMIT && isGenerating.value.not()) {
                    isGenerating.value = true
                    sagaContentManager
                        .updateLore(
                            reference = messageList.last().message,
                            messageSubList
                                .filter { it.message.senderType != SenderType.NEW_CHAPTER }
                                .takeLast(UpdateRules.LORE_UPDATE_LIMIT),
                        ).onSuccess {
                            notifyLoreUpdate(it)
                        }.onFailure {
                            sendError("Ocorreu um erro ao atualizar a história.")
                        }
                }
            }
        }

        private fun notifyLoreUpdate(newEvent: Timeline) {
            isGenerating.value = false
            viewModelScope.launch(Dispatchers.IO) {
                snackBarMessage.value =
                    SnackBarState(
                        "História atualizada",
                        newEvent.content,
                        redirectAction = Triple(ChatAction.OPEN_TIMELINE, "Ver mais", newEvent.id),
                    )
                delay(10.seconds)
                snackBarMessage.value = null
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
                    ).onSuccess {
                        updateMessage(
                            message.copy(
                                characterId = it.id,
                            ),
                        )
                        val previousMessage = messages.value[messages.value.lastIndex - 1].message
                        replyMessage(previousMessage)
                    }
                isGenerating.value = false
            }
        }

        private fun updateMessage(message: Message) {
            viewModelScope.launch(Dispatchers.IO) {
                messageUseCase.updateMessage(message)
            }
        }

        private fun lastEvents(): List<Timeline> {
            val saga = content.value ?: return emptyList()
            val lastChapter = saga.chapters.maxByOrNull { it.id }
            val events = saga.timelines
            val eventReference =
                lastChapter?.eventReference?.let { referenceId -> saga.timelines.find { it.id == referenceId } }
            return eventReference?.let {
                val referenceIndex = events.indexOf(it)
                events.subList(referenceIndex, events.size).takeLast(5)
            } ?: events
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
                        saga = saga,
                        chapter = content.value?.chapters?.lastOrNull(),
                        message = newMessage.joinMessage(),
                        lastEvents = lastEvents(),
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

        fun dismissSnackBar() {
            snackBarMessage.value = null
        }
    }
