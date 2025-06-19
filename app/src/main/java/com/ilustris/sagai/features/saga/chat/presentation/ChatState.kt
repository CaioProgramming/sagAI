package com.ilustris.sagai.features.saga.chat.presentation

sealed interface ChatState {
    data object Loading : ChatState

    data object Success : ChatState

    data class Error(
        val message: String,
    ) : ChatState

    data class LoreUpdated(
        val lore: String?,
    ) : ChatState

    data object Empty : ChatState
}
