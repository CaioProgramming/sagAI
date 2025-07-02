package com.ilustris.sagai.core.utils

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent

fun <T> List<T>.afterLast(predicate: (T) -> Boolean): List<T> {
    val index = indexOfLast(predicate)
    return if (index != -1 && index < lastIndex) subList(index + 1, size) else emptyList()
}

fun List<Any>.formatToJsonArray() = joinToString(separator = ",\n") { it.toJsonFormat() }

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
