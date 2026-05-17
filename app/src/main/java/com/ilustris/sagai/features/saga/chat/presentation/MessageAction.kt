package com.ilustris.sagai.features.saga.chat.presentation

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent

sealed class MessageAction {
    data class PlayAudio(
        val message: MessageContent,
    ) : MessageAction()

    data class RetryMessage(
        val message: MessageContent,
    ) : MessageAction()

    data class ClickCharacter(
        val character: Character?,
    ) : MessageAction()

    data class RegenerateAudio(
        val message: MessageContent,
    ) : MessageAction()

    data class ClickReactions(
        val message: MessageContent,
    ) : MessageAction()

    data class RequestNewCharacter(
        val name: String,
        val message: Message,
    ) : MessageAction()

    data class ToggleSelection(
        val messageId: Int,
    ) : MessageAction()

    data class LongPress(
        val messageId: Int,
    ) : MessageAction()
}
