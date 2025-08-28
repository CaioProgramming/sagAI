package com.ilustris.sagai.features.timeline.data.repository

import com.ilustris.sagai.features.characters.data.model.CharacterEvent
import kotlinx.coroutines.flow.Flow

interface CharacterEventRepository {


    suspend fun insertCharacterEvent(characterEvent: CharacterEvent) : CharacterEvent

    suspend fun insertCharacterEvents(characterEvents: List<CharacterEvent>)

}