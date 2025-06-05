package com.ilustris.sagai.features.chat.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.SenderType
import com.ilustris.sagai.features.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ChatViewModel
    @Inject
    constructor(
        private val messageUseCase: MessageUseCase,
        private val sagaHistoryUseCase: SagaHistoryUseCase,
    ) : ViewModel() {
        val state = MutableStateFlow<ChatState>(ChatState.Empty)
        val saga = MutableStateFlow<SagaData?>(null)
        val messages = MutableStateFlow<List<Message>>(emptyList())

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
                messageUseCase.getMessages(saga.id).collect {
                    if (it.isEmpty()) {
                        generateIntroduction(saga)
                    }
                    this@ChatViewModel.messages.value = it.reversed()
                    if (state.value != ChatState.Success) {
                        state.value = ChatState.Success
                    }
                    checkIfNeedsNarrationBreak(it)
                }
            }
        }

        private fun chapterBreak(messageList: List<Message>) {
            val lastNarrationIndex = messageList.indexOfLast { it.senderType == SenderType.NARRATOR }
            val countSinceNarration =
                messages.value
                    .subList(lastNarrationIndex + 1, messages.value.size)
                    .size

            if (countSinceNarration > 100) {
                viewModelScope.launch {
                    messageUseCase
                        .generateNarratorBreak(
                            saga.value!!,
                            messageList.filter { it.senderType != SenderType.NARRATOR },
                        ).onSuccess {
                            sendMessage(it.copy(senderType = SenderType.NARRATOR))
                        }.onFailure {
                            state.value =
                                ChatState.Error("Failed to generate chapter break: ${it.message ?: "Unknown error"}")
                        }
                }
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
                        .generateMessage(it, message, messages.value.takeLast(5))
                        .onSuccess { reply ->
                            sendMessage(reply.copy(senderType = SenderType.BOT))
                        }.onFailure {
                            state.value =
                                ChatState.Error("Failed to generate reply: ${it.message ?: "Unknown error"}")
                        }
                }
            }
        }
    }
