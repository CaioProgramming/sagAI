package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.features.home.data.model.Saga

data class SagaCreationGen(
    val message: String,
    val inputHint: String,
    val suggestions: List<String>,
    val callback: CallbackContent?,
)

data class CallbackContent(
    val action: CallBackAction,
    val data: SagaForm?,
)

enum class CallBackAction {
    UPDATE_DATA,
    AWAITING_CONFIRMATION,
    SAVE_SAGA,
}

enum class Sender {
    USER,
    AI,
}

data class ChatMessage(
    val id: String = System.nanoTime().toString(),
    val text: String,
    val sender: Sender,
    val sagaForm: SagaForm? = null,
)
