package com.ilustris.sagai.features.characters.events.data.source

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import kotlinx.coroutines.flow.Flow

class CharacterEventDaoImpl(
    private val database: SagaDatabase,
) : CharacterEventDao {
    private val dao by lazy {
        database.characterEventDao()
    }

    override suspend fun insertCharacterEvent(characterEvent: CharacterEvent): Long = dao.insertCharacterEvent(characterEvent)

    override suspend fun insertCharacterEvents(characterEvents: List<CharacterEvent>) = dao.insertCharacterEvents(characterEvents)
}
