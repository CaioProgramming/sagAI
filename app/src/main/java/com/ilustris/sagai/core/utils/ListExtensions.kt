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

fun List<*>.normalizetoAIItems(excludingFields: List<String> = emptyList()): String {
    if (isEmpty()) return ""
    val firstItem = first()

    return if (firstItem is String || firstItem is Number || firstItem is Boolean || firstItem is Enum<*>) {
        // For primitive-like types, just join them
        joinToString(", ")
    } else {
        // For complex objects, normalize each item and prepend its type and index
        filterNotNull()
            .mapIndexed { index, item ->
                val normalizedItem = item.toAINormalize(excludingFields)
                if (normalizedItem.isNotBlank()) {
                    "${item.javaClass.simpleName}[$index]: \n${normalizedItem.prependIndent("    ")}"
                } else {
                    "${item.javaClass.simpleName}[$index]: (empty)"
                }
            }.joinToString("\n")
    }
}

fun Array<*>.normalizetoAIItems(excludingFields: List<String> = emptyList()): String = this.toList().normalizetoAIItems(excludingFields)
