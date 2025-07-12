package com.ilustris.sagai.features.saga.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.domain.usecase.GetInputSuggestionsUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.CharacterInfo
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
        private val suggestionUseCase: GetInputSuggestionsUseCase,
    ) : ViewModel() {
        val state = MutableStateFlow<ChatState>(ChatState.Empty)

        val content = MutableStateFlow<SagaContent?>(null)
        val messages = MutableStateFlow<List<MessageContent>>(emptyList())
        val isGenerating = MutableStateFlow(false)
        val characters = MutableStateFlow<List<Character>>(emptyList())
        val snackBarMessage = MutableStateFlow<SnackBarState?>(null)
        val suggestions = MutableStateFlow<List<Suggestion>>(emptyList())

        private fun sendError(errorMessage: String) {
            viewModelScope.launch {
                snackBarMessage.value =
                    SnackBarState(
                        title = "Ocorreu um erro inesperado",
                        text = errorMessage,
                        redirectAction = Triple(ChatAction.RESEND, "Ok", null),
                    )
                isGenerating.value = false
                delay(15.seconds)
                snackBarMessage.value = null
            }
        }

        fun initChat(sagaId: String?) {
            state.value = ChatState.Loading
            observeSaga()
            observeContentUpdate()
            observeTrigger()
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
                        if (it.messages.size != messages.value.size && isGenerating.value.not()) {
                            generateSuggestions(it.messages)
                        }
                        messages.value =
                            it.messages.sortedByDescending { messageContent -> messageContent.message.timestamp }
                        characters.value = sortCharactersByMessageCount(it.characters, it.messages)
                        state.value = ChatState.Success

                        if (it.messages.isEmpty() && isGenerating.value.not()) {
                            generateIntroduction(it.data, it.mainCharacter)
                        }
                    }
                }
            }
        }

        private fun observeTrigger() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.endTrigger.collect {
                    if (it) {
                        content.value?.let { saga ->
                            messageUseCase
                                .generateEndingMessage(saga)
                                .onSuccessAsync { message ->
                                    sendMessage(message)
                                    sagaContentManager.endSaga()
                                }
                        }
                    }
                }
            }
        }

        private fun observeContentUpdate() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.contentUpdateMessages.collect {
                    sendMessage(it)
                }
            }
        }

        private fun generateIntroduction(
            saga: SagaData,
            character: Character?,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                isGenerating.value = true
                state.value = ChatState.Loading
                messageUseCase
                    .generateIntroMessage(saga, character)
                    .onSuccess {
                        sendMessage(
                            it.copy(
                                senderType = SenderType.NARRATOR,
                                characterId = character?.id,
                            ),
                            false,
                        )
                    }.onFailure {
                        sendError(it.message ?: "Unknown error")
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
                        SenderType.NEW_CHARACTER,
                        SenderType.NEW_CHAPTER,
                        -> null

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
                checkLoreUpdate()
            }
        }

        private fun generateSuggestions(messagesList: List<MessageContent>) {
            suggestions.value = emptyList()
            viewModelScope.launch(Dispatchers.IO) {
                content.value?.data?.let { saga ->
                    suggestionUseCase
                        .invoke(
                            messagesList.sortedBy { it.message.timestamp }.takeLast(10),
                            content.value?.mainCharacter,
                            saga,
                        ).onSuccess {
                            suggestions.value = it
                        }
                }
            }
        }

        private fun checkLoreUpdate() {
            viewModelScope.launch(Dispatchers.IO) {
                val currentSagaContent = content.value ?: return@launch
                val allMessages = messages.value

                val lastLoreReferenceId =
                    currentSagaContent.timelines.maxByOrNull { it.createdAt }?.messageReference
                val messagesSinceLastLore: List<MessageContent> =
                    if (lastLoreReferenceId != null) {
                        val indexOfLastLoreMessage =
                            allMessages.indexOfFirst { it.message.id == lastLoreReferenceId }
                        if (indexOfLastLoreMessage != -1) {
                            allMessages.subList(0, indexOfLastLoreMessage)
                        } else {
                            allMessages
                        }
                    } else {
                        allMessages
                    }

                if (messagesSinceLastLore.size >= UpdateRules.LORE_UPDATE_LIMIT && !isGenerating.value) {
                    isGenerating.value = true
                    val messagesToProcessForLore =
                        messagesSinceLastLore
                            .filter { it.message.senderType != SenderType.NEW_CHAPTER }
                            .take(UpdateRules.LORE_UPDATE_LIMIT)
                            .reversed()

                    val loreReferenceForUpdate = allMessages.firstOrNull()?.message

                    if (loreReferenceForUpdate != null && messagesToProcessForLore.isNotEmpty()) {
                        sagaContentManager
                            .updateLore(
                                reference = loreReferenceForUpdate,
                                messageSubList = messagesToProcessForLore,
                            ).onSuccess { newEvent ->
                                notifyLoreUpdate(newEvent)
                            }.onFailure {
                                isGenerating.value = false
                                sendError("Ocorreu um erro ao atualizar a história.")
                            }
                    } else {
                        isGenerating.value = false
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
                delay(20.seconds)
                snackBarMessage.value = null
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
                val characters = content.value!!.characters
                val directive = sagaContentManager.getDirective()
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
                        directive = directive,
                        lastMessages =
                            messages
                                .value
                                .reversed()
                                .takeLast(UpdateRules.LORE_UPDATE_LIMIT)
                                .map { m ->
                                    m.joinMessage()
                                },
                    ).onSuccess { genMessage ->
                        sendMessage(
                            genMessage.message.copy(
                                chapterId = null,
                                actId = null,
                            ),
                        )
                        isGenerating.value = false
                        if (genMessage.shouldCreateCharacter && genMessage.newCharacter != null) {
                            createCharacter(genMessage.newCharacter)
                        }
                        if (genMessage.shouldEndSaga) {
                            endSaga()
                        }
                    }.onFailure {
                        sendError(it.message ?: "Unknown error")
                    }
            }
        }

        private fun endSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.endSaga()
            }
        }

        fun createCharacter(newCharacter: Character) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager
                    .generateCharacter(
                        newCharacter.toJsonFormat(),
                    ).onSuccessAsync {
                        snackBarMessage.value =
                            SnackBarState(
                                "${it.name} juntou-se a história!",
                                it.backstory,
                                redirectAction = Triple(ChatAction.OPEN_CHARACTER, "Ver mais", it.id),
                            )
                        sagaContentManager.generateCharacterImage(
                            it,
                        )

                        delay(20.seconds)
                        snackBarMessage.value = null
                    }.onFailure {
                        sendError("Ocorreu um erro ao criar o personagem.")
                    }
            }
        }

        fun createCharacter(newCharacter: CharacterInfo) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager
                    .generateCharacter(
                        newCharacter.toJsonFormat(),
                    ).onSuccessAsync {
                        snackBarMessage.value =
                            SnackBarState(
                                "${it.name} juntou-se a história!",
                                it.backstory,
                                redirectAction = Triple(ChatAction.OPEN_CHARACTER, "Ver mais", it.id),
                            )
                        sagaContentManager.generateCharacterImage(
                            it,
                        )

                        delay(20.seconds)
                        snackBarMessage.value = null
                    }.onFailure {
                        sendError("Ocorreu um erro ao criar o personagem.")
                    }
            }
        }

        fun dismissSnackBar() {
            snackBarMessage.value = null
        }
    }
