package com.ilustris.sagai.core.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.SenderType

class ChatGenClient {
    val model: GenerativeModel by lazy {
        Firebase
            .ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")
    }

    fun startChat(messages: List<Message>) {
        val mappedMessages =
            messages.map {
                content(role = if (it.senderType == SenderType.USER) "user" else "model") {
                    text(it.text)
                }
            }
        model.startChat(
            mappedMessages,
        )
    }
}
