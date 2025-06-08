package com.ilustris.sagai.features.chat.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToString
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
import java.io.File
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
        private val fileHelper: FileHelper,
    ) : ViewModel() {
        val state = MutableStateFlow<ChatState>(ChatState.Empty)

        val saga = MutableStateFlow<SagaData?>(null)
        val messages = MutableStateFlow<List<MessageContent>>(emptyList())
        val characters = MutableStateFlow<List<Character>>(emptyList())
        val mainCharacter = MutableStateFlow<Character?>(null)

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

        private suspend fun observeChapterBreak(
            saga: SagaContent,
            mappedMessages: List<MessageContent>,
        ) {
            val chapters = saga.chapters
            val messages = saga.messages
            val chapterMessagesIds =
                saga.messages
                    .filter { it.senderType == SenderType.NEW_CHAPTER }
                    .mapNotNull { it.chapterId }

            if (chapterMessagesIds.isEmpty() || chapters.isEmpty() && messages.isNotEmpty()) {
                createNewChapter(mappedMessages, chapters.lastOrNull())
            } else {
                val lastChapterKey = chapterMessagesIds.last()
                val lastChapterMessage = saga.messages.findLast { it.id == lastChapterKey }
                if (lastChapterMessage != null) {
                    val messageIndex =
                        mappedMessages.indexOfLast { it.message.id == lastChapterMessage.id }
                    createNewChapter(
                        mappedMessages.subList(messageIndex + 1, messages.size).takeLast(100),
                        lastChapter = chapters.find { it.messageReference == lastChapterMessage.id },
                    )
                } else {
                    createNewChapter(mappedMessages.takeLast(100))
                }
            }
        }

        private suspend fun createNewChapter(
            messageList: List<MessageContent>,
            lastChapter: Chapter? = null,
        ) {
            if (messageList.size > 100) {
                chapterUseCase
                    .generateChapter(
                        saga.value!!,
                        messageList.map { it.joinMessage() },
                    ).onSuccess {
                        saveChapter(
                            it.copy(
                                messageReference = messageList.last().message.id,
                            ),
                        )
                    }.onFailure {
                        state.value =
                            ChatState.Error("Failed to generate chapter break: ${it.message ?: "Unknown error"}")
                    }
            }
        }

        private fun saveChapter(chapter: Chapter) {
            viewModelScope.launch {
                chapterUseCase
                    .generateChapterCover(
                        chapter,
                        saga.value!!.genre,
                    ).run {
                        val file =
                            (this as? RequestResult.Success<ByteArray>)?.success?.let {
                                fileHelper.saveToCache(
                                    chapter.title.trim().lowercase(),
                                    it.value,
                                )
                            }
                        finishSaveChapter(
                            chapter,
                            file,
                        )
                    }
            }
        }

        private fun finishSaveChapter(
            chapter: Chapter,
            coverFile: File?,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val filePath = coverFile?.absolutePath ?: emptyString()
                chapterUseCase
                    .saveChapter(
                        chapter.copy(
                            coverImage = filePath,
                            sagaId = saga.value!!.id,
                        ),
                    ).also {
                        messageUseCase
                            .saveMessage(
                                Message(
                                    id = 0,
                                    text = "Capitulo '${chapter.title}' iniciado.",
                                    senderType = SenderType.NEW_CHAPTER,
                                    sagaId = saga.value!!.id,
                                    timestamp = System.currentTimeMillis(),
                                    chapterId = it.toInt(),
                                ),
                            ).also {
                                updateChapter(
                                    chapter.copy(
                                        messageReference = it.id,
                                    ),
                                )
                            }
                    }
            }
        }

        private fun updateChapter(chapter: Chapter) {
            viewModelScope.launch(Dispatchers.IO) {
                chapterUseCase.updateChapter(
                    chapter,
                )
            }
        }

        private fun checkIfNeedsNarrationBreak(messageList: List<MessageContent>) {
            val lastNarrationIndex = messageList.indexOfLast { it.message.senderType == SenderType.NARRATOR }
            val countSinceNarration =
                messages.value
                    .subList(lastNarrationIndex + 1, messages.value.size)
                    .size

            if (countSinceNarration > 25) {
                viewModelScope.launch {
                    messageUseCase
                        .generateNarratorBreak(
                            saga.value!!,
                            messageList
                                .filter {
                                    it.message.senderType != SenderType.NARRATOR
                                }.map {
                                    it.joinMessage().formatToString()
                                },
                        ).onSuccess {
                            sendMessage(it.copy(senderType = SenderType.NARRATOR))
                        }.onFailure {
                            state.value =
                                ChatState.Error("Failed to generate narration break: ${it.message ?: "Unknown error"}")
                        }
                }
            }
        }

        private fun generateIntroduction(
            saga: SagaData,
            character: Character?,
        ) {
            viewModelScope.launch {
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

        fun sendInput(text: String) {
            val message =
                Message(
                    id = 0,
                    text = text,
                    senderType = SenderType.USER,
                    sagaId = saga.value?.id ?: 0,
                    timestamp = System.currentTimeMillis(),
                    characterId = mainCharacter.value?.id,
                )
            sendMessage(message)
        }

        fun sendMessage(message: Message) {
            viewModelScope.launch(Dispatchers.IO) {
                messageUseCase
                    .saveMessage(
                        message.copy(
                            id = 0,
                            sagaId = saga.value!!.id,
                            timestamp = Calendar.getInstance().timeInMillis,
                        ),
                    ).also {
                        when (message.senderType) {
                            SenderType.USER -> {
                                delay(2.seconds)
                                replyMessage(it)
                            }

                            SenderType.NEW_CHARACTER -> {
                                generateCharacter(it)
                            }
                            else -> doNothing()
                        }
                    }
            }
        }

        private fun generateCharacter(message: Message) {
            viewModelScope.launch(Dispatchers.IO) {
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
                        Log.e(javaClass.simpleName, "generateCharacter: Error generating new character.")
                    }
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
