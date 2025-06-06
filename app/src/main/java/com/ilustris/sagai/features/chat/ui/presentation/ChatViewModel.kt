package com.ilustris.sagai.features.chat.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.MessageContent
import com.ilustris.sagai.features.chat.data.model.SenderType
import com.ilustris.sagai.features.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
@HiltViewModel
class ChatViewModel
    @Inject
    constructor(
        private val messageUseCase: MessageUseCase,
        private val sagaHistoryUseCase: SagaHistoryUseCase,
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
                    sagaId?.let {
                        sagaHistoryUseCase.getSagaById(it.toInt()).collect {
                            if (it == null) {
                                state.value =
                                    ChatState.Error(
                                        "Saga not found. Please select a valid saga.",
                                    )
                            } else {
                                saga.value = it.saga
                                mainCharacter.value = it.mainCharacter
                                loadSagaMessages(it)
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

        private fun loadSagaMessages(sagaContent: SagaContent) {
            viewModelScope.launch(Dispatchers.IO) {
                if (sagaContent.messages.isEmpty()) {
                    generateIntroduction(sagaContent.saga, sagaContent.mainCharacter)
                } else {
                    this@ChatViewModel.messages.value =
                        sagaContent.messages
                            .map {
                                messageUseCase.getMessageDetail(it.id)
                            }.reversed()

                    if (state.value != ChatState.Success) {
                        state.value = ChatState.Success
                    }
                    checkIfNeedsNarrationBreak(sagaContent.messages)
                    observeChapterBreak(sagaContent)
                }
            }
        }

        private suspend fun observeChapterBreak(saga: SagaContent) {
            val chapters = saga.chapters
            val messages = saga.messages
            val chapterMessagesIds =
                saga.messages
                    .filter { it.senderType == SenderType.NEW_CHAPTER }
                    .mapNotNull { it.chapterId }

            if (chapterMessagesIds.isEmpty() || chapters.isEmpty()) {
                createNewChapter(saga.messages)
            } else {
                val lastChapterKey = chapterMessagesIds.last()
                val lastChapterMessage = saga.messages.findLast { it.id == lastChapterKey }
                if (lastChapterMessage != null) {
                    val messageIndex = messages.indexOf(lastChapterMessage)
                    createNewChapter(messages.subList(messageIndex + 1, messages.size))
                } else {
                    createNewChapter(messages)
                }
            }

            if (saga.chapters.isNotEmpty()) {
                val lastChapterMessage =
                    saga.messages.findLast {
                        it.senderType == SenderType.NEW_CHAPTER && it.chapterId == saga.chapters.last().id
                    }

                if (lastChapterMessage != null) {
                    val messageList =
                        saga.messages.subList(
                            saga.messages.indexOf(lastChapterMessage) + 1,
                            saga.messages.size,
                        )

                    createNewChapter(
                        messageList,
                        saga.chapters.find { it.id == lastChapterMessage.chapterId },
                    )
                }
            } else {
                createNewChapter(
                    saga.messages.takeLast(100),
                )
            }
        }

        private suspend fun createNewChapter(
            messageList: List<Message>,
            lastChapter: Chapter? = null,
        ) {
            if (messageList.size > 100) {
                chapterUseCase
                    .generateChapter(
                        saga.value!!,
                        messageList,
                    ).onSuccess {
                        saveChapter(
                            it.copy(
                                messageReference = messageList.lastOrNull()?.id ?: 0,
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
            coverFile: java.io.File?,
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
                                        messageReference = it.toInt(),
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

        private fun checkIfNeedsNarrationBreak(messageList: List<Message>) {
            val lastNarrationIndex = messageList.indexOfLast { it.senderType == SenderType.NARRATOR }
            val countSinceNarration =
                messages.value
                    .subList(lastNarrationIndex + 1, messages.value.size)
                    .size

            if (countSinceNarration > 25) {
                viewModelScope.launch {
                    messageUseCase
                        .generateNarratorBreak(
                            saga.value!!,
                            messageList.filter { it.senderType != SenderType.NARRATOR },
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
            if (text.isBlank()) return
            val message =
                Message(
                    id = 0,
                    text = text,
                    senderType = SenderType.USER,
                    sagaId = saga.value?.id ?: 0,
                    timestamp = System.currentTimeMillis(),
                )
            sendMessage(message)
        }

        fun sendMessage(message: Message) {
            viewModelScope.launch(Dispatchers.IO) {
                messageUseCase.saveMessage(
                    message.copy(
                        id = 0,
                        sagaId = saga.value!!.id,
                        timestamp = Calendar.getInstance().timeInMillis,
                    ),
                )
                if (message.senderType == SenderType.USER) {
                    replyMessage(message)
                }
            }
        }

        private fun replyMessage(message: Message) {
            viewModelScope.launch(Dispatchers.IO) {
                saga.value?.let { saga ->
                    messages.value.find { it.message.id == message.id }?.let {
                        val message = it.message
                        val character = it.character
                        messageUseCase
                            .generateMessage(
                                saga,
                                (character?.name ?: message.senderType.name) to message.text,
                                messages.value
                                    .take(10)
                                    .map {
                                        (it.character?.name ?: it.message.senderType.name) to it.message.text
                                    },
                            ).onSuccess { reply ->
                                sendMessage(reply.copy(senderType = SenderType.BOT))
                            }.onFailure {
                                state.value =
                                    ChatState.Error("Failed to generate reply: ${it.message ?: "Unknown error"}")
                            }
                    }
                }
            }
        }
    }
