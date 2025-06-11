package com.ilustris.sagai.features.chat.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.MessageContent
import com.ilustris.sagai.features.chat.data.model.SenderType
import com.ilustris.sagai.features.chat.data.model.joinMessage
import com.ilustris.sagai.features.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.newsaga.data.model.Genre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
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
                                        generateCharacterImage(character, it.saga.genre)
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
            genre: Genre,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                characterUseCase
                    .generateCharacterImage(character, genre)
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
                            }.reversed()
                            .map {
                                messageUseCase.getMessageDetail(it.id)
                            }
                    this@ChatViewModel.messages.value = mappedMessages.reversed()

                    if (state.value != ChatState.Success) {
                        state.value = ChatState.Success
                    }
                    if (mappedMessages.isNotEmpty()) {
                        observeChapterBreak(sagaContent, mappedMessages)
                    }
                }
            }
        }

        private fun observeChapterBreak(
            saga: SagaContent,
            mappedMessages: List<MessageContent>,
        ) {
            val chapters = saga.chapters
            val messages = saga.messages
            val chapterMessagesIds =
                saga.messages
                    .filter { it.senderType == SenderType.NEW_CHAPTER }
                    .mapNotNull { it.chapterId }

            if (chapterMessagesIds.isEmpty()) {
                createNewChapter(mappedMessages)
            } else {
                val lastChapterMessage =
                    mappedMessages.findLast { it.message.senderType == SenderType.NEW_CHAPTER }
                if (lastChapterMessage != null) {
                    val messageIndex = mappedMessages.indexOf(lastChapterMessage)
                    createNewChapter(
                        mappedMessages.subList(messageIndex + 1, messages.size).takeLast(100),
                        chapters,
                    )
                } else {
                    createNewChapter(mappedMessages.takeLast(100), chapters)
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
                            id = 0,
                            text = "Capitulo '${chapter.title}' iniciado.",
                            senderType = SenderType.NEW_CHAPTER,
                            sagaId = saga.value!!.id,
                            timestamp = System.currentTimeMillis(),
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
                        sendMessage(it.copy(senderType = SenderType.NARRATOR))
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
                    id = 0,
                    text = text,
                    senderType = sendType,
                    sagaId = saga.value?.id ?: 0,
                    timestamp = System.currentTimeMillis(),
                    characterId = mainCharacter.value?.id,
                )
            sendMessage(message)
        }

        fun sendMessage(message: Message) {
            viewModelScope.launch(Dispatchers.IO) {
                val characterReference =
                    characters.value.find {
                        message.speakerName?.contains(it.name, true) == true ||
                            message.characterId == mainCharacter.value?.id
                    }
                messageUseCase
                    .saveMessage(
                        message.copy(
                            id = 0,
                            sagaId = saga.value!!.id,
                            timestamp = Calendar.getInstance().timeInMillis,
                            characterId = characterReference?.id,
                        ),
                    ).also {
                        when (message.characterId == mainCharacter.value?.id) {
                            true -> {
                                delay(2.seconds)
                                replyMessage(it)
                            }
                            else -> {
                                if (message.senderType == SenderType.NEW_CHARACTER) {
                                    generateCharacter(it)
                                }
                            }
                        }
                    }
                isGenerating.value = false
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
                        isGenerating.value = true
                        messageUseCase
                            .generateMessage(
                                saga = saga,
                                chapter = content.value?.chapters?.lastOrNull(),
                                message = it.joinMessage(),
                                mainCharacter = mainCharacter.value!!,
                                lastMessages =
                                    messages.value
                                        .reversed()
                                        .takeLast(10)
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
