package com.ilustris.sagai.features.saga.chat.presentation

sealed class ChatState {
    data object Loading : ChatState()

    data object Success : ChatState()

    data class Error(
        val message: String,
    ) : ChatState()

    data object Empty : ChatState()
}

data class SnackBarState(
    val title: String,
    val text: String,
    val redirectAction: Triple<ChatAction, String, Any?>? = null,
)

enum class ChatAction {
    RESEND,
    OPEN_TIMELINE,
    OPEN_CHARACTER,
}
