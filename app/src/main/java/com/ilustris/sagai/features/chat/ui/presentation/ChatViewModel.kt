package com.ilustris.sagai.features.chat.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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

        fun getMessages(sagaId: String) {
            viewModelScope.launch {
                state.value = ChatState.Loading
                sagaHistoryUseCase.getSagaById(sagaId.toInt()).onSuccess {
                    loadSagaMessages(it)
                    saga.value = it
                }
            }
        }

        private fun loadSagaMessages(saga: SagaData) {
            viewModelScope.launch {
                messageUseCase.getMessages(saga.id).collect { messages ->
                    state.value =
                        if (messages.isEmpty()) {
                            ChatState.Empty
                        } else {
                            ChatState.Success(saga, messages)
                        }
                }
            }
        }

        fun sendMessage(string: String) {
        }
    }
