package com.ilustris.sagai.features.saga.chat.domain.model

import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.saga.chat.data.model.Message

data class MessageGen(
    val message: Message,
    val newCharacter: CharacterInfo? = null,
    val shouldCreateCharacter: Boolean = false,
    val shouldEndSaga: Boolean = false,
)
