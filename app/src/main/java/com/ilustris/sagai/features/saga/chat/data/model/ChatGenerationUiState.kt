package com.ilustris.sagai.features.saga.chat.data.model

import com.ilustris.sagai.features.newsaga.data.model.Genre

sealed interface ChatGenerationUiState {
    data class Generating(
        val sagaId: Int,
        val sagaTitle: String,
        val genre: Genre,
        val speakerName: String?,
        val reasoning: String?,
    ) : ChatGenerationUiState
}

sealed interface ChatGenerationOutcome {
    data class Success(
        val sagaId: Int,
        val reply: AIReply,
    ) : ChatGenerationOutcome

    data class GuardrailBlocked(
        val sagaId: Int,
        val originalMessage: Message,
    ) : ChatGenerationOutcome

    data class Error(
        val sagaId: Int,
        val originalMessage: Message,
        val throwable: Throwable?,
    ) : ChatGenerationOutcome
}
