package com.ilustris.sagai.features.characters.events.data.repository

import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent

interface CharacterEventRepository {
    suspend fun insertCharacterEvent(characterEvent: CharacterEvent): CharacterEvent

    suspend fun insertCharacterEvents(characterEvents: List<CharacterEvent>)
}
