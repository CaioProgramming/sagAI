package com.ilustris.sagai.features.characters.events.data.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import javax.inject.Inject

class CharacterEventRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : CharacterEventRepository {
        override suspend fun insertCharacterEvent(characterEvent: CharacterEvent) =
            characterEvent.copy(
                id = database.characterEventDao().insertCharacterEvent(characterEvent).toInt(),
            )

        override suspend fun insertCharacterEvents(characterEvents: List<CharacterEvent>) =
            database.characterEventDao().insertCharacterEvents(characterEvents)
    }
