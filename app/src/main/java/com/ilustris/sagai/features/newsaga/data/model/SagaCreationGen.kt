package com.ilustris.sagai.features.newsaga.data.model

data class CreationSuggestion(
    val text: String = "",
    val title: String = "",
    val description: String = "",
    val genre: Genre = Genre.FANTASY,
)

data class SagaCreationGen(
    val message: String,
    val inputHint: String,
    val suggestions: List<CreationSuggestion>,
    val callback: CallbackContent?,
)

data class CallbackContent(
    val action: CallBackAction,
    val data: SagaDraft?,
)

enum class CallBackAction {
    UPDATE_DATA,
    AWAITING_CONFIRMATION,
    CONTENT_READY,
}

enum class Sender {
    USER,
    AI,
}

data class ChatMessage(
    val id: String = System.nanoTime().toString(),
    val text: String,
    val sender: Sender,
    val callback: CallBackAction? = null,
    val audioFile: java.io.File? = null,
)

data class CreationAssist(
    val title: String = "",
    val subtitle: String = "",
    val suggestions: List<CreationSuggestion> = emptyList(),
    val inputHint: String = "",
)
