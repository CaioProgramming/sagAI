package com.ilustris.sagai.features.saga.chat.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.utils.afterLast
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
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
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val characterUseCase: CharacterUseCase,
        private val chapterUseCase: ChapterUseCase,
    ) : ViewModel() {
        val state = MutableStateFlow<ChatState>(ChatState.Empty)

        val content = MutableStateFlow<SagaContent?>(null)
        val saga = MutableStateFlow<SagaData?>(null)
        val messages = MutableStateFlow<List<MessageContent>>(emptyList())
        val characters = MutableStateFlow<List<Character>>(emptyList())
        val mainCharacter = MutableStateFlow<Character?>(null)
        val isGenerating = MutableStateFlow(false)
        val loreUpdated = MutableStateFlow(false)

        fun initChat(sagaId: String?) {
            viewModelScope.launch(Dispatchers.IO) {
                if (saga.value == null) {
                    state.value = ChatState.Loading
                    sagaId?.let { id ->
                        sagaHistoryUseCase.getSagaById(id.toInt()).collect {
                            if (it == null) {
                                state.value =
                                    ChatState.Error(
                                        "Saga not found. Please select a valid saga.",
                                    )
                            } else {
                                content.value = it
                                saga.value = it.saga
                                mainCharacter.value = it.mainCharacter
                                characters.value = it.characters
                                loadSagaMessages(it)
                                it.mainCharacter?.let { character ->
                                    if (character.image.isEmpty()) {
                                        generateCharacterImage(character, it.saga)
                                    }
                                }
                            }
                        }
                    } ?: run {
                        state.value =
                            ChatState.Error(
                                "Saga not found. Please select a valid saga.",
                            )
                        return@launch
                    }
                }
            }
        }

        private fun generateCharacterImage(
            character: Character,
            saga: SagaData,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                characterUseCase
                    .generateCharacterImage(character, saga)
                    .onSuccess {
                        mainCharacter.value = it
                    }.onFailure {
                        Log.e(
                            javaClass.simpleName,
                            "generateCharacterImage: Error generating character icon",
                        )
                    }
            }
        }

        private fun loadSagaMessages(sagaContent: SagaContent) {
            viewModelScope.launch(Dispatchers.IO) {
                if (sagaContent.messages.isEmpty()) {
                    generateIntroduction(sagaContent.saga, sagaContent.mainCharacter)
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
                        state.value = ChatState.Success
                    }
                }
            }
        }

        private fun observeChapterBreak() {
            viewModelScope.launch(Dispatchers.IO) {
                val sagaContent = content.value
                if (sagaContent == null) return@launch

                val mappedMessages = messages.value
                val chapters = sagaContent.chapters
                val chapterMessagesIds =
                    sagaContent.messages
                        .filter { it.senderType == SenderType.NEW_CHAPTER }
                        .mapNotNull { it.chapterId }

                if (chapterMessagesIds.isEmpty()) {
                    createNewChapter(mappedMessages)
                } else {
                    val lastChapterMessage =
                        mappedMessages.findLast { it.message.senderType == SenderType.NEW_CHAPTER }

                    if (lastChapterMessage != null) {
                        val chapterMessages =
                            mappedMessages.afterLast { it.message.id == lastChapterMessage.message.id }
                        if (chapterMessages.size >= 100) {
                            createNewChapter(
                                chapterMessages.takeLast(100),
                                chapters,
                            )
                        }
                    } else {
                        createNewChapter(mappedMessages.takeLast(100), chapters)
                    }
                }
            }
        }

        private fun createNewChapter(
            messageList: List<MessageContent>,
            chapters: List<Chapter> = emptyList(),
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                if (messageList.size >= 100 && isGenerating.value.not()) {
                    val messageReference = messageList.last()
                    isGenerating.value = true
                    chapterUseCase
                        .generateChapter(
                            saga.value!!,
                            messageReference.message.id,
                            messageList.map { it.joinMessage() },
                            chapters,
                            characters.value,
                        ).onSuccess {
                            finishSaveChapter(it)
                        }.onFailure {
                            state.value =
                                ChatState.Error("Failed to generate chapter break: ${it.message ?: "Unknown error"}")
                        }
                    delay(3.seconds)
                    isGenerating.value = false
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
                            sagaId = saga.value!!.id,
                            chapterId = chapter.id,
                        ),
                    ).also {
                        updateChapter(
                            chapter.copy(
                                messageReference = it.id,
                            ),
                        )
                    }
                isGenerating.value = false
            }
        }

        private fun updateChapter(chapter: Chapter) {
            viewModelScope.launch(Dispatchers.IO) {
                chapterUseCase.updateChapter(
                    chapter,
                )
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
                                characterId = null,
                            ),
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
                    sagaId = saga.value?.id ?: 0,
                    characterId = if (sendType == SenderType.NEW_CHARACTER) null else mainCharacter.value?.id,
                )
            sendMessage(message, true)
        }

        fun sendMessage(
            message: Message,
            fromPlayer: Boolean = false,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val characterReference =
                    characters.value.find {
                        message.speakerName?.contains(it.name, true) == true ||
                            message.characterId == mainCharacter.value?.id
                    }
                messageUseCase
                    .saveMessage(
                        message.copy(
                            sagaId = saga.value!!.id,
                            characterId = characterReference?.id,
                        ),
                    ).also {
                        if (messages.value.isNotEmpty()) {
                            checkLoreUpdate()
                            observeChapterBreak()
                        }

                        when (message.characterId == mainCharacter.value?.id) {
                            true -> {
                                isGenerating.value = true
                                delay(2.seconds)
                                replyMessage(it)
                            }

                            else -> doNothing()
                        }

                        if (message.senderType == SenderType.NEW_CHARACTER) {
                            generateCharacter(it)
                        }
                    }
                isGenerating.value = false
            }
        }

        private fun checkLoreUpdate() {
            viewModelScope.launch {
                val currentSaga = saga.value
                val messageList = messages.value
                val lastLoreMessage =
                    messageList.find { it.message.id == currentSaga?.lastLoreReference }
                val messageSubList =
                    lastLoreMessage?.let {
                        messageList.subList(messageList.indexOf(it), messageList.size)
                    } ?: messageList

                if (messageSubList.size >= LORE_UPDATE_THRESHOLD ||
                    messageSubList.last().message.senderType == SenderType.NEW_CHAPTER
                ) {
                    val chapterMessage =
                        messageSubList.findLast { it.message.senderType == SenderType.NEW_CHAPTER }
                    val messageReference =
                        if (messageSubList.size >= LORE_UPDATE_THRESHOLD) {
                            messageSubList.last().message.id
                        } else {
                            (
                                chapterMessage?.message?.id
                                    ?: messageList.last().message.id
                            )
                        }
                    sagaHistoryUseCase
                        .generateLore(
                            currentSaga!!,
                            mainCharacter.value!!,
                            messageReference,
                            messageSubList.map { it.joinMessage().formatToString() },
                        ).onSuccess {
                            notifyLoreUpdate()
                        }
                }
            }
        }

        private fun notifyLoreUpdate() {
            viewModelScope.launch {
                loreUpdated.value = true
                delay(10.seconds)
                loreUpdated.value = false
            }
        }

        private fun generateCharacter(message: Message) {
            viewModelScope.launch(Dispatchers.IO) {
                isGenerating.value = true
                characterUseCase
                    .generateCharacter(
                        saga.value!!,
                        message.text,
                    ).onSuccess {
                        updateMessage(
                            message.copy(
                                characterId = it.id,
                            ),
                        )
                    }.onFailure {
                        Log.e(
                            javaClass.simpleName,
                            "generateCharacter: Error generating new character.",
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
            viewModelScope.launch(Dispatchers.IO) {
                saga.value?.let { saga ->
                    messages.value.find { it.message.id == message.id }?.let {
                        messageUseCase
                            .generateMessage(
                                saga = saga,
                                chapter = content.value?.chapters?.lastOrNull(),
                                message = it.joinMessage(),
                                mainCharacter = mainCharacter.value!!,
                                lastMessages =
                                    messages.value
                                        .takeLast(25)
                                        .map { m ->
                                            m.joinMessage()
                                        },
                                characters =
                                    characters.value.filter { c ->
                                        c.id != mainCharacter.value?.id
                                    },
                            ).onSuccess { reply ->
                                sendMessage(
                                    reply.copy(
                                        characterId = null,
                                        chapterId = null,
                                    ),
                                )
                            }.onFailure {
                                state.value =
                                    ChatState.Error("Failed to generate reply: ${it.message ?: "Unknown error"}")
                            }
                    }
                }
            }
        }
    }

private const val LORE_UPDATE_THRESHOLD = 30
