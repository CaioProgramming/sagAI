package com.ilustris.sagai.features.chat.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.MessageContent
import com.ilustris.sagai.features.chat.data.model.SenderType
import com.ilustris.sagai.features.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.last
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
                                saga.value = it
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

        private fun loadSagaMessages(saga: SagaData) {
            viewModelScope.launch(Dispatchers.IO) {
                messageUseCase.getMessages(saga.id).collectLatest {
                    if (it.isEmpty()) {
                        generateIntroduction(saga)
                    }
                    val mappedMessages =
                        it.map {
                            val content =
                                if (it.senderType == SenderType.NEW_CHAPTER) {
                                    chapterUseCase.getChapterBySagaAndMessageId(
                                        saga.id,
                                        it.id,
                                    )
                                } else {
                                    null
                                }
                            MessageContent(
                                it,
                                content,
                            )
                        }
                    this@ChatViewModel.messages.value = mappedMessages.reversed()
                    if (state.value != ChatState.Success) {
                        state.value = ChatState.Success
                    }
                    checkIfNeedsNarrationBreak(it)
                    observeChapterBreak(saga, it)
                }
            }
        }

        private suspend fun observeChapterBreak(
            sagaData: SagaData,
            messageList: List<Message>,
        ) {
            chapterUseCase.getChaptersBySagaId(sagaData.id).collect { chapters ->
                if (chapters.isNotEmpty()) {
                    val lastChapter = chapters.last()
                    messageList
                        .find {
                            it.id == lastChapter.messageReference
                        }?.let {
                            val messageIndex = messageList.indexOf(it)
                            if (messageIndex != -1) {
                                val chapterMessages =
                                    messageList
                                        .subList(messageIndex + 1, messages.value.size)
                                        .filter {
                                            it.senderType != SenderType.NARRATOR &&
                                                it.senderType != SenderType.NEW_CHAPTER
                                        }

                                if (chapterMessages.size > 100) {
                                    createNewChapter(
                                        chapterMessages.takeLast(100),
                                    )
                                }
                            }
                        }
                } else {
                    // If no chapters exist, create a new chapter with the first messages
                    if (messageList.size > 100) {
                        createNewChapter(
                            messageList
                                .filter {
                                    it.senderType != SenderType.NARRATOR &&
                                        it.senderType != SenderType.NEW_CHAPTER
                                }.takeLast(100),
                        )
                    }
                }
            }
        }

        private suspend fun createNewChapter(messageList: List<Message>) {
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

        private fun generateIntroduction(saga: SagaData) {
            viewModelScope.launch {
                messageUseCase
                    .generateIntroMessage(saga)
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
                saga.value?.let {
                    messageUseCase
                        .generateMessage(
                            it,
                            message,
                            messages.value
                                .map {
                                    it.message
                                }.take(10),
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
