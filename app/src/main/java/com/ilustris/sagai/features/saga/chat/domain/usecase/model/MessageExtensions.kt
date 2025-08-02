package com.ilustris.sagai.features.saga.chat.domain.usecase.model

import com.ilustris.sagai.features.characters.data.model.Character

fun List<MessageContent>.filterInteractionMessages() =
    filter {
        it.message.senderType != SenderType.NEW_CHAPTER &&
            it.message.senderType != SenderType.NEW_ACT &&
            it.message.senderType != SenderType.NEW_CHARACTER
    }

fun MessageContent.joinMessage(showSender: Boolean = true): Pair<String, String> =
    when (message.senderType) {
        SenderType.USER -> "${character?.name}(${message.senderType})" to message.text
        SenderType.CHARACTER ->
            "${(character?.name) ?: "Unknown"} ${
                senderDescription(
                    message.senderType,
                    showSender,
                )
            }" to message.text

        SenderType.THOUGHT,
        SenderType.ACTION,
        ->
            (
                "${character?.name}${
                    senderDescription(
                        message.senderType,
                        showSender,
                    )
                }"
            ) to message.text

        else -> message.senderType.name to message.text
    }

fun MessageContent.isUser(mainCharacter: Character?): Boolean {
    if (message.senderType == SenderType.CHARACTER) return false
    return message.senderType == SenderType.USER ||
        message.characterId == mainCharacter?.id ||
        message.speakerName.equals(mainCharacter?.name, true)
}

fun List<MessageContent>.filterCharacterMessages(character: Character?) =
    if (character == null) {
        emptyList()
    } else {
        filter {
            it.character?.id == character.id || it.message.speakerName == character.name
        }
    }

fun List<MessageContent>.filterMessageType(type: SenderType) = filter { it.message.senderType == type }

fun List<MessageContent>.filterMention(name: String) =
    filter {
        it.message.text.contains(name, true)
    }

fun List<MessageContent>.rankMentions(characters: List<Character>): List<Pair<Character, Int>> =
    characters
        .map { character ->
            character to this.filterMention(character.name).size
        }.sortedByDescending {
            it.second
        }

fun List<MessageContent>.rankTopCharacters(characters: List<Character>): List<Pair<Character, Int>> =
    characters
        .map {
            it to filterCharacterMessages(it).size
        }.sortedByDescending {
            it.second
        }

fun List<MessageContent>.rankMessageTypes() =
    SenderType.entries
        .map {
            it to this.filterMessageType(it).size
        }.sortedByDescending {
            it.second
        }
