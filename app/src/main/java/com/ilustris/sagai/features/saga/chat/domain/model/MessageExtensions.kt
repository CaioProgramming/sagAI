package com.ilustris.sagai.features.saga.chat.domain.model

import com.ilustris.sagai.features.characters.data.model.Character

fun MessageContent.joinMessage(): Pair<String, String> = ((character?.name) ?: "Unknown") to message.text

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
