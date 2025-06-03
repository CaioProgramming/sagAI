package com.ilustris.sagai.features.chat.ui.presentation

import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.home.data.model.SagaData

sealed interface ChatState {
    data object Loading : ChatState

    data class Success(
        val sagaData: SagaData,
        val messages: List<Message>
    ) : ChatState

    data class Error(val message: String) : ChatState

    data object Empty : ChatState
}