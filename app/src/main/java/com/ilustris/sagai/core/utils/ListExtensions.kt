package com.ilustris.sagai.core.utils

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent

fun List<Any>.formatToJsonArray(excludingFields: List<String> = emptyList()) =
    joinToString(prefix = "[", postfix = "]", separator = ",\n") { it.toJsonFormatExcludingFields(excludingFields) }

fun sortCharactersByMessageCount(
    characters: List<Character>,
    messages: List<MessageContent>,
) = characters.sortedByDescending { character ->
    messages
        .count { message ->

            message.character?.id == character.id ||
                message.message.speakerName.equals(character.name, true)
        }
}

fun sortCharactersContentByMessageCount(
    characters: List<CharacterContent>,
    messages: List<MessageContent>,
) = characters.sortedByDescending { character ->
    messages
        .count { message ->

            message.character?.id == character.data.id ||
                message.message.speakerName.equals(character.data.name, true)
        }
}

fun List<Any>.listToAINormalize(excludingFields: List<String> = emptyList()): String =
    this.joinToString(separator = ";\n") { item ->
        "• ${item.toAINormalize(excludingFields)}"
    }
