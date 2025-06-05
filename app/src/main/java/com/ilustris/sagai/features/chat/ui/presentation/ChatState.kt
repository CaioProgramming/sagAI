package com.ilustris.sagai.features.chat.ui.presentation

sealed interface ChatState {
    data object Loading : ChatState

    data object Success : ChatState

    data class Error(
        val message: String,
    ) : ChatState

    data object Empty : ChatState
}
